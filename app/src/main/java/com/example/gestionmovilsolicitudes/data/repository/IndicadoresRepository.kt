package com.example.gestionmovilsolicitudes.data.repository

import com.example.gestionmovilsolicitudes.data.remote.externa.IndicadorDto
import com.example.gestionmovilsolicitudes.data.remote.externa.IndicadoresApi
import com.example.gestionmovilsolicitudes.data.remote.llamarApi
import java.text.NumberFormat
import java.util.Locale

/** Indicador listo para mostrar en pantalla. */
data class Indicador(
    val codigo: String,
    val nombre: String,
    val valor: String,
    val fecha: String
)

/** Obtiene los indicadores económicos desde la API externa con Retrofit. */
class IndicadoresRepository(private val api: IndicadoresApi) {

    private val pesos = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    suspend fun obtener(): List<Indicador> {
        val r = llamarApi { api.obtenerIndicadores() }
        return listOfNotNull(
            r.uf?.let { aIndicador(it, "UF") },
            r.dolar?.let { aIndicador(it, "Dólar") },
            r.euro?.let { aIndicador(it, "Euro") },
            r.utm?.let { aIndicador(it, "UTM") }
        )
    }

    private fun aIndicador(dto: IndicadorDto, nombreCorto: String) = Indicador(
        codigo = dto.codigo,
        nombre = nombreCorto,
        valor = pesos.format(dto.valor),
        fecha = formatearFecha(dto.fecha)
    )

    /** "2026-11-18T03:00:00.000Z" → "18-11-2026" */
    private fun formatearFecha(iso: String?): String {
        val partes = iso?.take(10)?.split("-") ?: return ""
        return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else iso
    }
}
