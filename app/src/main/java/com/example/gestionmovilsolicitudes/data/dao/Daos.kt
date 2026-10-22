package com.example.gestionmovilsolicitudes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gestionmovilsolicitudes.data.entity.ClienteEntity
import com.example.gestionmovilsolicitudes.data.entity.EvidenciaEntity
import com.example.gestionmovilsolicitudes.data.entity.HistorialEntity
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad
import kotlinx.coroutines.flow.Flow
import androidx.room.Embedded
data class ConteoEstado(val estado: EstadoSolicitud, val total: Int)
data class ConteoPrioridad(val prioridad: Prioridad?, val total: Int)
data class ConteoResponsable(val responsableId: Long?, val total: Int)
data class SolicitudDetalle(
    @Embedded val solicitud: SolicitudEntity,
    val clienteNombre: String?,
    val responsableNombre: String?
)

/** Acción reciente de un usuario, con el código y título de la solicitud. */
data class ActividadReciente(
    val id: Long,
    val codigo: String,
    val titulo: String,
    val accion: String,
    val comentario: String?,
    val estadoNuevo: EstadoSolicitud,
    val fechaHora: Long
)

/** Cambio hecho por otra persona en una solicitud del usuario, desde su última sesión. */
data class NovedadHistorial(
    val id: Long,
    val solicitudId: Long,
    val codigo: String,
    val titulo: String,
    val accion: String,
    val comentario: String?,
    val fechaHora: Long
)
/** Evento del historial con el nombre del usuario que lo realizó. */
data class EventoHistorial(
    val id: Long,
    val estadoAnterior: EstadoSolicitud?,
    val estadoNuevo: EstadoSolicitud,
    val accionRealizada: String,
    val comentario: String?,
    val perfil: Perfil,
    val nombreUsuario: String,
    val fechaHora: Long
)
@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password AND activo = 1 LIMIT 1")
    suspend fun login(correo: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE perfil = :perfil AND activo = 1 ORDER BY nombre")
    fun observarPorPerfil(perfil: Perfil): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}

@Dao
interface ClienteDao {
    @Insert
    suspend fun insertar(cliente: ClienteEntity): Long

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ClienteEntity?

    @Query("SELECT * FROM clientes ORDER BY nombreCliente")
    fun observarTodos(): Flow<List<ClienteEntity>>
}

@Dao
interface SolicitudDao {
    @Insert
    suspend fun insertar(solicitud: SolicitudEntity): Long

    @Update
    suspend fun actualizar(solicitud: SolicitudEntity)

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    fun observarPorId(id: Long): Flow<SolicitudEntity?>

    @Query("SELECT * FROM solicitudes WHERE id = :id")
    suspend fun obtenerPorId(id: Long): SolicitudEntity?

    @Query("SELECT * FROM solicitudes WHERE codigoSeguimiento = :codigo LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): SolicitudEntity?

    @Query(
        """
    SELECT s.*, c.nombreCliente AS clienteNombre, u.nombre AS responsableNombre
    FROM solicitudes s
    LEFT JOIN clientes c ON c.id = s.clienteId
    LEFT JOIN usuarios u ON u.id = s.responsableId
    WHERE s.id = :id
    """
    )
    fun observarDetalle(id: Long): Flow<SolicitudDetalle?>

    @Query("SELECT COUNT(*) FROM solicitudes")
    suspend fun contar(): Int

    @Query("SELECT * FROM solicitudes WHERE clienteId = :clienteId ORDER BY fechaActualizacion DESC")
    fun observarPorCliente(clienteId: Long): Flow<List<SolicitudEntity>>

    @Query("SELECT * FROM solicitudes WHERE responsableId = :tecnicoId ORDER BY fechaActualizacion DESC")
    fun observarPorResponsable(tecnicoId: Long): Flow<List<SolicitudEntity>>

    /** Filtros opcionales: un parametro null significa "sin filtro". */
    @Query(
        """
        SELECT * FROM solicitudes
        WHERE (:estado IS NULL OR estado = :estado)
          AND (:prioridad IS NULL OR prioridad = :prioridad)
          AND (:clienteId IS NULL OR clienteId = :clienteId)
          AND (:responsableId IS NULL OR responsableId = :responsableId)
          AND (:texto IS NULL OR titulo LIKE '%' || :texto || '%' OR codigoSeguimiento LIKE '%' || :texto || '%')
        ORDER BY fechaActualizacion DESC
        """
    )
    fun filtrar(
        estado: EstadoSolicitud? = null,
        prioridad: Prioridad? = null,
        clienteId: Long? = null,
        responsableId: Long? = null,
        texto: String? = null
    ): Flow<List<SolicitudEntity>>

    /** Atrasadas: sin resolver y creadas antes de [limite] (epoch millis). */
    @Query(
        """
        SELECT * FROM solicitudes
        WHERE fechaCreacion < :limite
          AND estado NOT IN ('RESUELTA', 'CERRADA', 'CANCELADA')
        ORDER BY fechaCreacion
        """
    )
    fun observarAtrasadas(limite: Long): Flow<List<SolicitudEntity>>

    @Query("SELECT estado, COUNT(*) AS total FROM solicitudes GROUP BY estado")
    fun conteoPorEstado(): Flow<List<ConteoEstado>>

    @Query("SELECT prioridad, COUNT(*) AS total FROM solicitudes GROUP BY prioridad")
    fun conteoPorPrioridad(): Flow<List<ConteoPrioridad>>

    @Query("SELECT responsableId, COUNT(*) AS total FROM solicitudes GROUP BY responsableId")
    fun conteoPorResponsable(): Flow<List<ConteoResponsable>>
}

@Dao
interface HistorialDao {
    @Insert
    suspend fun insertar(evento: HistorialEntity): Long

    @Query("SELECT * FROM historial WHERE solicitudId = :solicitudId ORDER BY fechaHora ASC, id ASC")
    fun observarPorSolicitud(solicitudId: Long): Flow<List<HistorialEntity>>

    @Query(
        """
    SELECT h.id AS id, h.estadoAnterior AS estadoAnterior, h.estadoNuevo AS estadoNuevo,
           h.accionRealizada AS accionRealizada, h.comentario AS comentario,
           h.perfilQueRealizoAccion AS perfil, u.nombre AS nombreUsuario, h.fechaHora AS fechaHora
    FROM historial h
    INNER JOIN usuarios u ON u.id = h.usuarioResponsableId
    WHERE h.solicitudId = :solicitudId
    ORDER BY h.fechaHora ASC, h.id ASC
    """
    )
    fun observarDetallado(solicitudId: Long): Flow<List<EventoHistorial>>
    @Query(
        """
    SELECT h.id AS id, s.codigoSeguimiento AS codigo, s.titulo AS titulo,
           h.accionRealizada AS accion, h.comentario AS comentario,
           h.estadoNuevo AS estadoNuevo, h.fechaHora AS fechaHora
    FROM historial h
    INNER JOIN solicitudes s ON s.id = h.solicitudId
    WHERE h.usuarioResponsableId = :usuarioId
    ORDER BY h.fechaHora DESC, h.id DESC
    LIMIT :limite
    """
    )
    fun observarActividadDeUsuario(usuarioId: Long, limite: Int): Flow<List<ActividadReciente>>

    /**
     * Cambios posteriores a [desde] hechos por OTROS usuarios en solicitudes que le conciernen:
     * todas si es administrador, las suyas si es cliente o las asignadas si es técnico.
     */
    @Query(
        """
        SELECT h.id AS id, s.id AS solicitudId, s.codigoSeguimiento AS codigo, s.titulo AS titulo,
               h.accionRealizada AS accion, h.comentario AS comentario, h.fechaHora AS fechaHora
        FROM historial h
        INNER JOIN solicitudes s ON s.id = h.solicitudId
        WHERE h.fechaHora > :desde
          AND h.usuarioResponsableId != :usuarioId
          AND (:esAdmin = 1 OR s.clienteId = :clienteId OR s.responsableId = :tecnicoId)
        ORDER BY h.fechaHora DESC, h.id DESC
        LIMIT 20
        """
    )
    suspend fun novedadesPara(
        usuarioId: Long,
        desde: Long,
        esAdmin: Boolean,
        clienteId: Long?,
        tecnicoId: Long?
    ): List<NovedadHistorial>
}

@Dao
interface EvidenciaDao {
    @Insert
    suspend fun insertar(evidencia: EvidenciaEntity): Long

    @Query("SELECT * FROM evidencias WHERE solicitudId = :solicitudId ORDER BY fechaIncorporacion DESC")
    fun observarPorSolicitud(solicitudId: Long): Flow<List<EvidenciaEntity>>
}
