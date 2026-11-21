package com.example.gestionmovilsolicitudes.data.remote

import com.example.gestionmovilsolicitudes.model.Categoria
import com.example.gestionmovilsolicitudes.model.EstadoSolicitud
import com.example.gestionmovilsolicitudes.model.Perfil
import com.example.gestionmovilsolicitudes.model.Prioridad

/*
 * Objetos que viajan en JSON entre la app y los microservicios Spring Boot.
 * Los nombres de los campos son los mismos que usan los records del backend.
 */

// ---------- usuarios-service (puerto 8081) ----------

data class LoginRequest(val correo: String, val password: String)

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val perfil: Perfil,
    val clienteId: Long?,
    val empresaAsociada: String?,
    val activo: Boolean,
    val fechaCreacion: Long?,
    val fechaActualizacion: Long?
)

data class ClienteDto(
    val id: Long,
    val nombreCliente: String,
    val nombreEmpresa: String,
    val correo: String,
    val telefono: String,
    val serviciosAsociados: String?
)

// ---------- solicitudes-service (puerto 8082) ----------

data class SolicitudDto(
    val id: Long,
    val codigoSeguimiento: String,
    val clienteId: Long,
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val prioridad: Prioridad?,
    val servicioRelacionado: String?,
    val sitioOActivoRelacionado: String?,
    val evidenciaUri: String?,
    val estado: EstadoSolicitud,
    val responsableId: Long?,
    val comentariosSeguimiento: String?,
    val fechaCreacion: Long,
    val fechaActualizacion: Long,
    val fechaResolucion: Long?,
    val confirmacionCierre: Boolean,
    val motivoCancelacion: String?
)

data class HistorialDto(
    val id: Long,
    val solicitudId: Long,
    val estadoAnterior: EstadoSolicitud?,
    val estadoNuevo: EstadoSolicitud,
    val accionRealizada: String,
    val comentario: String?,
    val perfilQueRealizoAccion: Perfil,
    val usuarioResponsableId: Long,
    val nombreUsuario: String?,
    val fechaHora: Long
)

data class SolicitudRequest(
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val servicioRelacionado: String?,
    val sitioOActivoRelacionado: String?,
    val evidenciaUri: String?
)

data class CambioEstadoRequest(val estado: EstadoSolicitud, val comentario: String?, val accion: String?)

data class PrioridadRequest(val prioridad: Prioridad)

data class AsignacionRequest(val tecnicoId: Long)

data class TextoRequest(val texto: String)

/** Cuerpo de error que devuelven los dos microservicios. */
data class ErrorDto(val estado: Int?, val mensaje: String?)
