package cl.duoc.gestion.solicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Microservicio de solicitudes: CRUD, cambios de estado, prioridad, asignación e historial. Puerto 8082. */
@SpringBootApplication
public class SolicitudesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SolicitudesServiceApplication.class, args);
    }
}
