package cl.duoc.gestion.usuarios.dto;

import cl.duoc.gestion.usuarios.model.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Se usa tanto para recibir como para devolver una ficha de cliente (el id se ignora al recibir). */
public record ClienteDto(
        Long id,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String nombreCliente,

        @NotBlank(message = "El nombre de la empresa es obligatorio")
        String nombreEmpresa,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,

        String serviciosAsociados
) {
    public static ClienteDto desde(Cliente c) {
        return new ClienteDto(c.getId(), c.getNombreCliente(), c.getNombreEmpresa(),
                c.getCorreo(), c.getTelefono(), c.getServiciosAsociados());
    }
}
