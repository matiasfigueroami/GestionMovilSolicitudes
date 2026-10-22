package com.example.gestionmovilsolicitudes.notificaciones

import android.content.Context

/** Recuerda cuándo inició sesión cada usuario por última vez, para avisarle de las novedades. */
class UltimaConexion(context: Context) {

    private val prefs = context.getSharedPreferences("novedades", Context.MODE_PRIVATE)

    private fun clave(usuarioId: Long) = "ultima_conexion_$usuarioId"

    /** Instante de la última sesión, o null si el usuario nunca había entrado. */
    fun obtener(usuarioId: Long): Long? =
        if (prefs.contains(clave(usuarioId))) prefs.getLong(clave(usuarioId), 0L) else null

    fun guardar(usuarioId: Long, instante: Long) {
        prefs.edit().putLong(clave(usuarioId), instante).apply()
    }
}