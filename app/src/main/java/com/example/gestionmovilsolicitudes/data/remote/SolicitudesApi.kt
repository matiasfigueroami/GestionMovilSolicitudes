package com.example.gestionmovilsolicitudes.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Endpoints del microservicio de solicitudes (Spring Boot, puerto 8082).
 * Cada petición lleva la cabecera X-Usuario-Id: el servidor decide qué puede ver y hacer ese usuario.
 */
interface SolicitudesApi {

    // ---------- CRUD ----------

    @GET("api/solicitudes")
    suspend fun listar(@Header(USUARIO) usuarioId: Long): List<SolicitudDto>

    @GET("api/solicitudes/{id}")
    suspend fun obtener(@Path("id") id: Long, @Header(USUARIO) usuarioId: Long): SolicitudDto

    @POST("api/solicitudes")
    suspend fun crear(@Body datos: SolicitudRequest, @Header(USUARIO) usuarioId: Long): SolicitudDto

    @PUT("api/solicitudes/{id}")
    suspend fun editar(
        @Path("id") id: Long,
        @Body datos: SolicitudRequest,
        @Header(USUARIO) usuarioId: Long
    ): SolicitudDto

    @DELETE("api/solicitudes/{id}")
    suspend fun eliminar(@Path("id") id: Long, @Header(USUARIO) usuarioId: Long): Response<Unit>

    // ---------- Flujo de trabajo ----------

    @PATCH("api/solicitudes/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body datos: CambioEstadoRequest,
        @Header(USUARIO) usuarioId: Long
    ): SolicitudDto

    @PATCH("api/solicitudes/{id}/prioridad")
    suspend fun definirPrioridad(
        @Path("id") id: Long,
        @Body datos: PrioridadRequest,
        @Header(USUARIO) usuarioId: Long
    ): SolicitudDto

    @PATCH("api/solicitudes/{id}/responsable")
    suspend fun asignarResponsable(
        @Path("id") id: Long,
        @Body datos: AsignacionRequest,
        @Header(USUARIO) usuarioId: Long
    ): SolicitudDto

    @POST("api/solicitudes/{id}/avances")
    suspend fun registrarAvance(
        @Path("id") id: Long,
        @Body datos: TextoRequest,
        @Header(USUARIO) usuarioId: Long
    ): SolicitudDto

    @POST("api/solicitudes/{id}/comentarios")
    suspend fun comentar(
        @Path("id") id: Long,
        @Body datos: TextoRequest,
        @Header(USUARIO) usuarioId: Long
    ): SolicitudDto

    // ---------- Historial ----------

    @GET("api/solicitudes/{id}/historial")
    suspend fun historial(@Path("id") id: Long, @Header(USUARIO) usuarioId: Long): List<HistorialDto>

    /** Historial de todas las solicitudes que el usuario puede ver. */
    @GET("api/historial")
    suspend fun historialVisible(@Header(USUARIO) usuarioId: Long): List<HistorialDto>

    companion object {
        const val USUARIO = "X-Usuario-Id"
    }
}
