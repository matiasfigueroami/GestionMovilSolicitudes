package com.example.gestionmovilsolicitudes.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestionmovilsolicitudes.model.Perfil

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val perfil: Perfil,
    // Solo para perfil CLIENTE: ficha de cliente asociada a esta cuenta.
    val clienteId: Long? = null,
    val empresaAsociada: String? = null,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)
