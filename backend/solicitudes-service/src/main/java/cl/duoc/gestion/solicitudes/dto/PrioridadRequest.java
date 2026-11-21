package cl.duoc.gestion.solicitudes.dto;

import cl.duoc.gestion.solicitudes.model.Prioridad;
import jakarta.validation.constraints.NotNull;

public record PrioridadRequest(
        @NotNull(message = "Indica la prioridad")
        Prioridad prioridad
) {
}
