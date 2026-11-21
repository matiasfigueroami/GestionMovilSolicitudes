package cl.duoc.gestion.usuarios.repository;

import cl.duoc.gestion.usuarios.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findAllByOrderByNombreClienteAsc();
}
