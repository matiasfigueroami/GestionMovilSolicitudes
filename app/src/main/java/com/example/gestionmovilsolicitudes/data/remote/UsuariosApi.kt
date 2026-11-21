package com.example.gestionmovilsolicitudes.data.remote

import com.example.gestionmovilsolicitudes.model.Perfil
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** Endpoints del microservicio de usuarios (Spring Boot, puerto 8081). */
interface UsuariosApi {

    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginRequest): UsuarioDto

    @GET("api/usuarios")
    suspend fun usuarios(@Query("perfil") perfil: Perfil? = null): List<UsuarioDto>

    @GET("api/clientes")
    suspend fun clientes(): List<ClienteDto>
}
