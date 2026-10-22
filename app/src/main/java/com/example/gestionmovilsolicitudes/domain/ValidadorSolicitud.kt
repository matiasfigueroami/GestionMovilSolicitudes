package com.example.gestionmovilsolicitudes.domain

import com.example.gestionmovilsolicitudes.model.Categoria

object ValidadorSolicitud {

    const val TITULO_MAX = 80
    const val DESCRIPCION_MIN = 10
    const val SITIO_MAX = 100

    // Categorías en las que el servicio relacionado es obligatorio
    private val conServicio = setOf(
        Categoria.DESARROLLO_WEB, Categoria.MANTENIMIENTO_WEB, Categoria.HOSTING,
        Categoria.SOPORTE_CORREO, Categoria.MARKETING_DIGITAL
    )

    data class Resultado(
        val titulo: String?,
        val descripcion: String?,
        val categoria: String?,
        val servicio: String?,
        val sitio: String? = null
    ) {
        val valido: Boolean
            get() = titulo == null && descripcion == null && categoria == null && servicio == null && sitio == null
    }

    fun validar(
        titulo: String,
        descripcion: String,
        categoria: String,
        servicio: String,
        sitio: String = ""
    ): Resultado {
        val cat = Categoria.entries.firstOrNull { it.name == categoria }
        return Resultado(
            titulo = when {
                titulo.isBlank() -> "El título es obligatorio"
                titulo.trim().length > TITULO_MAX -> "Máximo $TITULO_MAX caracteres"
                else -> null
            },
            descripcion = when {
                descripcion.isBlank() -> "La descripción es obligatoria"
                descripcion.trim().length < DESCRIPCION_MIN -> "Debe tener al menos $DESCRIPCION_MIN caracteres"
                else -> null
            },
            categoria = if (cat == null) "Selecciona una categoría" else null,
            servicio = if (cat != null && cat in conServicio && servicio.isBlank())
                "Indica el servicio relacionado" else null,
            sitio = if (sitio.trim().length > SITIO_MAX) "Máximo $SITIO_MAX caracteres" else null
        )
    }
}