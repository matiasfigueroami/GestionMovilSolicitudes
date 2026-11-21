package cl.duoc.gestion.solicitudes.dto;

import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** [accion] es opcional: texto que queda en el historial (por ejemplo "Solución registrada"). */
public record CambioEstadoRequest(
        @NotNull(message = "Indica el nuevo estado")
        EstadoSolicitud estado,

        @Size(max = 1000, message = "El comentario admite hasta 1000 caracteres")
        String comentario,

        @Size(max = 150)
        String accion
) {
}
