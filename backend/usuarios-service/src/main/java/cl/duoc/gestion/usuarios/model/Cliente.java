package cl.duoc.gestion.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Ficha de la empresa cliente. Una cuenta de perfil CLIENTE apunta a una ficha. */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false, length = 120)
    private String nombreEmpresa;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(length = 255)
    private String serviciosAsociados;

    public Cliente() {
    }

    public Cliente(String nombreCliente, String nombreEmpresa, String correo, String telefono, String serviciosAsociados) {
        this.nombreCliente = nombreCliente;
        this.nombreEmpresa = nombreEmpresa;
        this.correo = correo;
        this.telefono = telefono;
        this.serviciosAsociados = serviciosAsociados;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getServiciosAsociados() { return serviciosAsociados; }
    public void setServiciosAsociados(String serviciosAsociados) { this.serviciosAsociados = serviciosAsociados; }
}
