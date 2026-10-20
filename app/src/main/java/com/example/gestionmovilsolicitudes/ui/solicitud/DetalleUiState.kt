package com.example.gestionmovilsolicitudes.ui.solicitud

import com.example.gestionmovilsolicitudes.data.dao.EventoHistorial
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil

data class DetalleUiState(
    val cargando: Boolean = true,
    val perfil: Perfil? = null,
    val solicitud: SolicitudEntity? = null,
    val clienteNombre: String? = null,
    val responsableNombre: String? = null,
    val historial: List<EventoHistorial> = emptyList(),

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
}