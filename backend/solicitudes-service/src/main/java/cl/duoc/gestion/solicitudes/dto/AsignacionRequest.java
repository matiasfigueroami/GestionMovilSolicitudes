package cl.duoc.gestion.solicitudes.dto;

import jakarta.validation.constraints.NotNull;

public record AsignacionRequest(
        @NotNull(message = "Indica el técnico responsable")
        Long tecnicoId
) {
}
