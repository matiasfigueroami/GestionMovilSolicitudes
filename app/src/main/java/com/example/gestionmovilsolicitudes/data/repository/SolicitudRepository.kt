package com.example.gestionmovilsolicitudes.data.repository

import androidx.room.withTransaction
import com.example.gestionmovilsolicitudes.data.AppDatabase
import com.example.gestionmovilsolicitudes.data.entity.EvidenciaEntity
import com.example.gestionmovilsolicitudes.data.entity.HistorialEntity
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import com.example.gestionmovilsolicitudes.domain.ReglasEstado
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad
import java.util.Calendar

class SolicitudRepository(private val db: AppDatabase) {

    private val solicitudDao = db.solicitudDao()
    private val historialDao = db.historialDao()
    private val evidenciaDao = db.evidenciaDao()

    // ---------- Crear ----------

    suspend fun crear(
        usuario: UsuarioEntity,
        titulo: String,
        descripcion: String,
        categoria: Categoria,
        servicio: String? = null,
        sitio: String? = null
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
                sitioOActivoRelacionado = sitio
            )
        )
        registrarHistorial(id, null, EstadoSolicitud.RECIBIDA, "Solicitud creada", null, usuario)
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
        comentario: String? = null
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
        registrarHistorial(s.id, s.estado, nuevo, "Cambio de estado a $nuevo", comentario, usuario)
    }

    // ---------- Acciones del administrador ----------

    suspend fun definirPrioridad(solicitudId: Long, prioridad: Prioridad, admin: UsuarioEntity) =
        db.withTransaction {
            exigirAdmin(admin)
            val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
            solicitudDao.actualizar(s.copy(prioridad = prioridad, fechaActualizacion = System.currentTimeMillis()))
            registrarHistorial(s.id, s.estado, s.estado, "Prioridad definida: $prioridad", null, admin)
        }

    suspend fun asignarResponsable(solicitudId: Long, tecnicoId: Long, admin: UsuarioEntity) =
        db.withTransaction {
            exigirAdmin(admin)
            val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
            val tecnico = db.usuarioDao().obtenerPorId(tecnicoId)
            check(tecnico != null && tecnico.perfil == Perfil.TECNICO) { "Responsable inválido" }

            // Primera asignación: pasa a ASIGNADA. Reasignación: conserva el estado.
            val nuevoEstado = if (s.estado == EstadoSolicitud.EN_REVISION) EstadoSolicitud.ASIGNADA else s.estado
            solicitudDao.actualizar(
                s.copy(responsableId = tecnicoId, estado = nuevoEstado, fechaActualizacion = System.currentTimeMillis())
            )
            registrarHistorial(s.id, s.estado, nuevoEstado, "Asignada a ${tecnico.nombre}", null, admin)
        }

    // ---------- Comentarios y evidencias ----------

    suspend fun agregarComentario(solicitudId: Long, texto: String, usuario: UsuarioEntity) =
        db.withTransaction {
            require(texto.isNotBlank()) { "El comentario no puede estar vacío" }
            val s = solicitudDao.obtenerPorId(solicitudId) ?: error("La solicitud no existe")
            verificarAcceso(s, usuario)
            solicitudDao.actualizar(s.copy(fechaActualizacion = System.currentTimeMillis()))
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

    // ---------- Reglas internas ----------

    /** Cliente: solo lo suyo. Técnico: solo lo asignado. Admin: todo. */
    private fun verificarAcceso(s: SolicitudEntity, u: UsuarioEntity) {
        val ok = when (u.perfil) {
            Perfil.ADMINISTRADOR -> true
            Perfil.CLIENTE -> s.clienteId == u.clienteId
            Perfil.TECNICO -> s.responsableId == u.id
        }
        check(ok) { "No tienes acceso a esta solicitud" }
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