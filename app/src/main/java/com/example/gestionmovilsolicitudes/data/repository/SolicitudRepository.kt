package com.example.gestionmovilsolicitudes.data.repository

import androidx.room.withTransaction
import com.example.gestionmovilsolicitudes.data.AppDatabase
import com.example.gestionmovilsolicitudes.data.dao.ActividadReciente
import com.example.gestionmovilsolicitudes.data.dao.EventoHistorial
import com.example.gestionmovilsolicitudes.data.dao.NovedadHistorial
import com.example.gestionmovilsolicitudes.data.dao.SolicitudDetalle
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import com.example.gestionmovilsolicitudes.data.remote.ApiException
import com.example.gestionmovilsolicitudes.data.remote.AsignacionRequest
import com.example.gestionmovilsolicitudes.data.remote.CambioEstadoRequest
import com.example.gestionmovilsolicitudes.data.remote.LoginRequest
import com.example.gestionmovilsolicitudes.data.remote.PrioridadRequest
import com.example.gestionmovilsolicitudes.data.remote.SolicitudDto
import com.example.gestionmovilsolicitudes.data.remote.SolicitudRequest
import com.example.gestionmovilsolicitudes.data.remote.SolicitudesApi
import com.example.gestionmovilsolicitudes.data.remote.TextoRequest
import com.example.gestionmovilsolicitudes.data.remote.UsuariosApi
import com.example.gestionmovilsolicitudes.data.remote.aEntidad
import com.example.gestionmovilsolicitudes.data.remote.llamarApi
import com.example.gestionmovilsolicitudes.domain.NotificacionSolicitud
import com.example.gestionmovilsolicitudes.domain.ReglasAtraso
import com.example.gestionmovilsolicitudes.domain.ReglasNotificacion
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Fuente de datos de la app.
 *
 * - Las escrituras (crear, editar, eliminar, cambiar estado, asignar, comentar...) se envían a los
 *   microservicios Spring Boot con Retrofit. El servidor aplica las reglas de negocio y registra el historial.
 * - La respuesta del servidor se guarda en Room, que funciona como caché local: las pantallas siguen
 *   observando Room (Flow) y se actualizan solas cuando llega la respuesta.
 * - Las lecturas descargan los datos del servidor al abrir cada pantalla (onStart → sincronizar).
 */
class SolicitudRepository(
    private val db: AppDatabase,
    private val usuariosApi: UsuariosApi,
    private val solicitudesApi: SolicitudesApi
) {

    private val solicitudDao = db.solicitudDao()
    private val historialDao = db.historialDao()
    private val usuarioDao = db.usuarioDao()
    private val clienteDao = db.clienteDao()

    // Avisos que la app convierte en notificaciones locales (el repositorio no conoce Android)
    private val _eventos = MutableSharedFlow<NotificacionSolicitud>(extraBufferCapacity = 32)
    val eventos: SharedFlow<NotificacionSolicitud> = _eventos.asSharedFlow()

    // Último problema al sincronizar con el servidor (null = todo bien). Se muestra en la lista.
    private val _avisoConexion = MutableStateFlow<String?>(null)
    val avisoConexion: StateFlow<String?> = _avisoConexion.asStateFlow()

    // Evita que dos pantallas sincronicen al mismo tiempo
    private val mutexSincronizacion = Mutex()

    // ---------- Sesión ----------

    /** Valida las credenciales en el microservicio de usuarios y descarga los datos del usuario. */
    suspend fun login(correo: String, password: String): UsuarioEntity {
        val usuario = llamarApi { usuariosApi.login(LoginRequest(correo.trim(), password)) }.aEntidad()
        sincronizar(usuario)
        return usuario
    }

    /**
     * Descarga usuarios, clientes, las solicitudes visibles y su historial, y reemplaza la caché de Room.
     * Lanza [ApiException] si algún microservicio no responde.
     */
    suspend fun sincronizar(usuario: UsuarioEntity) {
        mutexSincronizacion.withLock { descargarTodo(usuario) }
        _avisoConexion.value = null
    }

    private suspend fun descargarTodo(usuario: UsuarioEntity) {
        val usuarios = llamarApi { usuariosApi.usuarios() }
        val clientes = llamarApi { usuariosApi.clientes() }
        val solicitudes = llamarApi { solicitudesApi.listar(usuario.id) }
        val historial = llamarApi { solicitudesApi.historialVisible(usuario.id) }

        db.withTransaction {
            // Primero las tablas "padre" (por las claves foráneas de Room)
            usuarioDao.borrarExcepto(usuarios.map { it.id })
            usuarioDao.guardarTodos(usuarios.map { it.aEntidad() })
            clienteDao.borrarExcepto(clientes.map { it.id })
            clienteDao.guardarTodos(clientes.map { it.aEntidad() })

            if (solicitudes.isEmpty()) solicitudDao.borrarTodas()
            else solicitudDao.borrarExcepto(solicitudes.map { it.id })
            solicitudDao.guardarTodas(solicitudes.map { it.aEntidad() })

            historialDao.borrarTodo()
            historialDao.guardarTodos(historial.map { it.aEntidad() })
        }
    }

    /** Sincroniza sin interrumpir la pantalla: si falla, se sigue mostrando la caché y se publica el aviso. */
    suspend fun intentarSincronizar(usuario: UsuarioEntity) {
        try {
            sincronizar(usuario)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _avisoConexion.value = e.message ?: "No se pudieron actualizar los datos"
        }
    }

    /** Vuelve a descargar una solicitud y su historial. Si ya no existe (o no hay acceso) se quita de la caché. */
    private suspend fun refrescarSolicitud(id: Long, usuario: UsuarioEntity) {
        try {
            val dto = llamarApi { solicitudesApi.obtener(id, usuario.id) }
            guardarRespuesta(dto, usuario)
            _avisoConexion.value = null
        } catch (e: ApiException) {
            if (e.codigo == 404 || e.codigo == 403) solicitudDao.eliminarPorId(id)
            else _avisoConexion.value = e.message
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _avisoConexion.value = e.message ?: "No se pudo actualizar la solicitud"
        }
    }

    /** Guarda en Room la solicitud que devolvió el servidor junto con su historial actualizado. */
    private suspend fun guardarRespuesta(dto: SolicitudDto, usuario: UsuarioEntity): SolicitudEntity {
        asegurarReferencias(dto)
        val historial = llamarApi { solicitudesApi.historial(dto.id, usuario.id) }
        val entidad = dto.aEntidad()
        db.withTransaction {
            solicitudDao.guardar(entidad)
            historialDao.borrarDeSolicitud(dto.id)
            historialDao.guardarTodos(historial.map { it.aEntidad() })
        }
        return entidad
    }

    /** Si la solicitud apunta a un cliente o técnico que todavía no está en la caché, los descarga. */
    private suspend fun asegurarReferencias(dto: SolicitudDto) {
        val faltaCliente = clienteDao.obtenerPorId(dto.clienteId) == null
        val faltaResponsable = dto.responsableId?.let { usuarioDao.obtenerPorId(it) == null } ?: false
        if (faltaCliente || faltaResponsable) {
            val usuarios = llamarApi { usuariosApi.usuarios() }
            val clientes = llamarApi { usuariosApi.clientes() }
            db.withTransaction {
                usuarioDao.guardarTodos(usuarios.map { it.aEntidad() })
                clienteDao.guardarTodos(clientes.map { it.aEntidad() })
            }
        }
    }

    /** Estado anterior de la solicitud (para decidir la notificación). */
    private suspend fun estadoPrevio(id: Long, usuario: UsuarioEntity): SolicitudEntity =
        solicitudDao.obtenerPorId(id) ?: llamarApi { solicitudesApi.obtener(id, usuario.id) }.aEntidad()

    // ---------- Crear (POST) ----------

    suspend fun crear(
        usuario: UsuarioEntity,
        titulo: String,
        descripcion: String,
        categoria: Categoria,
        servicio: String? = null,
        sitio: String? = null,
        evidenciaUri: String? = null
    ): Long {
        val dto = llamarApi {
            solicitudesApi.crear(
                SolicitudRequest(titulo.trim(), descripcion.trim(), categoria, servicio, sitio, evidenciaUri),
                usuario.id
            )
        }
        val creada = guardarRespuesta(dto, usuario)
        _eventos.tryEmit(ReglasNotificacion.nuevaSolicitud(creada))
        return creada.id
    }

    // ---------- Editar (PUT) ----------

    suspend fun editar(
        solicitudId: Long,
        usuario: UsuarioEntity,
        titulo: String,
        descripcion: String,
        categoria: Categoria,
        servicio: String?,
        sitio: String?,
        evidenciaUri: String?
    ) {
        val dto = llamarApi {
            solicitudesApi.editar(
                solicitudId,
                SolicitudRequest(
                    titulo.trim(), descripcion.trim(), categoria,
                    servicio?.trim()?.ifBlank { null }, sitio?.trim()?.ifBlank { null }, evidenciaUri
                ),
                usuario.id
            )
        }
        guardarRespuesta(dto, usuario)
    }

    // ---------- Eliminar (DELETE) ----------

    /** Elimina una solicitud con su historial. Solo el administrador puede hacerlo (lo valida el servidor). */
    suspend fun eliminar(solicitudId: Long, admin: UsuarioEntity) {
        llamarApi {
            val respuesta = solicitudesApi.eliminar(solicitudId, admin.id)
            if (!respuesta.isSuccessful) throw retrofit2.HttpException(respuesta)
        }
        solicitudDao.eliminarPorId(solicitudId) // el historial local se borra por CASCADE
    }

    // ---------- Cambio de estado (PATCH) ----------

    suspend fun cambiarEstado(
        solicitudId: Long,
        nuevo: EstadoSolicitud,
        usuario: UsuarioEntity,
        comentario: String? = null,
        accion: String? = null
    ) {
        val antes = estadoPrevio(solicitudId, usuario)
        val dto = llamarApi {
            solicitudesApi.cambiarEstado(solicitudId, CambioEstadoRequest(nuevo, comentario, accion), usuario.id)
        }
        guardarRespuesta(dto, usuario)
        ReglasNotificacion.cambioEstado(antes, nuevo, usuario.perfil)?.let { _eventos.tryEmit(it) }
    }

    // ---------- Acciones del administrador ----------

    suspend fun definirPrioridad(solicitudId: Long, prioridad: Prioridad, admin: UsuarioEntity) {
        val antes = estadoPrevio(solicitudId, admin)
        val dto = llamarApi { solicitudesApi.definirPrioridad(solicitudId, PrioridadRequest(prioridad), admin.id) }
        guardarRespuesta(dto, admin)
        _eventos.tryEmit(ReglasNotificacion.prioridadCambiada(antes, prioridad))
    }

    suspend fun asignarResponsable(solicitudId: Long, tecnicoId: Long, admin: UsuarioEntity) {
        val antes = estadoPrevio(solicitudId, admin)
        val dto = llamarApi {
            solicitudesApi.asignarResponsable(solicitudId, AsignacionRequest(tecnicoId), admin.id)
        }
        guardarRespuesta(dto, admin)
        _eventos.tryEmit(ReglasNotificacion.asignada(antes))
    }

    /** Cantidad de solicitudes atrasadas (para avisar al administrador al entrar). */
    suspend fun contarAtrasadas(admin: UsuarioEntity): Int {
        exigirAdmin(admin)
        return solicitudDao.observarAtrasadas(ReglasAtraso.limite()).first().size
    }

    // ---------- Acciones del técnico ----------

    suspend fun registrarAvance(solicitudId: Long, texto: String, tecnico: UsuarioEntity) {
        val antes = estadoPrevio(solicitudId, tecnico)
        val dto = llamarApi { solicitudesApi.registrarAvance(solicitudId, TextoRequest(texto.trim()), tecnico.id) }
        guardarRespuesta(dto, tecnico)
        _eventos.tryEmit(ReglasNotificacion.avance(antes))
    }

    /** Acciones más recientes realizadas por el usuario (historial de sus acciones). */
    fun observarActividad(usuario: UsuarioEntity): Flow<List<ActividadReciente>> =
        historialDao.observarActividadDeUsuario(usuario.id, 15)

    /** Cambios de otras personas en las solicitudes del usuario desde [desde] (para avisarle al entrar). */
    suspend fun novedadesDesde(usuario: UsuarioEntity, desde: Long): List<NovedadHistorial> =
        historialDao.novedadesPara(
            usuarioId = usuario.id,
            desde = desde,
            esAdmin = usuario.perfil == Perfil.ADMINISTRADOR,
            clienteId = if (usuario.perfil == Perfil.CLIENTE) usuario.clienteId else null,
            tecnicoId = if (usuario.perfil == Perfil.TECNICO) usuario.id else null
        )

    // ---------- Comentarios ----------

    suspend fun agregarComentario(solicitudId: Long, texto: String, usuario: UsuarioEntity) {
        val antes = estadoPrevio(solicitudId, usuario)
        val dto = llamarApi { solicitudesApi.comentar(solicitudId, TextoRequest(texto.trim()), usuario.id) }
        guardarRespuesta(dto, usuario)
        _eventos.tryEmit(ReglasNotificacion.comentario(antes, usuario.perfil))
    }

    // ---------- Consultas (Room, actualizado desde el servidor al abrir la pantalla) ----------

    /** Qué solicitudes ve cada perfil (el servidor aplica la misma regla al descargarlas). */
    fun observarVisibles(usuario: UsuarioEntity): Flow<List<SolicitudEntity>> =
        when (usuario.perfil) {
            Perfil.CLIENTE -> solicitudDao.observarPorCliente(usuario.clienteId ?: -1)
            Perfil.ADMINISTRADOR -> solicitudDao.filtrar()
            Perfil.TECNICO -> solicitudDao.observarPorResponsable(usuario.id)
        }.onStart { intentarSincronizar(usuario) }

    // ---------- Panel del administrador (todas exigen perfil administrador) ----------

    fun filtrar(
        admin: UsuarioEntity,
        estado: EstadoSolicitud?,
        prioridad: Prioridad?,
        clienteId: Long?,
        responsableId: Long?,
        texto: String?
    ): Flow<List<SolicitudEntity>> {
        exigirAdmin(admin)
        return solicitudDao.filtrar(estado, prioridad, clienteId, responsableId, texto)
    }

    fun observarAtrasadas(admin: UsuarioEntity, limite: Long): Flow<List<SolicitudEntity>> {
        exigirAdmin(admin)
        return solicitudDao.observarAtrasadas(limite)
    }

    /** Al abrir el panel se descargan los datos actualizados del servidor. */
    fun conteoPorEstado(admin: UsuarioEntity) = run {
        exigirAdmin(admin)
        solicitudDao.conteoPorEstado().onStart { intentarSincronizar(admin) }
    }

    fun conteoPorPrioridad(admin: UsuarioEntity) = run { exigirAdmin(admin); solicitudDao.conteoPorPrioridad() }
    fun conteoPorResponsable(admin: UsuarioEntity) = run { exigirAdmin(admin); solicitudDao.conteoPorResponsable() }
    fun observarClientes(admin: UsuarioEntity) = run { exigirAdmin(admin); clienteDao.observarTodos() }
    fun observarTecnicos(): Flow<List<UsuarioEntity>> = usuarioDao.observarPorPerfil(Perfil.TECNICO)

    /** Detalle de una solicitud; emite null si no existe o el usuario no tiene acceso. */
    fun observarDetalle(id: Long, usuario: UsuarioEntity): Flow<SolicitudDetalle?> =
        solicitudDao.observarDetalle(id)
            .map { d -> d?.takeIf { tieneAcceso(it.solicitud, usuario) } }
            .onStart { refrescarSolicitud(id, usuario) }

    fun observarHistorial(id: Long): Flow<List<EventoHistorial>> =
        historialDao.observarDetallado(id)

    suspend fun obtener(id: Long, usuario: UsuarioEntity): SolicitudEntity {
        val s = solicitudDao.obtenerPorId(id)
            ?: llamarApi { solicitudesApi.obtener(id, usuario.id) }.aEntidad()
        check(tieneAcceso(s, usuario)) { "No tienes acceso a esta solicitud" }
        return s
    }

    // ---------- Reglas internas (la validación definitiva la hace el servidor) ----------

    private fun tieneAcceso(s: SolicitudEntity, u: UsuarioEntity): Boolean = when (u.perfil) {
        Perfil.ADMINISTRADOR -> true
        Perfil.CLIENTE -> s.clienteId == u.clienteId
        Perfil.TECNICO -> s.responsableId == u.id
    }

    private fun exigirAdmin(u: UsuarioEntity) =
        check(u.perfil == Perfil.ADMINISTRADOR) { "Acción reservada al administrador" }
}
