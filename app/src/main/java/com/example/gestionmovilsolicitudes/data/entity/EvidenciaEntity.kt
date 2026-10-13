package com.example.gestionmovilsolicitudes.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidencias",
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
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val solicitudId: Long,
    val nombreArchivo: String,
    val tipo: String,
    val tamanoBytes: Long,
    val fechaIncorporacion: Long = System.currentTimeMillis(),
    val adjuntadoPorUsuarioId: Long,
    val descripcion: String? = null,
    val estadoRevision: String = "Pendiente"
)
