package cl.duoc.gestion.solicitudes.service;

import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import cl.duoc.gestion.solicitudes.model.Perfil;

import java.util.Map;
import java.util.Set;

import static cl.duoc.gestion.solicitudes.model.EstadoSolicitud.*;

/** Transiciones de estado que puede hacer cada perfil (mismas reglas que la app). */
public final class ReglasEstado {

    private ReglasEstado() {
    }

    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> ADMIN = Map.of(
            RECIBIDA, Set.of(EN_REVISION, ASIGNADA, CANCELADA),
            EN_REVISION, Set.of(PENDIENTE_INFORMACION, ASIGNADA, CANCELADA),
            PENDIENTE_INFORMACION, Set.of(EN_REVISION, CANCELADA),
            ASIGNADA, Set.of(EN_PROCESO, PENDIENTE_INFORMACION, CANCELADA),
            EN_PROCESO, Set.of(PENDIENTE_INFORMACION, RESUELTA, CANCELADA),
            RESUELTA, Set.of(CERRADA, EN_PROCESO)
    );

    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TECNICO = Map.of(
            ASIGNADA, Set.of(EN_PROCESO),
            EN_PROCESO, Set.of(PENDIENTE_INFORMACION, RESUELTA)
    );

    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> CLIENTE = Map.of(
            PENDIENTE_INFORMACION, Set.of(EN_REVISION), // respondió la información pedida
            RESUELTA, Set.of(CERRADA, EN_PROCESO),      // confirma o reabre
            CERRADA, Set.of(EN_PROCESO)                 // reapertura
    );

    public static Set<EstadoSolicitud> permitidos(Perfil perfil, EstadoSolicitud desde) {
        Map<EstadoSolicitud, Set<EstadoSolicitud>> tabla = switch (perfil) {
            case ADMINISTRADOR -> ADMIN;
            case TECNICO -> TECNICO;
            case CLIENTE -> CLIENTE;
        };
        return tabla.getOrDefault(desde, Set.of());
    }

    public static boolean puede(Perfil perfil, EstadoSolicitud desde, EstadoSolicitud hacia) {
        return permitidos(perfil, desde).contains(hacia);
    }
}
