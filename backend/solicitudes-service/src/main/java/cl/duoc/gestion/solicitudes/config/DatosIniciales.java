package cl.duoc.gestion.solicitudes.config;

import cl.duoc.gestion.solicitudes.model.Categoria;
import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import cl.duoc.gestion.solicitudes.model.Historial;
import cl.duoc.gestion.solicitudes.model.Perfil;
import cl.duoc.gestion.solicitudes.model.Prioridad;
import cl.duoc.gestion.solicitudes.model.Solicitud;
import cl.duoc.gestion.solicitudes.repository.SolicitudRepository;
import cl.duoc.gestion.solicitudes.service.GeneradorCodigo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Solicitudes ficticias de ejemplo (solo si la base está vacía). Usan los ids que crea
 * el microservicio de usuarios: admin=1, tecnico1=2, cliente1=4 (cliente 1), cliente2=5 (cliente 2).
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    private static final long DIA = 24L * 60 * 60 * 1000;

    private static final long ADMIN = 1, TECNICO1 = 2, U_CLIENTE1 = 4, U_CLIENTE2 = 5;
    private static final long CLIENTE1 = 1, CLIENTE2 = 2;

    private final SolicitudRepository solicitudes;

    public DatosIniciales(SolicitudRepository solicitudes) {
        this.solicitudes = solicitudes;
    }

    @Override
    public void run(String... args) {
        if (solicitudes.count() > 0) return;
        int anio = Year.now().getValue();

        crear(anio, 1, CLIENTE1, U_CLIENTE1, "Cliente Uno", "Cambiar banner de portada",
                "Actualizar la imagen principal del sitio web.", Categoria.MANTENIMIENTO_WEB, "Sitio web",
                EstadoSolicitud.RECIBIDA, null, null, 1);
        crear(anio, 2, CLIENTE1, U_CLIENTE1, "Cliente Uno", "Sitio caído",
                "El sitio no carga desde la mañana.", Categoria.INCIDENTE_TECNICO, null,
                EstadoSolicitud.EN_PROCESO, Prioridad.URGENTE, TECNICO1, 8);
        crear(anio, 3, CLIENTE2, U_CLIENTE2, "Cliente Dos", "Crear casilla de correo",
                "Necesito una casilla nueva para ventas.", Categoria.SOPORTE_CORREO, "Correo corporativo",
                EstadoSolicitud.PENDIENTE_INFORMACION, Prioridad.MEDIA, null, 3);
        crear(anio, 4, CLIENTE2, U_CLIENTE2, "Cliente Dos", "Consulta por renovación de hosting",
                "¿Cuándo vence mi plan de hosting?", Categoria.HOSTING, "Hosting",
                EstadoSolicitud.RESUELTA, Prioridad.BAJA, TECNICO1, 5);

        log.info("Datos de demostración cargados: 4 solicitudes de ejemplo");
    }

    private void crear(int anio, int numero, long clienteId, long creadorId, String creadorNombre,
                       String titulo, String descripcion, Categoria categoria, String servicio,
                       EstadoSolicitud estado, Prioridad prioridad, Long responsableId, int haceDias) {
        long ahora = System.currentTimeMillis();
        long creada = ahora - haceDias * DIA;

        Solicitud s = new Solicitud();
        s.setCodigoSeguimiento(GeneradorCodigo.prefijo(anio) + String.format("%04d", numero));
        s.setClienteId(clienteId);
        s.setTitulo(titulo);
        s.setDescripcion(descripcion);
        s.setCategoria(categoria);
        s.setServicioRelacionado(servicio);
        s.setEstado(estado);
        s.setPrioridad(prioridad);
        s.setResponsableId(responsableId);
        s.setFechaCreacion(creada);
        s.setFechaActualizacion(ahora);
        if (estado == EstadoSolicitud.RESUELTA) s.setFechaResolucion(ahora);

        s.getHistorial().add(new Historial(s, null, EstadoSolicitud.RECIBIDA, "Solicitud creada", null,
                Perfil.CLIENTE, creadorId, creadorNombre, creada));
        if (estado != EstadoSolicitud.RECIBIDA) {
            s.getHistorial().add(new Historial(s, EstadoSolicitud.RECIBIDA, estado,
                    "Cambio de estado (datos de ejemplo)", null, Perfil.ADMINISTRADOR, ADMIN, "Admin Demo", ahora));
        }
        solicitudes.save(s);
    }
}
