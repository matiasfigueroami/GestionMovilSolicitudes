package com.example.gestionmovilsolicitudes.domain

import com.example.gestionmovilsolicitudes.data.dao.NovedadHistorial
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad
import com.example.gestionmovilsolicitudes.model.etiqueta

/** Aviso que la app muestra como notificación local. [para] indica a qué perfil va dirigido. */
data class NotificacionSolicitud(
    val solicitudId: Long,
    val titulo: String,
    val texto: String,
    val para: Perfil
)

/** Decide qué aviso corresponde a cada evento y a quién va dirigido (lógica pura, sin Android). */
object ReglasNotificacion {

    private fun detalle(s: SolicitudEntity) = "${s.codigoSeguimiento} · ${s.titulo}"

    fun nuevaSolicitud(s: SolicitudEntity) =
        NotificacionSolicitud(s.id, "Nueva solicitud recibida", detalle(s), Perfil.ADMINISTRADOR)

    fun asignada(s: SolicitudEntity) =
        NotificacionSolicitud(s.id, "Solicitud asignada", detalle(s), Perfil.TECNICO)

    fun prioridadCambiada(s: SolicitudEntity, prioridad: Prioridad) =
        NotificacionSolicitud(s.id, "Cambio de prioridad", "${detalle(s)} · ahora ${prioridad.etiqueta()}", Perfil.CLIENTE)

    fun avance(s: SolicitudEntity) =
        NotificacionSolicitud(s.id, "Nuevo avance en tu solicitud", detalle(s), Perfil.CLIENTE)

    fun comentario(s: SolicitudEntity, autor: Perfil) = NotificacionSolicitud(
        s.id, "Nuevo comentario", detalle(s),
        if (autor == Perfil.CLIENTE) Perfil.ADMINISTRADOR else Perfil.CLIENTE
    )

    fun atrasadas(cantidad: Int) = NotificacionSolicitud(
        0, "Solicitudes atrasadas",
        "Hay $cantidad sin resolver hace más de ${ReglasAtraso.DIAS_PARA_ATRASO} días",
        Perfil.ADMINISTRADOR
    )

    private const val MAX_INDIVIDUALES = 3

    /**
     * Avisos que se muestran al iniciar sesión por lo que otros hicieron mientras el usuario no estaba:
     * uno por cambio (hasta 3) o un resumen si son más.
     */
    fun novedades(perfil: Perfil, novedades: List<NovedadHistorial>): List<NotificacionSolicitud> = when {
        novedades.isEmpty() -> emptyList()
        novedades.size <= MAX_INDIVIDUALES -> novedades.asReversed().map { n ->
            NotificacionSolicitud(
                n.solicitudId,
                n.accion,
                "${n.codigo} · ${n.titulo}" + (n.comentario?.let { " — $it" } ?: ""),
                perfil
            )
        }
        else -> listOf(
            NotificacionSolicitud(
                0, "Tienes ${novedades.size} novedades",
                "Hubo cambios en tus solicitudes desde tu última sesión", perfil
            )
        )
    }

    /** Aviso por un cambio de estado; null si ese estado no genera notificación. */
    fun cambioEstado(s: SolicitudEntity, nuevo: EstadoSolicitud, autor: Perfil): NotificacionSolicitud? {
        val d = detalle(s)
        val porCliente = autor == Perfil.CLIENTE
        return when (nuevo) {
            EstadoSolicitud.EN_REVISION ->
                if (porCliente) NotificacionSolicitud(s.id, "El cliente respondió", d, Perfil.ADMINISTRADOR)
                else NotificacionSolicitud(s.id, "Solicitud en revisión", d, Perfil.CLIENTE)

            EstadoSolicitud.PENDIENTE_INFORMACION ->
                NotificacionSolicitud(s.id, "Necesitamos más información", d, Perfil.CLIENTE)

            EstadoSolicitud.ASIGNADA ->
                NotificacionSolicitud(s.id, "Solicitud asignada", d, Perfil.TECNICO)

            EstadoSolicitud.EN_PROCESO ->
                if (porCliente) NotificacionSolicitud(s.id, "Solicitud reabierta", d, Perfil.ADMINISTRADOR)
                else NotificacionSolicitud(s.id, "Tu solicitud está en proceso", d, Perfil.CLIENTE)

            EstadoSolicitud.RESUELTA ->
                NotificacionSolicitud(s.id, "Solicitud resuelta", "$d · confirma si estás conforme", Perfil.CLIENTE)

            EstadoSolicitud.CERRADA ->
                NotificacionSolicitud(s.id, "Solicitud cerrada", d, if (porCliente) Perfil.ADMINISTRADOR else Perfil.CLIENTE)

            EstadoSolicitud.CANCELADA ->
                NotificacionSolicitud(s.id, "Solicitud cancelada", d, Perfil.CLIENTE)

            EstadoSolicitud.RECIBIDA -> null
        }
    }
}

