package com.example.gestionmovilsolicitudes.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil

@Entity(
    tableName = "historial",
    foreignKeys = [
        ForeignKey(
            entity = SolicitudEntity::class,
            parentColumns = ["id"],
            childColumns = ["solicitudId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("solicitudId")]
)
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val solicitudId: Long,
    val estadoAnterior: EstadoSolicitud?,
    val estadoNuevo: EstadoSolicitud,
    val accionRealizada: String,
    val comentario: String? = null,
    val perfilQueRealizoAccion: Perfil,
    val usuarioResponsableId: Long,
    val fechaHora: Long = System.currentTimeMillis(),
    val archivoEvidenciaId: Long? = null
)