package cl.duoc.gestion.usuarios.dto;

import cl.duoc.gestion.usuarios.model.Perfil;
import cl.duoc.gestion.usuarios.model.Usuario;

/** Usuario tal como lo ve la app (nunca incluye la contraseña). */
public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        Perfil perfil,
        Long clienteId,
        String empresaAsociada,
        boolean activo,
        Long fechaCreacion,
        Long fechaActualizacion
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getPerfil(),
                u.getCliente() != null ? u.getCliente().getId() : null,
                u.getEmpresaAsociada(),
                u.isActivo(),
                u.getFechaCreacion(),
                u.getFechaActualizacion()
        );
    }
}
