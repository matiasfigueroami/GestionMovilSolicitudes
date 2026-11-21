package cl.duoc.gestion.solicitudes.dto;

import cl.duoc.gestion.solicitudes.model.Perfil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Usuario tal como lo entrega el microservicio de usuarios (GET /api/usuarios/{id}). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioRemoto(
        Long id,
        String nombre,
        String correo,
        Perfil perfil,
        Long clienteId,
        boolean activo
) {
}
