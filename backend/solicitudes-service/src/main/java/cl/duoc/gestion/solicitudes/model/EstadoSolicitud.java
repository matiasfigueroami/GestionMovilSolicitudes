package cl.duoc.gestion.solicitudes.model;

public enum EstadoSolicitud {
    RECIBIDA("Recibida"),
    EN_REVISION("En revisión"),
    PENDIENTE_INFORMACION("Pendiente de información"),
    ASIGNADA("Asignada"),
    EN_PROCESO("En proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoSolicitud(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Estados en los que la solicitud ya no se considera pendiente. */
    public boolean esFinalOResuelta() {
        return this == RESUELTA || this == CERRADA || this == CANCELADA;
    }
}
