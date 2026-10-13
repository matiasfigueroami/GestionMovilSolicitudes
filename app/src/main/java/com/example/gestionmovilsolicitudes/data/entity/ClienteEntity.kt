package com.example.gestionmovilsolicitudes.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreCliente: String,
    val nombreEmpresa: String,
    val correo: String,
    val telefono: String,
    val serviciosAsociados: String? = null
)