package com.example.gestionmovilsolicitudes.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.gestionmovilsolicitudes.MainActivity
import com.example.gestionmovilsolicitudes.R
import com.example.gestionmovilsolicitudes.domain.NotificacionSolicitud
import com.example.gestionmovilsolicitudes.model.etiqueta

/**
 * Recurso nativo: notificaciones locales. Crea el canal, comprueba el permiso
 * (obligatorio desde Android 13) y publica los avisos de las solicitudes.
 */
class Notificador(private val context: Context) {

    init {
        crearCanal()
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // IMPORTANCE_HIGH hace que el aviso aparezca como banner emergente sobre la pantalla
            val canal = NotificationChannel(CANAL_ID, "Avisos de solicitudes", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos sobre cambios en las solicitudes"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.deleteNotificationChannel(CANAL_ANTERIOR) // el canal antiguo no permite subir su importancia
            manager.createNotificationChannel(canal)
        }
    }

    /** Antes de Android 13 no se pide permiso; desde Android 13 el usuario debe concederlo. */
    fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // el permiso se verifica con tienePermiso()
    fun mostrar(aviso: NotificacionSolicitud) {
        if (!tienePermiso() || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.texto))
            .setSubText("Para: ${aviso.para.etiqueta()}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacion)
    }

    private companion object {
        const val CANAL_ID = "solicitudes_avisos"
        const val CANAL_ANTERIOR = "solicitudes"
    }
}