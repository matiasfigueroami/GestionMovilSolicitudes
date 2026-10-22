package com.example.gestionmovilsolicitudes.domain

/** Validación de los textos libres que pide la app (motivos, respuestas, avances, soluciones). */
object ValidadorTexto {

    const val MINIMO = 3

    /** Devuelve el mensaje de error, o null si el texto es válido. */
    fun validar(texto: String): String? = when {
        texto.isBlank() -> "Este campo es obligatorio"
        texto.trim().length < MINIMO -> "Escribe al menos $MINIMO caracteres"
        else -> null
    }
}