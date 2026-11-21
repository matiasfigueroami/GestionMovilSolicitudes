package cl.duoc.gestion.solicitudes.repository;

import cl.duoc.gestion.solicitudes.model.Historial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HistorialRepository extends JpaRepository<Historial, Long> {

    List<Historial> findBySolicitud_IdOrderByFechaHoraAscIdAsc(Long solicitudId);

    List<Historial> findBySolicitud_IdInOrderByFechaHoraAscIdAsc(Collection<Long> solicitudIds);
}
