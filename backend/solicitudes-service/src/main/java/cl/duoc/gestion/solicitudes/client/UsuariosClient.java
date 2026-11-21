package cl.duoc.gestion.solicitudes.client;

import cl.duoc.gestion.solicitudes.dto.UsuarioRemoto;
import cl.duoc.gestion.solicitudes.exception.AccesoDenegadoException;
import cl.duoc.gestion.solicitudes.exception.ReglaNegocioException;
import cl.duoc.gestion.solicitudes.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import cl.duoc.gestion.solicitudes.model.Perfil;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Comunicación entre microservicios: consulta al microservicio de usuarios (puerto 8081)
 * quién está haciendo la petición y qué perfil tiene.
 */
@Component
public class UsuariosClient {

    private final RestClient http;

    public UsuariosClient(RestClient.Builder builder, @Value("${servicios.usuarios.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    /** Usuario que realiza la petición. Falla con 403 si no existe o está inactivo. */
    public UsuarioRemoto usuarioActual(Long id) {
        UsuarioRemoto u = buscar(id);
        if (u == null || !u.activo()) {
            throw new AccesoDenegadoException("Usuario no válido o inactivo");
        }
        return u;
    }

    /** Técnico al que se asignará una solicitud. */
    public UsuarioRemoto tecnico(Long id) {
        UsuarioRemoto u = buscar(id);
        if (u == null || u.perfil() != Perfil.TECNICO || !u.activo()) {
            throw new ReglaNegocioException("Responsable inválido: debe ser un técnico activo");
        }
        return u;
    }

    private UsuarioRemoto buscar(Long id) {
        try {
            return http.get()
                    .uri("/api/usuarios/{id}", id)
                    .retrieve()
                    .body(UsuarioRemoto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) return null;
            throw new ServicioNoDisponibleException("Error al consultar el servicio de usuarios");
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("El servicio de usuarios no está disponible (puerto 8081)");
        }
    }
}
