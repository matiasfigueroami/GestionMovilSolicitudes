package cl.duoc.gestion.usuarios.config;

import cl.duoc.gestion.usuarios.model.Cliente;
import cl.duoc.gestion.usuarios.model.Perfil;
import cl.duoc.gestion.usuarios.model.Usuario;
import cl.duoc.gestion.usuarios.repository.ClienteRepository;
import cl.duoc.gestion.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Datos 100% ficticios para la demo (solo si la base está vacía). Contraseña de todas las cuentas: 1234.
 * Ids resultantes: admin=1, tecnico1=2, tecnico2=3, cliente1=4, cliente2=5; clientes 1 y 2.
 * El microservicio de solicitudes usa estos mismos ids en sus datos de ejemplo.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepository usuarios, ClienteRepository clientes, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) return;

        Cliente cli1 = clientes.save(new Cliente("Cliente Uno", "Empresa Demo Uno",
                "cliente1@demo.cl", "+56 9 0000 0001", "Sitio web, Hosting"));
        Cliente cli2 = clientes.save(new Cliente("Cliente Dos", "Empresa Demo Dos",
                "cliente2@demo.cl", "+56 9 0000 0002", "Correo corporativo"));

        usuarios.save(usuario("Admin Demo", "admin@demo.cl", Perfil.ADMINISTRADOR, null, null));
        usuarios.save(usuario("Tecnico Uno", "tecnico1@demo.cl", Perfil.TECNICO, null, null));
        usuarios.save(usuario("Tecnico Dos", "tecnico2@demo.cl", Perfil.TECNICO, null, null));
        usuarios.save(usuario("Cliente Uno", "cliente1@demo.cl", Perfil.CLIENTE, cli1, "Empresa Demo Uno"));
        usuarios.save(usuario("Cliente Dos", "cliente2@demo.cl", Perfil.CLIENTE, cli2, "Empresa Demo Dos"));

        log.info("Datos de demostración cargados: 5 usuarios y 2 clientes (contraseña 1234)");
    }

    private Usuario usuario(String nombre, String correo, Perfil perfil, Cliente cliente, String empresa) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode("1234"));
        u.setPerfil(perfil);
        u.setCliente(cliente);
        u.setEmpresaAsociada(empresa);
        return u;
    }
}
