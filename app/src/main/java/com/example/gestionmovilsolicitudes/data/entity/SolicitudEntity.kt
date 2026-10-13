package com.example.gestionmovilsolicitudes.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Prioridad

@Entity(
    tableName = "solicitudes",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["responsableId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("clienteId"), Index("responsableId")]
)
data class SolicitudEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigoSeguimiento: String,
    val clienteId: Long,
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val prioridad: Prioridad? = null,
    val servicioRelacionado: String? = null,
    val sitioOActivoRelacionado: String? = null,
    val estado: EstadoSolicitud = EstadoSolicitud.RECIBIDA,
    val responsableId: Long? = null,
    val comentariosSeguimiento: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis(),
    val fechaResolucion: Long? = null,
    val confirmacionCierre: Boolean = false,
    val motivoCancelacion: String? = null
)
