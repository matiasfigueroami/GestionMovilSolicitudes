package com.example.gestionmovilsolicitudes.ui.solicitud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionmovilsolicitudes.GestionApp
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.gestionmovilsolicitudes.domain.ReglasEstado
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad

class DetalleViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as GestionApp).repository

    // Estado propio de la pantalla (lo que escribe o provoca el usuario)
    private data class Local(
        val comentario: String = "",
        val comentarioError: String? = null,
        val procesando: Boolean = false,
        val mensaje: String? = null
    )

    private val solicitudId = MutableStateFlow<Long?>(null)
    private val local = MutableStateFlow(Local())

    // Une: la solicitud (Room) + su historial (Room) + el estado local
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DetalleUiState> =
        combine(solicitudId, SessionManager.usuario) { id, usuario -> id to usuario }
            .flatMapLatest { (id, usuario) ->
                if (id == null || usuario == null) {
                    flowOf(DetalleUiState())
                } else {
                    // ---- NUEVO: declarar esAdmin y tecnicosFlow ----
                    val esAdmin = usuario.perfil == Perfil.ADMINISTRADOR
                    val tecnicosFlow =
                        if (esAdmin) repo.observarTecnicos() else flowOf(emptyList())
                    // ------------------------------------------------

                    combine(
                        repo.observarDetalle(id, usuario),
                        repo.observarHistorial(id),
                        local,
                        tecnicosFlow
                    ) { detalle, historial, l, tecnicos ->
                        val estadoActual = detalle?.solicitud?.estado
                        val estadosDisponibles =
                            if (esAdmin && estadoActual != null) {
                                ReglasEstado.permitidos(Perfil.ADMINISTRADOR, estadoActual)
                                    .filter { it != EstadoSolicitud.ASIGNADA }
                                    .toList()
                            } else emptyList()

                        DetalleUiState(
                            cargando = false,
                            perfil = usuario.perfil,
                            esAdmin = esAdmin,
                            tecnicos = tecnicos,
                            estadosDisponibles = estadosDisponibles,
                            solicitud = detalle?.solicitud,
                            clienteNombre = detalle?.clienteNombre,
                            responsableNombre = detalle?.responsableNombre,
                            historial = if (detalle == null) emptyList() else historial,
                            comentario = l.comentario,
                            comentarioError = l.comentarioError,
                            procesando = l.procesando,
                            mensaje = l.mensaje
                        )
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState())

    fun cargar(id: Long) {
        solicitudId.value = id
    }

    fun onComentarioChange(v: String) =
        local.update { it.copy(comentario = v, comentarioError = null) }

    fun consumirMensaje() = local.update { it.copy(mensaje = null) }

    // ---------- Acciones ----------

    fun enviarComentario() {
        val texto = local.value.comentario
        if (texto.isBlank()) {
            local.update { it.copy(comentarioError = "Escribe un comentario") }
            return
        }
        ejecutar("Comentario agregado", limpiarComentario = true) { usuario, id ->
            repo.agregarComentario(id, texto, usuario)
        }
    }

    fun confirmarResolucion() =
        ejecutar("Gracias por confirmar. La solicitud quedó cerrada.") { usuario, id ->
            repo.cambiarEstado(id, EstadoSolicitud.CERRADA, usuario, "El cliente confirmó la resolución")
        }

    fun reabrir(motivo: String) {
        if (motivo.isBlank()) {
            local.update { it.copy(mensaje = "Indica el motivo de la reapertura") }
            return
        }
        ejecutar("Solicitud reabierta") { usuario, id ->
            repo.cambiarEstado(id, EstadoSolicitud.EN_PROCESO, usuario, motivo.trim())
        }
    }

    fun responderInformacion(respuesta: String) {
        if (respuesta.isBlank()) {
            local.update { it.copy(mensaje = "Escribe tu respuesta") }
            return
        }
        ejecutar("Respuesta enviada") { usuario, id ->
            repo.cambiarEstado(id, EstadoSolicitud.EN_REVISION, usuario, respuesta.trim())
        }
    }
    fun cambiarEstadoAdmin(nuevo: EstadoSolicitud) =
        ejecutar("Estado actualizado") { usuario, id ->
            repo.cambiarEstado(id, nuevo, usuario)
        }

    fun cancelarSolicitud(motivo: String) {
        if (motivo.isBlank()) {
            local.update { it.copy(mensaje = "Indica el motivo de la cancelación") }
            return
        }
        ejecutar("Solicitud cancelada") { usuario, id ->
            repo.cambiarEstado(id, EstadoSolicitud.CANCELADA, usuario, motivo.trim())
        }
    }
    fun cambiarPrioridad(nueva: Prioridad) =
        ejecutar("Prioridad actualizada") { usuario, id ->
            repo.definirPrioridad(id, nueva, usuario)
        }

    fun asignarTecnico(tecnicoId: Long) =
        ejecutar("Responsable actualizado") { usuario, id ->
            repo.asignarResponsable(id, tecnicoId, usuario)
        }





    // Ejecuta una acción del repositorio y publica el resultado como mensaje
    private fun ejecutar(
        exito: String,
        limpiarComentario: Boolean = false,
        accion: suspend (UsuarioEntity, Long) -> Unit
    ) {
        val usuario = SessionManager.usuario.value ?: return
        val id = solicitudId.value ?: return
        viewModelScope.launch {
            local.update { it.copy(procesando = true) }
            try {
                accion(usuario, id)
                local.update {
                    it.copy(
                        procesando = false,
                        mensaje = exito,
                        comentario = if (limpiarComentario) "" else it.comentario
                    )
                }
            } catch (e: Exception) {
                local.update { it.copy(procesando = false, mensaje = e.message ?: "No se pudo completar la acción") }
            }
        }
    }
}