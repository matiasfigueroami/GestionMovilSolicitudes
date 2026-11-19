package com.example.gestionmovilsolicitudes.domain

/**
 * Genera el código de seguimiento de una solicitud: SOL-AAAA-NNNN.
 * Usa el mayor número ya existente del año (y no la cantidad de solicitudes),
 * así un código no se repite aunque se haya eliminado alguna solicitud.
 */
object GeneradorCodigo {

    fun prefijo(anio: Int) = "SOL-$anio-"

    fun siguiente(anio: Int, codigosDelAnio: List<String>): String {
        val mayor = codigosDelAnio.mapNotNull { it.substringAfterLast('-').toIntOrNull() }.maxOrNull() ?: 0
        return prefijo(anio) + (mayor + 1).toString().padStart(4, '0')
    }
}
