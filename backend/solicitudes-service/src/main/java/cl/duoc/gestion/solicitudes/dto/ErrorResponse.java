package cl.duoc.gestion.solicitudes.dto;

/** Formato único de error: la app muestra [mensaje] tal cual. */
public record ErrorResponse(int estado, String mensaje, long timestamp) {
    public static ErrorResponse de(int estado, String mensaje) {
        return new ErrorResponse(estado, mensaje, System.currentTimeMillis());
    }
}
