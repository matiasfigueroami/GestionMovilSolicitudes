package com.example.gestionmovilsolicitudes.data.remote.externa

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

/**
 * API externa pública mindicador.cl: indicadores económicos diarios de Chile (UF, dólar, euro, UTM).
 * La empresa cotiza la renovación de hosting y los servicios en UF y dólares, por eso se muestran en el menú.
 * Documentación: https://mindicador.cl
 */
interface IndicadoresApi {
    @GET("api")
    suspend fun obtenerIndicadores(): IndicadoresResponse
}

data class IndicadoresResponse(
    val fecha: String?,
    val uf: IndicadorDto?,
    val dolar: IndicadorDto?,
    val euro: IndicadorDto?,
    val utm: IndicadorDto?
)

data class IndicadorDto(
    val codigo: String,
    val nombre: String,
    @SerializedName("unidad_medida") val unidadMedida: String?,
    val fecha: String?,
    val valor: Double
)
