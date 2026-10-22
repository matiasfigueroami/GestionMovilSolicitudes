package com.example.gestionmovilsolicitudes.domain

/** Reglas de validación del formulario de inicio de sesión (sin dependencias de Android). */
object ValidadorLogin {

    const val PASSWORD_MIN = 4

    private val FORMATO_CORREO = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")

    data class Resultado(val correo: String?, val password: String?) {
        val valido: Boolean get() = correo == null && password == null
    }

    fun validar(correo: String, password: String): Resultado = Resultado(
        correo = when {
            correo.isBlank() -> "Ingresa tu correo"
            !FORMATO_CORREO.matches(correo.trim()) -> "Ingresa un correo válido (ej: nombre@dominio.cl)"
            else -> null
        },
        password = when {
            password.isBlank() -> "Ingresa tu contraseña"
            password.length < PASSWORD_MIN -> "Debe tener al menos $PASSWORD_MIN caracteres"
            else -> null
        }
    )
}