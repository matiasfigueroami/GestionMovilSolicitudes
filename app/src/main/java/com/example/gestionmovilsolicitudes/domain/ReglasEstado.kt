package com.example.gestionmovilsolicitudes.domain

import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud.*
import com.example.gestionmovilsolicitudes.model.Perfil

object ReglasEstado {

    private val admin = mapOf(
        RECIBIDA to setOf(EN_REVISION, CANCELADA),
        EN_REVISION to setOf(PENDIENTE_INFORMACION, ASIGNADA, CANCELADA),
        PENDIENTE_INFORMACION to setOf(EN_REVISION, CANCELADA),
        ASIGNADA to setOf(EN_PROCESO, PENDIENTE_INFORMACION, CANCELADA),
        EN_PROCESO to setOf(PENDIENTE_INFORMACION, RESUELTA, CANCELADA),
        RESUELTA to setOf(CERRADA, EN_PROCESO)
    )

    private val tecnico = mapOf(
        ASIGNADA to setOf(EN_PROCESO),
        EN_PROCESO to setOf(PENDIENTE_INFORMACION, RESUELTA)
    )

    private val cliente = mapOf(
        PENDIENTE_INFORMACION to setOf(EN_REVISION), // respondió la información pedida
        RESUELTA to setOf(CERRADA, EN_PROCESO),      // confirma o reabre
        CERRADA to setOf(EN_PROCESO)                 // reapertura
    )

    fun permitidos(perfil: Perfil, desde: EstadoSolicitud): Set<EstadoSolicitud> {
        val tabla = when (perfil) {
            Perfil.ADMINISTRADOR -> admin
            Perfil.TECNICO -> tecnico
            Perfil.CLIENTE -> cliente
        }
        return tabla[desde].orEmpty()
    }
}