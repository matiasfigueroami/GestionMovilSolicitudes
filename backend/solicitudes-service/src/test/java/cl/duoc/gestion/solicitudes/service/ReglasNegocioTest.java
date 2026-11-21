package cl.duoc.gestion.solicitudes.service;

import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import cl.duoc.gestion.solicitudes.model.Perfil;
import cl.duoc.gestion.solicitudes.model.Solicitud;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de las reglas puras (no levantan Spring ni base de datos). */
class ReglasNegocioTest {

    @Test
    void elClienteNoPuedeCerrarUnaSolicitudEnProceso() {
        assertFalse(ReglasEstado.puede(Perfil.CLIENTE, EstadoSolicitud.EN_PROCESO, EstadoSolicitud.CERRADA));
    }

    @Test
    void elClienteConfirmaUnaSolicitudResuelta() {
        assertTrue(ReglasEstado.puede(Perfil.CLIENTE, EstadoSolicitud.RESUELTA, EstadoSolicitud.CERRADA));
    }

    @Test
    void elTecnicoSoloIniciaLasAsignadas() {
        assertTrue(ReglasEstado.puede(Perfil.TECNICO, EstadoSolicitud.ASIGNADA, EstadoSolicitud.EN_PROCESO));
        assertFalse(ReglasEstado.puede(Perfil.TECNICO, EstadoSolicitud.RECIBIDA, EstadoSolicitud.EN_PROCESO));
    }

    @Test
    void elCodigoEsCorrelativoPorAnio() {
        assertEquals("SOL-2026-0001", GeneradorCodigo.siguiente(2026, null));
        assertEquals("SOL-2026-0013", GeneradorCodigo.siguiente(2026, "SOL-2026-0012"));
        assertEquals("SOL-2027-0001", GeneradorCodigo.siguiente(2027, "SOL-2026-0099"));
    }

    @Test
    void unaSolicitudSinResolverDeHaceOchoDiasEstaAtrasada() {
        long ahora = System.currentTimeMillis();
        long limite = ahora - 7L * 24 * 60 * 60 * 1000;
        Solicitud s = new Solicitud();
        s.setFechaCreacion(ahora - 8L * 24 * 60 * 60 * 1000);
        s.setEstado(EstadoSolicitud.EN_PROCESO);
        assertTrue(SolicitudService.esAtrasada(s, limite));

        s.setEstado(EstadoSolicitud.RESUELTA);
        assertFalse(SolicitudService.esAtrasada(s, limite));
    }
}
