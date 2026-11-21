package cl.duoc.gestion.solicitudes.dto;

import cl.duoc.gestion.solicitudes.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar una solicitud. */
public record SolicitudRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 80, message = "El título admite hasta 80 caracteres")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
        String descripcion,

        @NotNull(message = "Selecciona una categoría")
        Categoria categoria,

        @Size(max = 100, message = "El servicio admite hasta 100 caracteres")
        String servicioRelacionado,

        @Size(max = 100, message = "El sitio o activo admite hasta 100 caracteres")
        String sitioOActivoRelacionado,

        @Size(max = 500)
        String evidenciaUri
) {
}
