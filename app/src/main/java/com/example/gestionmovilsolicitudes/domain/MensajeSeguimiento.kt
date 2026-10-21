package com.example.gestionmovilsolicitudes.domain

import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.model.etiqueta

/**
 * Mensaje estructurado para compartir una solicitud (por WhatsApp, correo u otra app).
 * Solo incluye código, título, categoría y una referencia ficticia de seguimiento:
 * nunca la descripción, datos del cliente ni información sensible.
 */
object MensajeSeguimiento {
    private const val BASE_ENLACE = "https://seguimiento.example.com/solicitudes/"

    fun enlace(s: SolicitudEntity): String = BASE_ENLACE + s.codigoSeguimiento

    fun construir(s: SolicitudEntity): String = buildString {
        appendLine("Solicitud ${s.codigoSeguimiento}")
        appendLine("Título: ${s.titulo}")
        appendLine("Categoría: ${s.categoria.etiqueta()}")
        append("Seguimiento: ${enlace(s)}")
    }
}