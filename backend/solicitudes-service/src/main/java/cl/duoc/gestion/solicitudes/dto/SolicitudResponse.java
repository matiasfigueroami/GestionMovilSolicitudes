package cl.duoc.gestion.solicitudes.dto;

import cl.duoc.gestion.solicitudes.model.Categoria;
import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import cl.duoc.gestion.solicitudes.model.Prioridad;
import cl.duoc.gestion.solicitudes.model.Solicitud;

/** Solicitud tal como la recibe la app (mismos nombres de campo que SolicitudEntity de Room). */
public record SolicitudResponse(
        Long id,
        String codigoSeguimiento,
        Long clienteId,
        String titulo,
        String descripcion,
        Categoria categoria,
        Prioridad prioridad,
        String servicioRelacionado,
        String sitioOActivoRelacionado,
        String evidenciaUri,
        EstadoSolicitud estado,
        Long responsableId,
        String comentariosSeguimiento,
        Long fechaCreacion,
        Long fechaActualizacion,
        Long fechaResolucion,
        boolean confirmacionCierre,
        String motivoCancelacion
) {
    public static SolicitudResponse desde(Solicitud s) {
        return new SolicitudResponse(
                s.getId(), s.getCodigoSeguimiento(), s.getClienteId(), s.getTitulo(), s.getDescripcion(),
                s.getCategoria(), s.getPrioridad(), s.getServicioRelacionado(), s.getSitioOActivoRelacionado(),
                s.getEvidenciaUri(), s.getEstado(), s.getResponsableId(), s.getComentariosSeguimiento(),
                s.getFechaCreacion(), s.getFechaActualizacion(), s.getFechaResolucion(),
                s.isConfirmacionCierre(), s.getMotivoCancelacion()
        );
    }
}
