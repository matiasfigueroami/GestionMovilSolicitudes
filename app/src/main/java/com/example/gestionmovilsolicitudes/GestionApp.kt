package com.example.gestionmovilsolicitudes

import android.app.Application
import com.example.gestionmovilsolicitudes.data.AppDatabase
import com.example.gestionmovilsolicitudes.data.remote.ApiClient
import com.example.gestionmovilsolicitudes.data.repository.IndicadoresRepository
import com.example.gestionmovilsolicitudes.data.repository.SolicitudRepository
import com.example.gestionmovilsolicitudes.domain.ReglasNotificacion
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.notificaciones.Notificador
import com.example.gestionmovilsolicitudes.notificaciones.UltimaConexion
import com.example.gestionmovilsolicitudes.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class GestionApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    // Los datos viven en los microservicios Spring Boot; Room guarda una copia local
    val repository by lazy { SolicitudRepository(database, ApiClient.usuarios, ApiClient.solicitudes) }

    // API externa (mindicador.cl) consumida con Retrofit
    val indicadoresRepository by lazy { IndicadoresRepository(ApiClient.indicadores) }

    val notificador by lazy { Notificador(this) }
    private val ultimaConexion by lazy { UltimaConexion(this) }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Los eventos del repositorio se muestran como notificaciones locales
        scope.launch { repository.eventos.collect { notificador.mostrar(it) } }

        // Al iniciar sesión, cada perfil recibe las novedades de sus solicitudes
        scope.launch {
            SessionManager.usuario
                .filterNotNull()
                .collect { usuario ->
                    val desde = ultimaConexion.obtener(usuario.id)
                    val puedeNotificar = notificador.tienePermiso()

                    // Cambios hechos por otras personas desde su última sesión
                    if (desde != null && puedeNotificar) {
                        ReglasNotificacion
                            .novedades(usuario.perfil, repository.novedadesDesde(usuario, desde))
                            .forEach { notificador.mostrar(it) }
                    }
                    // Si todavía no puede recibir avisos, las novedades se conservan para la próxima sesión
                    if (desde == null || puedeNotificar) {
                        ultimaConexion.guardar(usuario.id, System.currentTimeMillis())
                    }

                    // El administrador además recibe el aviso de solicitudes atrasadas
                    if (usuario.perfil == Perfil.ADMINISTRADOR) {
                        val atrasadas = repository.contarAtrasadas(usuario)
                        if (atrasadas > 0) notificador.mostrar(ReglasNotificacion.atrasadas(atrasadas))
                    }
                }
        }
    }
}