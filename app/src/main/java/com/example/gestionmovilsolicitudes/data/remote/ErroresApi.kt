package com.example.gestionmovilsolicitudes.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/** Error de la API con un mensaje listo para mostrar en pantalla. */
class ApiException(mensaje: String, val codigo: Int? = null) : Exception(mensaje)

/**
 * Ejecuta una llamada a un microservicio y traduce los errores a un [ApiException] legible:
 * - errores HTTP: usa el campo "mensaje" que devuelve el backend
 * - sin conexión: indica que el servidor no responde
 */
suspend fun <T> llamarApi(bloque: suspend () -> T): T =
    try {
        bloque()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        throw ApiException(mensajeDe(e), e.code())
    } catch (e: IOException) {
        throw ApiException("No se pudo conectar con el servidor. Revisa que los microservicios estén encendidos.")
    }

private fun mensajeDe(e: HttpException): String {
    val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    val mensaje = cuerpo?.let { runCatching { ApiClient.gson().fromJson(it, ErrorDto::class.java)?.mensaje }.getOrNull() }
    return mensaje?.takeIf { it.isNotBlank() } ?: when (e.code()) {
        401 -> "Correo o contraseña incorrectos"
        403 -> "No tienes permiso para realizar esta acción"
        404 -> "No se encontró el recurso solicitado"
        503 -> "Un servicio del backend no está disponible"
        else -> "Error del servidor (${e.code()})"
    }
}
