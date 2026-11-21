package cl.duoc.gestion.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio de usuarios: cuentas, perfiles, clientes e inicio de sesión. Puerto 8081. */
@SpringBootApplication
public class UsuariosServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsuariosServiceApplication.class, args);
    }
}
