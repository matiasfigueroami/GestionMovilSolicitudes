package cl.duoc.gestion.solicitudes.dto;

import java.util.Map;

/** Indicadores del panel del administrador. Las claves "SIN_..." agrupan las solicitudes sin valor. */
public record EstadisticasResponse(
        long total,
        long atrasadas,
        Map<String, Long> porEstado,
        Map<String, Long> porPrioridad,
        Map<String, Long> porResponsable
) {
}
