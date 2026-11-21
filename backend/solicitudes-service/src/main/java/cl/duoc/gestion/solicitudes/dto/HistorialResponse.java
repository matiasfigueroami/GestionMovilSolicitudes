package cl.duoc.gestion.solicitudes.dto;

import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import cl.duoc.gestion.solicitudes.model.Historial;
import cl.duoc.gestion.solicitudes.model.Perfil;

public record HistorialResponse(
        Long id,
        Long solicitudId,
        EstadoSolicitud estadoAnterior,
        EstadoSolicitud estadoNuevo,
        String accionRealizada,
        String comentario,
        Perfil perfilQueRealizoAccion,
        Long usuarioResponsableId,
        String nombreUsuario,
        Long fechaHora
) {
    public static HistorialResponse desde(Historial h) {
        return new HistorialResponse(
                h.getId(), h.getSolicitud().getId(), h.getEstadoAnterior(), h.getEstadoNuevo(),
                h.getAccionRealizada(), h.getComentario(), h.getPerfilQueRealizoAccion(),
                h.getUsuarioResponsableId(), h.getNombreUsuario(), h.getFechaHora()
        );
    }
}
