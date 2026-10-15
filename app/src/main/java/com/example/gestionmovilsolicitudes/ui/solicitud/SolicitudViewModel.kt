package com.example.gestionmovilsolicitudes.ui.solicitud
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionmovilsolicitudes.GestionApp
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.domain.ValidadorSolicitud
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SolicitudViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as GestionApp).repository

    // ---------- Estado del formulario ----------
    private val _uiState = MutableStateFlow(SolicitudUiState())
    val uiState: StateFlow<SolicitudUiState> = _uiState.asStateFlow()

    // ---------- Estado de la lista (se actualiza sola al guardar) ----------
    @OptIn(ExperimentalCoroutinesApi::class)
    val solicitudes: StateFlow<List<SolicitudEntity>> = SessionManager.usuario
        .flatMapLatest { u -> if (u == null) flowOf(emptyList()) else repo.observarVisibles(u) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---------- Eventos del formulario ----------
    fun onTituloChange(v: String) = _uiState.update { it.copy(titulo = v, tituloError = null) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(descripcion = v, descripcionError = null) }
    fun onCategoriaChange(v: String) = _uiState.update { it.copy(categoria = v, categoriaError = null, servicioError = null) }
    fun onServicioChange(v: String) = _uiState.update { it.copy(servicio = v, servicioError = null) }
    fun onEvidenciaChange(uri: String?) = _uiState.update { it.copy(evidenciaUri = uri) }

    fun limpiarFormulario() { _uiState.value = SolicitudUiState() }

    fun cargarParaEditar(id: Long) {
        val usuario = SessionManager.usuario.value ?: return
        viewModelScope.launch {
            try {
                val s = repo.obtener(id, usuario)
                _uiState.update {
                    it.copy(
                        editandoId = s.id,
                        codigo = s.codigoSeguimiento,
                        titulo = s.titulo,
                        descripcion = s.descripcion,
                        categoria = s.categoria.name,
                        servicio = s.servicioRelacionado.orEmpty(),
                        evidenciaUri = s.evidenciaUri
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeError = e.message ?: "No se pudo cargar la solicitud") }
            }
        }
    }

    fun guardar() {
        val usuario = SessionManager.usuario.value ?: return
        val st = _uiState.value
        if (st.guardando) return

        // 1. Validar: los errores se publican en el estado y la pantalla solo los muestra
        val r = ValidadorSolicitud.validar(st.titulo, st.descripcion, st.categoria, st.servicio)
        if (!r.valido) {
            _uiState.update {
                it.copy(
                    tituloError = r.titulo, descripcionError = r.descripcion,
                    categoriaError = r.categoria, servicioError = r.servicio
                )
            }
            return
        }

        // 2. Guardar
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null) }
            try {
                val categoria = Categoria.valueOf(st.categoria)
                if (st.editandoId == null) {
                    repo.crear(usuario, st.titulo, st.descripcion, categoria, st.servicio.ifBlank { null }, null, st.evidenciaUri)
                } else {
                    repo.editar(st.editandoId, usuario, st.titulo, st.descripcion, categoria, st.servicio, st.evidenciaUri)
                }
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensajeError = e.message ?: "No se pudo guardar") }
            }
        }
    }
}

