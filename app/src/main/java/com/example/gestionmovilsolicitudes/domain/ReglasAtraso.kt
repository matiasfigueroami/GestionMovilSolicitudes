package com.example.gestionmovilsolicitudes.domain

import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud

/** Una solicitud está atrasada si sigue sin resolverse pasados [DIAS_PARA_ATRASO] días desde su creación. */
object ReglasAtraso {
    const val DIAS_PARA_ATRASO = 7
    private const val DIA_MS = 24L * 60 * 60 * 1000

    fun limite(ahora: Long = System.currentTimeMillis()): Long = ahora - DIAS_PARA_ATRASO * DIA_MS

    fun esAtrasada(s: SolicitudEntity, ahora: Long = System.currentTimeMillis()): Boolean =
        s.fechaCreacion < limite(ahora) && s.estado !in setOf(
            EstadoSolicitud.RESUELTA, EstadoSolicitud.CERRADA, EstadoSolicitud.CANCELADA
        )
}