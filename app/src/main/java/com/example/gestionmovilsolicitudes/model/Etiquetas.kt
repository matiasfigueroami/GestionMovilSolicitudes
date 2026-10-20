package com.example.gestionmovilsolicitudes.model

fun Categoria.etiqueta(): String = when (this) {
    Categoria.DESARROLLO_WEB -> "Desarrollo web"
    Categoria.MANTENIMIENTO_WEB -> "Mantenimiento web"
    Categoria.INCIDENTE_TECNICO -> "Incidente técnico"
    Categoria.HOSTING -> "Hosting"
    Categoria.SOPORTE_CORREO -> "Soporte de correo"
    Categoria.MARKETING_DIGITAL -> "Marketing digital"
    Categoria.SOLICITUD_CAMBIO -> "Solicitud de cambio"
    Categoria.CONSULTA -> "Consulta"
    Categoria.ACCESO_CONFIGURACION -> "Acceso o configuración"
    Categoria.OTRO -> "Otro requerimiento"
}

fun EstadoSolicitud.etiqueta(): String = when (this) {
    EstadoSolicitud.RECIBIDA -> "Recibida"
    EstadoSolicitud.EN_REVISION -> "En revisión"
    EstadoSolicitud.PENDIENTE_INFORMACION -> "Pendiente de información"
    EstadoSolicitud.ASIGNADA -> "Asignada"
    EstadoSolicitud.EN_PROCESO -> "En proceso"
    EstadoSolicitud.RESUELTA -> "Resuelta"
    EstadoSolicitud.CERRADA -> "Cerrada"
    EstadoSolicitud.CANCELADA -> "Cancelada"
}

fun Prioridad.etiqueta(): String = when (this) {
    Prioridad.BAJA -> "Baja"
    Prioridad.MEDIA -> "Media"
    Prioridad.ALTA -> "Alta"
    Prioridad.URGENTE -> "Urgente"
}

fun Perfil.etiqueta(): String = when (this) {
    Perfil.CLIENTE -> "Cliente"
    Perfil.ADMINISTRADOR -> "Administrador"
    Perfil.TECNICO -> "Técnico"
}