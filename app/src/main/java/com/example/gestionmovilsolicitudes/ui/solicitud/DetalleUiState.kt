package com.example.gestionmovilsolicitudes.ui.solicitud

import com.example.gestionmovilsolicitudes.data.dao.EventoHistorial
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity   // ← NUEVO import
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.domain.ReglasEstado
data class DetalleUiState(
    val cargando: Boolean = true,
    val perfil: Perfil? = null,
    val solicitud: SolicitudEntity? = null,
    val clienteNombre: String? = null,
    val responsableNombre: String? = null,
    val historial: List<EventoHistorial> = emptyList(),

    // ---- NUEVOS (los 3) ----
    val esAdmin: Boolean = false,
    val tecnicos: List<UsuarioEntity> = emptyList(),
    val estadosDisponibles: List<EstadoSolicitud> = emptyList(),
    // -------------------------

    val comentario: String = "",
    val comentarioError: String? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null



) {
    private val estado: EstadoSolicitud? get() = solicitud?.estado

    // Qué acciones se ofrecen según el perfil y el estado de la solicitud
    val puedeEditar: Boolean
        get() = perfil != null && perfil != Perfil.TECNICO &&
                estado != null && estado != EstadoSolicitud.CERRADA && estado != EstadoSolicitud.CANCELADA

    val puedeConfirmar: Boolean
        get() = perfil == Perfil.CLIENTE && estado == EstadoSolicitud.RESUELTA

    val puedeReabrir: Boolean
        get() = perfil == Perfil.CLIENTE &&
                (estado == EstadoSolicitud.RESUELTA || estado == EstadoSolicitud.CERRADA)

    val puedeResponder: Boolean
        get() = perfil == Perfil.CLIENTE && estado == EstadoSolicitud.PENDIENTE_INFORMACION

    val puedeComentar: Boolean
        get() = estado != null && estado != EstadoSolicitud.CERRADA && estado != EstadoSolicitud.CANCELADA

    // ---- Acciones del técnico: salen de ReglasEstado según el estado actual ----
    private val destinosTecnico: Set<EstadoSolicitud>
        get() {
            val e = estado ?: return emptySet()
            return if (perfil == Perfil.TECNICO) ReglasEstado.permitidos(Perfil.TECNICO, e) else emptySet()
        }

    val puedeIniciar: Boolean
        get() = estado == EstadoSolicitud.ASIGNADA && EstadoSolicitud.EN_PROCESO in destinosTecnico

    val puedeRegistrarAvance: Boolean
        get() = perfil == Perfil.TECNICO && estado == EstadoSolicitud.EN_PROCESO

    val puedePedirInformacion: Boolean
        get() = EstadoSolicitud.PENDIENTE_INFORMACION in destinosTecnico

    val puedeResolver: Boolean
        get() = EstadoSolicitud.RESUELTA in destinosTecnico

    val tieneAccionesTecnico: Boolean
        get() = puedeIniciar || puedeRegistrarAvance || puedePedirInformacion || puedeResolver

    // ---- Textos que el cliente necesita leer sin buscar en el historial ----
    val ultimaConsulta: String?
        get() = historial.lastOrNull {
            it.estadoNuevo == EstadoSolicitud.PENDIENTE_INFORMACION && !it.comentario.isNullOrBlank()
        }?.comentario

    val ultimaSolucion: String?
        get() = historial.lastOrNull {
            it.estadoNuevo == EstadoSolicitud.RESUELTA && !it.comentario.isNullOrBlank()
        }?.comentario

}