package com.example.gestionmovilsolicitudes.data.remote

import com.example.gestionmovilsolicitudes.BuildConfig
import com.example.gestionmovilsolicitudes.data.remote.externa.IndicadoresApi
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Crea las instancias de Retrofit: dos microservicios propios y una API externa. */
object ApiClient {

    private val gson = Gson()

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            // En Logcat (filtro "okhttp") se ven las peticiones y respuestas de la API
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    val usuarios: UsuariosApi by lazy { retrofit(BuildConfig.USUARIOS_URL).create(UsuariosApi::class.java) }

    val solicitudes: SolicitudesApi by lazy { retrofit(BuildConfig.SOLICITUDES_URL).create(SolicitudesApi::class.java) }

    val indicadores: IndicadoresApi by lazy { retrofit(BuildConfig.INDICADORES_URL).create(IndicadoresApi::class.java) }

    internal fun gson(): Gson = gson
}
