package cl.duoc.gestion.solicitudes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud de un cliente. clienteId y responsableId son ids del microservicio de usuarios
 * (cada microservicio tiene su propia base de datos, por eso no hay clave foránea entre ellas).
 */
@Entity
@Table(name = "solicitudes", indexes = {
        @Index(name = "idx_solicitud_cliente", columnList = "clienteId"),
        @Index(name = "idx_solicitud_responsable", columnList = "responsableId")
})
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigoSeguimiento;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Prioridad prioridad;

    @Column(length = 100)
    private String servicioRelacionado;

    @Column(length = 100)
    private String sitioOActivoRelacionado;

    @Column(length = 500)
    private String evidenciaUri;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSolicitud estado = EstadoSolicitud.RECIBIDA;

    private Long responsableId;

    @Column(length = 1000)
    private String comentariosSeguimiento;

    /** Fechas en milisegundos (epoch), igual que en la app. */
    @Column(nullable = false)
    private Long fechaCreacion;

    @Column(nullable = false)
    private Long fechaActualizacion;

    private Long fechaResolucion;

    @Column(nullable = false)
    private boolean confirmacionCierre = false;

    @Column(length = 500)
    private String motivoCancelacion;

    /** Al eliminar la solicitud se elimina su historial. */
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC, id ASC")
    private List<Historial> historial = new ArrayList<>();

    public void tocar() {
        this.fechaActualizacion = System.currentTimeMillis();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
    public String getServicioRelacionado() { return servicioRelacionado; }
    public void setServicioRelacionado(String servicioRelacionado) { this.servicioRelacionado = servicioRelacionado; }
    public String getSitioOActivoRelacionado() { return sitioOActivoRelacionado; }
    public void setSitioOActivoRelacionado(String sitioOActivoRelacionado) { this.sitioOActivoRelacionado = sitioOActivoRelacionado; }
    public String getEvidenciaUri() { return evidenciaUri; }
    public void setEvidenciaUri(String evidenciaUri) { this.evidenciaUri = evidenciaUri; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public Long getResponsableId() { return responsableId; }
    public void setResponsableId(Long responsableId) { this.responsableId = responsableId; }
    public String getComentariosSeguimiento() { return comentariosSeguimiento; }
    public void setComentariosSeguimiento(String comentariosSeguimiento) { this.comentariosSeguimiento = comentariosSeguimiento; }
    public Long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public Long getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Long fechaResolucion) { this.fechaResolucion = fechaResolucion; }
    public boolean isConfirmacionCierre() { return confirmacionCierre; }
    public void setConfirmacionCierre(boolean confirmacionCierre) { this.confirmacionCierre = confirmacionCierre; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public List<Historial> getHistorial() { return historial; }
}
