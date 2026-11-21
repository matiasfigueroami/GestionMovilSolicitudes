package com.example.gestionmovilsolicitudes.data.remote

import com.example.gestionmovilsolicitudes.data.entity.ClienteEntity
import com.example.gestionmovilsolicitudes.data.entity.HistorialEntity
import com.example.gestionmovilsolicitudes.data.entity.SolicitudEntity
import com.example.gestionmovilsolicitudes.data.entity.UsuarioEntity

/* Conversión de lo que llega del servidor a las entidades de Room (caché local). */

fun UsuarioDto.aEntidad(): UsuarioEntity {
    val ahora = System.currentTimeMillis()
    return UsuarioEntity(
        id = id,
        nombre = nombre,
        correo = correo,
        password = "", // la contraseña nunca sale del servidor
        perfil = perfil,
        clienteId = clienteId,
        empresaAsociada = empresaAsociada,
        activo = activo,
        fechaCreacion = fechaCreacion ?: ahora,
        fechaActualizacion = fechaActualizacion ?: ahora
    )
}

fun ClienteDto.aEntidad() = ClienteEntity(
    id = id,
    nombreCliente = nombreCliente,
    nombreEmpresa = nombreEmpresa,
    correo = correo,
    telefono = telefono,
    serviciosAsociados = serviciosAsociados
)

fun SolicitudDto.aEntidad() = SolicitudEntity(
    id = id,
    codigoSeguimiento = codigoSeguimiento,
    clienteId = clienteId,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria,
    prioridad = prioridad,
    servicioRelacionado = servicioRelacionado,
    sitioOActivoRelacionado = sitioOActivoRelacionado,
    evidenciaUri = evidenciaUri,
    estado = estado,
    responsableId = responsableId,
    comentariosSeguimiento = comentariosSeguimiento,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    fechaResolucion = fechaResolucion,
    confirmacionCierre = confirmacionCierre,
    motivoCancelacion = motivoCancelacion
)

fun HistorialDto.aEntidad() = HistorialEntity(
    id = id,
    solicitudId = solicitudId,
    estadoAnterior = estadoAnterior,
    estadoNuevo = estadoNuevo,
    accionRealizada = accionRealizada,
    comentario = comentario,
    perfilQueRealizoAccion = perfilQueRealizoAccion,
    usuarioResponsableId = usuarioResponsableId,
    fechaHora = fechaHora
)
