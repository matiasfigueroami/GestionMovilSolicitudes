package cl.duoc.gestion.usuarios.dto;

import cl.duoc.gestion.usuarios.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar un usuario. En la edición, una contraseña vacía conserva la actual. */
public record UsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String correo,

        @Size(min = 4, max = 60, message = "La contraseña debe tener entre 4 y 60 caracteres")
        String password,

        @NotNull(message = "El perfil es obligatorio")
        Perfil perfil,

        Long clienteId,
        String empresaAsociada,
        Boolean activo
) {
}
