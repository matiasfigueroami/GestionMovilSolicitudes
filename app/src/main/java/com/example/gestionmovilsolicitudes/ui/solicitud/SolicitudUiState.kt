package com.example.gestionmovilsolicitudes.ui.solicitud

data class SolicitudUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val servicio: String = "",
    val sitio: String = "",
    val evidenciaUri: String? = null,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,
    val servicioError: String? = null,
    val sitioError: String? = null,

    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,

    // Extras para poder editar
    val editandoId: Long? = null,
    val codigo: String? = null,
    val mensajeError: String? = null
)

