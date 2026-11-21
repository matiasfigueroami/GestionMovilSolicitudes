package cl.duoc.gestion.solicitudes.repository;

import cl.duoc.gestion.solicitudes.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByFechaActualizacionDesc();

    List<Solicitud> findByClienteIdOrderByFechaActualizacionDesc(Long clienteId);

    List<Solicitud> findByResponsableIdOrderByFechaActualizacionDesc(Long responsableId);

    /** Último código del año (SOL-AAAA-NNNN) para generar el siguiente. */
    Optional<Solicitud> findTopByCodigoSeguimientoStartingWithOrderByCodigoSeguimientoDesc(String prefijo);
}
