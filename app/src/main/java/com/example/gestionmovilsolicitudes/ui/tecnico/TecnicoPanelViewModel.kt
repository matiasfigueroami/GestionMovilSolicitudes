package com.example.gestionmovilsolicitudes.ui.tecnico

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionmovilsolicitudes.GestionApp
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.domain.ReglasAtraso
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class TecnicoPanelViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as GestionApp).repository

    private val filtroEstado = MutableStateFlow<EstadoSolicitud?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TecnicoPanelUiState> = SessionManager.usuario
        .flatMapLatest { usuario ->
            if (usuario == null || usuario.perfil != Perfil.TECNICO) {
                flowOf(TecnicoPanelUiState(cargando = false, autorizado = false))
            } else {
                combine(
                    repo.observarVisibles(usuario),   // para el técnico: solo las que tiene asignadas
                    repo.observarActividad(usuario),
                    filtroEstado
                ) { asignadas, actividad, filtro ->
                    val ahora = System.currentTimeMillis()
                    val visibles = asignadas
                        .filter { filtro == null || it.estado == filtro }
                        // Primero las más urgentes y, a igual prioridad, las más antiguas
                        .sortedWith(
                            compareByDescending<SolicitudEntity> { it.prioridad?.ordinal ?: -1 }
                                .thenBy { it.fechaCreacion }
                        )

                    TecnicoPanelUiState(
                        cargando = false,
                        total = asignadas.size,
                        atrasadas = asignadas.count { ReglasAtraso.esAtrasada(it, ahora) },
                        porEstado = ESTADOS.map { e -> e to asignadas.count { it.estado == e } },
                        filtroEstado = filtro,
                        solicitudes = visibles,
                        actividad = actividad
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TecnicoPanelUiState())

    /** Tocar un estado lo filtra; tocarlo otra vez quita el filtro. */
    fun onEstado(estado: EstadoSolicitud) {
        filtroEstado.value = if (filtroEstado.value == estado) null else estado
    }

    fun quitarFiltro() { filtroEstado.value = null }

    private companion object {
        val ESTADOS = listOf(
            EstadoSolicitud.ASIGNADA,
            EstadoSolicitud.EN_PROCESO,
            EstadoSolicitud.PENDIENTE_INFORMACION,
            EstadoSolicitud.RESUELTA,
            EstadoSolicitud.CERRADA
        )
    }
}
