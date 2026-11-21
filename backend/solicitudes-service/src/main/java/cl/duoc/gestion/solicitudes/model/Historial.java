package cl.duoc.gestion.solicitudes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Evento de la trazabilidad de una solicitud: quién hizo qué, cuándo y con qué cambio de estado. */
@Entity
@Table(name = "historial")
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSolicitud estadoNuevo;

    @Column(nullable = false, length = 150)
    private String accionRealizada;

    @Column(length = 1000)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfilQueRealizoAccion;

    @Column(nullable = false)
    private Long usuarioResponsableId;

    /** Copia del nombre al momento de la acción (el usuario vive en otro microservicio). */
    @Column(nullable = false, length = 100)
    private String nombreUsuario;

    @Column(nullable = false)
    private Long fechaHora;

    public Historial() {
    }

    public Historial(Solicitud solicitud, EstadoSolicitud estadoAnterior, EstadoSolicitud estadoNuevo,
                     String accionRealizada, String comentario, Perfil perfil,
                     Long usuarioId, String nombreUsuario, Long fechaHora) {
        this.solicitud = solicitud;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.accionRealizada = accionRealizada;
        this.comentario = comentario;
        this.perfilQueRealizoAccion = perfil;
        this.usuarioResponsableId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.fechaHora = fechaHora;
    }

    public Long getId() { return id; }
    public Solicitud getSolicitud() { return solicitud; }
    public EstadoSolicitud getEstadoAnterior() { return estadoAnterior; }
    public EstadoSolicitud getEstadoNuevo() { return estadoNuevo; }
    public String getAccionRealizada() { return accionRealizada; }
    public String getComentario() { return comentario; }
    public Perfil getPerfilQueRealizoAccion() { return perfilQueRealizoAccion; }
    public Long getUsuarioResponsableId() { return usuarioResponsableId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public Long getFechaHora() { return fechaHora; }
}
