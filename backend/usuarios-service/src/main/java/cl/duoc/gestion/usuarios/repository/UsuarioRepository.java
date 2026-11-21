package cl.duoc.gestion.usuarios.repository;

import cl.duoc.gestion.usuarios.model.Perfil;
import cl.duoc.gestion.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCliente_Id(Long clienteId);

    List<Usuario> findByPerfilOrderByNombreAsc(Perfil perfil);

    List<Usuario> findAllByOrderByIdAsc();
}
