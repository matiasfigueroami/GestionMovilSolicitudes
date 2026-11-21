package cl.duoc.gestion.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de comentarios y avances. */
public record TextoRequest(
        @NotBlank(message = "El texto no puede estar vacío")
        @Size(max = 1000, message = "El texto admite hasta 1000 caracteres")
        String texto
) {
}
