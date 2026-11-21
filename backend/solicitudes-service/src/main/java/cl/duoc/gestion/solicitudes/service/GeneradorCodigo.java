package cl.duoc.gestion.solicitudes.service;

/** Códigos de seguimiento con el formato SOL-AAAA-NNNN (correlativo por año). */
public final class GeneradorCodigo {

    private GeneradorCodigo() {
    }

    public static String prefijo(int anio) {
        return "SOL-" + anio + "-";
    }

    /** @param ultimo último código del año, o null si todavía no hay ninguno */
    public static String siguiente(int anio, String ultimo) {
        int numero = 0;
        if (ultimo != null && ultimo.startsWith(prefijo(anio))) {
            try {
                numero = Integer.parseInt(ultimo.substring(prefijo(anio).length()));
            } catch (NumberFormatException ignorado) {
                numero = 0;
            }
        }
        return prefijo(anio) + String.format("%04d", numero + 1);
    }
}
