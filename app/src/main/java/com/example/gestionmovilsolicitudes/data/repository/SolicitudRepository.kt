package com.example.gestionmovilsolicitudes.data.repository

import androidx.room.withTransaction
import com.example.gestionmovilsolicitudes.data.AppDatabase
import com.example.gestionmovilsolicitudes.data.dao.ActividadReciente
import com.example.gestionmovilsolicitudes.data.dao.EventoHistorial
import com.example.gestionmovilsolicitudes.data.dao.NovedadHistorial
import com.example.gestionmovilsolicitudes.data.dao.SolicitudDetalle
import com.example.gestionmovilsolicitudes.data.entity.EvidenciaEntity
import com.example.gestionmovilsolicitudes.data.entity.HistorialEntity
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import com.example.gestionmovilsolicitudes.domain.NotificacionSolicitud
import com.example.gestionmovilsolicitudes.domain.ReglasAtraso
import com.example.gestionmovilsolicitudes.domain.ReglasEstado
import com.example.gestionmovilsolicitudes.domain.ReglasNotificacion
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad
import com.example.gestionmovilsolicitudes.model.etiqueta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class SolicitudRepository(private val db: AppDatabase) {

    private val solicitudDao = db.solicitudDao()
    private val historialDao = db.historialDao()
    private val evidenciaDao = db.evidenciaDao()

    // Avisos que la app convierte en notificaciones locales (el repositorio no conoce Android)
    private val _eventos = MutableSharedFlow<NotificacionSolicitud>(extraBufferCapacity = 32)
    val eventos: SharedFlow<NotificacionSolicitud> = _eventos.asSharedFlow()

    // ---------- Crear ----------
    suspend fun crear(
        usuario: UsuarioEntity,
        titulo: String,
        descripcion: String,
        categoria: Categoria,
        servicio: String? = null,
        sitio: String? = null,
        evidenciaUri: String? = null
    ): Long = db.withTransaction {
        require(usuario.perfil == Perfil.CLIENTE && usuario.clienteId != null) {
            "Solo un cliente puede crear solicitudes"
        }
        require(titulo.isNotBlank()) { "El título es obligatorio" }
        require(descripcion.isNotBlank()) { "La descripción es obligatoria" }

        val id = solicitudDao.insertar(
            SolicitudEntity(
                codigoSeguimiento = generarCodigo(),
                clienteId = usuario.clienteId,
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicioRelacionado = servicio,
                sitioOActivoRelacionado = sitio,
                evidenciaUri = evidenciaUri
            )
        )
        registrarHistorial(id, null, EstadoSolicitud.RECIBIDA, "Solicitud creada", null, usuario)
        solicitudDao.obtenerPorId(id)?.let { _eventos.tryEmit(ReglasNotificacion.nuevaSolicitud(it)) }
        id
    }

    private suspend fun generarCodigo(): String {
        val anio = Calendar.getInstance().get(Calendar.YEAR)
        return "SOL-%d-%04d".format(anio, solicitudDao.contar() + 1)
    }

    // ---------- Cambio de estado ----------

    suspend fun cambiarEstado(
        solicitudId: Long,
        nuevo: EstadoSolicitud,
        usuario: UsuarioEntity,
        comentario: String? = null,
        accion: String? = null
    ) = db.withTransaction {
        val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
        verificarAcceso(s, usuario)

        check(nuevo in ReglasEstado.permitidos(usuario.perfil, s.estado)) {
            "No puedes pasar de ${s.estado} a $nuevo"
        }
        if (nuevo == EstadoSolicitud.CANCELADA) {
            require(!comentario.isNullOrBlank()) { "Debes indicar el motivo de cancelación" }
        }

        val ahora = System.currentTimeMillis()
        solicitudDao.actualizar(
            s.copy(
                estado = nuevo,
                fechaActualizacion = ahora,
                fechaResolucion = if (nuevo == EstadoSolicitud.RESUELTA) ahora else s.fechaResolucion,
                confirmacionCierre = nuevo == EstadoSolicitud.CERRADA && usuario.perfil == Perfil.CLIENTE,
                motivoCancelacion = if (nuevo == EstadoSolicitud.CANCELADA) comentario else s.motivoCancelacion
            )
        )
        ReglasNotificacion.cambioEstado(s, nuevo, usuario.perfil)?.let { _eventos.tryEmit(it) }
        registrarHistorial(s.id, s.estado, nuevo, accion ?: "Cambio de estado a ${nuevo.etiqueta()}", comentario, usuario)
    }

    // ---------- Acciones del administrador ----------

    suspend fun definirPrioridad(solicitudId: Long, prioridad: Prioridad, admin: UsuarioEntity) =
        db.withTransaction {
            exigirAdmin(admin)
            val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
            solicitudDao.actualizar(s.copy(prioridad = prioridad, fechaActualizacion = System.currentTimeMillis()))
            _eventos.tryEmit(ReglasNotificacion.prioridadCambiada(s, prioridad))
            registrarHistorial(s.id, s.estado, s.estado, "Prioridad definida: ${prioridad.etiqueta()}", null, admin)
        }

    suspend fun asignarResponsable(solicitudId: Long, tecnicoId: Long, admin: UsuarioEntity) =
        db.withTransaction {
            exigirAdmin(admin)
            val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
            val tecnico = db.usuarioDao().obtenerPorId(tecnicoId)
            check(tecnico != null && tecnico.perfil == Perfil.TECNICO) { "Responsable inválido" }

            val nuevoEstado = if (s.estado == EstadoSolicitud.RECIBIDA || s.estado == EstadoSolicitud.EN_REVISION)
                EstadoSolicitud.ASIGNADA
            else s.estado
            solicitudDao.actualizar(
                s.copy(responsableId = tecnicoId, estado = nuevoEstado, fechaActualizacion = System.currentTimeMillis())
            )
            _eventos.tryEmit(ReglasNotificacion.asignada(s))
            registrarHistorial(s.id, s.estado, nuevoEstado, "Asignada a ${tecnico.nombre}", null, admin)
        }

    /** Cantidad de solicitudes atrasadas (para avisar al administrador al entrar). */
    suspend fun contarAtrasadas(admin: UsuarioEntity): Int {
        exigirAdmin(admin)
        return solicitudDao.observarAtrasadas(ReglasAtraso.limite()).first().size
    }

    // ---------- Acciones del técnico ----------

    suspend fun registrarAvance(solicitudId: Long, texto: String, tecnico: UsuarioEntity) =
        db.withTransaction {
            check(tecnico.perfil == Perfil.TECNICO) { "Solo el técnico responsable puede registrar avances" }
            require(texto.isNotBlank()) { "Describe el avance realizado" }
            val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
            verificarAcceso(s, tecnico)
            check(s.estado == EstadoSolicitud.EN_PROCESO) { "Solo se registran avances en solicitudes en proceso" }
            solicitudDao.actualizar(s.copy(fechaActualizacion = System.currentTimeMillis()))
            _eventos.tryEmit(ReglasNotificacion.avance(s))
            registrarHistorial(s.id, s.estado, s.estado, "Avance registrado", texto.trim(), tecnico)
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

    // ---------- Comentarios y evidencias ----------

    suspend fun agregarComentario(solicitudId: Long, texto: String, usuario: UsuarioEntity) =
        db.withTransaction {
            require(texto.isNotBlank()) { "El comentario no puede estar vacío" }
            val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
            verificarAcceso(s, usuario)
            solicitudDao.actualizar(s.copy(fechaActualizacion = System.currentTimeMillis()))
            _eventos.tryEmit(ReglasNotificacion.comentario(s, usuario.perfil))
            registrarHistorial(s.id, s.estado, s.estado, "Comentario", texto.trim(), usuario)
        }

    suspend fun adjuntarEvidencia(
        solicitudId: Long, nombre: String, tamanoBytes: Long, descripcion: String?, usuario: UsuarioEntity
    ) = db.withTransaction {
        val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
        verificarAcceso(s, usuario)

        val ext = nombre.substringAfterLast('.', "").lowercase()
        require(ext in FORMATOS_PERMITIDOS) { "Formato no permitido (usa ${FORMATOS_PERMITIDOS.joinToString()})" }
        require(tamanoBytes <= MAX_BYTES) { "El archivo supera los 2 MB" }

        val evidenciaId = evidenciaDao.insertar(
            EvidenciaEntity(
                solicitudId = s.id, nombreArchivo = nombre, tipo = ext, tamanoBytes = tamanoBytes,
                adjuntadoPorUsuarioId = usuario.id, descripcion = descripcion
            )
        )
        registrarHistorial(s.id, s.estado, s.estado, "Evidencia adjuntada: $nombre", null, usuario, evidenciaId)
    }

    // ---------- Consultas / edición ----------

    /** Qué solicitudes ve cada perfil */
    fun observarVisibles(usuario: UsuarioEntity): Flow<List<SolicitudEntity>> =
        when (usuario.perfil) {
            Perfil.CLIENTE -> solicitudDao.observarPorCliente(usuario.clienteId ?: -1)
            Perfil.ADMINISTRADOR -> solicitudDao.filtrar()
            Perfil.TECNICO -> solicitudDao.observarPorResponsable(usuario.id)
        }

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

    fun conteoPorEstado(admin: UsuarioEntity) = run { exigirAdmin(admin); solicitudDao.conteoPorEstado() }
    fun conteoPorPrioridad(admin: UsuarioEntity) = run { exigirAdmin(admin); solicitudDao.conteoPorPrioridad() }
    fun conteoPorResponsable(admin: UsuarioEntity) = run { exigirAdmin(admin); solicitudDao.conteoPorResponsable() }
    fun observarClientes(admin: UsuarioEntity) = run { exigirAdmin(admin); db.clienteDao().observarTodos() }
    fun observarTecnicos(): Flow<List<UsuarioEntity>> =
        db.usuarioDao().observarPorPerfil(Perfil.TECNICO)

    /** Detalle de una solicitud; emite null si no existe o el usuario no tiene acceso. */
    fun observarDetalle(id: Long, usuario: UsuarioEntity): Flow<SolicitudDetalle?> =
        solicitudDao.observarDetalle(id).map { d -> d?.takeIf { tieneAcceso(it.solicitud, usuario) } }

    fun observarHistorial(id: Long): Flow<List<EventoHistorial>> =
        historialDao.observarDetallado(id)

    suspend fun obtener(id: Long, usuario: UsuarioEntity): SolicitudEntity {
        val s = solicitudDao.obtenerPorId(id) ?: error("La solicitud no existe")
        verificarAcceso(s, usuario)
        return s
    }

    suspend fun editar(
        solicitudId: Long,
        usuario: UsuarioEntity,
        titulo: String,
        descripcion: String,
        categoria: Categoria,
        servicio: String?,
        sitio: String?,
        evidenciaUri: String?
    ) = db.withTransaction {
        check(usuario.perfil != Perfil.TECNICO) { "El técnico no puede editar los datos de la solicitud" }
        val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
        verificarAcceso(s, usuario)
        check(s.estado != EstadoSolicitud.CERRADA && s.estado != EstadoSolicitud.CANCELADA) {
            "No se puede editar una solicitud cerrada o cancelada"
        }
        solicitudDao.actualizar(
            s.copy(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                servicioRelacionado = servicio?.trim()?.ifBlank { null },
                sitioOActivoRelacionado = sitio?.trim()?.ifBlank { null },
                evidenciaUri = evidenciaUri,
                fechaActualizacion = System.currentTimeMillis()
            )
        )
        registrarHistorial(s.id, s.estado, s.estado, "Datos de la solicitud editados", null, usuario)
    }

    // ---------- Reglas internas ----------

    private fun tieneAcceso(s: SolicitudEntity, u: UsuarioEntity): Boolean = when (u.perfil) {
        Perfil.ADMINISTRADOR -> true
        Perfil.CLIENTE -> s.clienteId == u.clienteId
        Perfil.TECNICO -> s.responsableId == u.id
    }

    private fun verificarAcceso(s: SolicitudEntity, u: UsuarioEntity) {
        check(tieneAcceso(s, u)) { "No tienes acceso a esta solicitud" }
    }

    private fun exigirAdmin(u: UsuarioEntity) =
        check(u.perfil == Perfil.ADMINISTRADOR) { "Acción reservada al administrador" }

    private suspend fun registrarHistorial(
        solicitudId: Long,
        anterior: EstadoSolicitud?,
        nuevo: EstadoSolicitud,
        accion: String,
        comentario: String?,
        usuario: UsuarioEntity,
        evidenciaId: Long? = null
    ) {
        historialDao.insertar(
            HistorialEntity(
                solicitudId = solicitudId,
                estadoAnterior = anterior,
                estadoNuevo = nuevo,
                accionRealizada = accion,
                comentario = comentario,
                perfilQueRealizoAccion = usuario.perfil,
                usuarioResponsableId = usuario.id,
                archivoEvidenciaId = evidenciaId
            )
        )
    }

    companion object {
        private val FORMATOS_PERMITIDOS = setOf("jpg", "jpeg", "png", "pdf")
        private const val MAX_BYTES = 2L * 1024 * 1024
    }
}