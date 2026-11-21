package cl.duoc.gestion.solicitudes.controller;

import cl.duoc.gestion.solicitudes.dto.AsignacionRequest;
import cl.duoc.gestion.solicitudes.dto.CambioEstadoRequest;
import cl.duoc.gestion.solicitudes.dto.EstadisticasResponse;
import cl.duoc.gestion.solicitudes.dto.HistorialResponse;
import cl.duoc.gestion.solicitudes.dto.PrioridadRequest;
import cl.duoc.gestion.solicitudes.dto.SolicitudRequest;
import cl.duoc.gestion.solicitudes.dto.SolicitudResponse;
import cl.duoc.gestion.solicitudes.dto.TextoRequest;
import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import cl.duoc.gestion.solicitudes.model.Prioridad;
import cl.duoc.gestion.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de solicitudes. Todas las peticiones llevan la cabecera X-Usuario-Id con el id
 * del usuario que inició sesión; el servicio decide qué puede ver y hacer según su perfil.
 */
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    public static final String USUARIO = "X-Usuario-Id";

    private final SolicitudService service;

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    // ----- CRUD -----

    @GetMapping
    public List<SolicitudResponse> listar(
            @RequestHeader(USUARIO) Long usuarioId,
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) String texto) {
        return service.listar(usuarioId, estado, prioridad, clienteId, responsableId, texto);
    }

    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable Long id, @RequestHeader(USUARIO) Long usuarioId) {
        return service.obtener(id, usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponse crear(@Valid @RequestBody SolicitudRequest req, @RequestHeader(USUARIO) Long usuarioId) {
        return service.crear(req, usuarioId);
    }

    @PutMapping("/{id}")
    public SolicitudResponse editar(@PathVariable Long id, @Valid @RequestBody SolicitudRequest req,
                                    @RequestHeader(USUARIO) Long usuarioId) {
        return service.editar(id, req, usuarioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @RequestHeader(USUARIO) Long usuarioId) {
        service.eliminar(id, usuarioId);
    }

    // ----- Flujo de trabajo -----

    @PatchMapping("/{id}/estado")
    public SolicitudResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest req,
                                           @RequestHeader(USUARIO) Long usuarioId) {
        return service.cambiarEstado(id, req.estado(), req.comentario(), req.accion(), usuarioId);
    }

    @PatchMapping("/{id}/prioridad")
    public SolicitudResponse definirPrioridad(@PathVariable Long id, @Valid @RequestBody PrioridadRequest req,
                                              @RequestHeader(USUARIO) Long usuarioId) {
        return service.definirPrioridad(id, req.prioridad(), usuarioId);
    }

    @PatchMapping("/{id}/responsable")
    public SolicitudResponse asignar(@PathVariable Long id, @Valid @RequestBody AsignacionRequest req,
                                     @RequestHeader(USUARIO) Long usuarioId) {
        return service.asignarResponsable(id, req.tecnicoId(), usuarioId);
    }

    @PostMapping("/{id}/avances")
    public SolicitudResponse registrarAvance(@PathVariable Long id, @Valid @RequestBody TextoRequest req,
                                             @RequestHeader(USUARIO) Long usuarioId) {
        return service.registrarAvance(id, req.texto(), usuarioId);
    }

    @PostMapping("/{id}/comentarios")
    public SolicitudResponse comentar(@PathVariable Long id, @Valid @RequestBody TextoRequest req,
                                      @RequestHeader(USUARIO) Long usuarioId) {
        return service.agregarComentario(id, req.texto(), usuarioId);
    }

    // ----- Consultas -----

    @GetMapping("/{id}/historial")
    public List<HistorialResponse> historial(@PathVariable Long id, @RequestHeader(USUARIO) Long usuarioId) {
        return service.historial(id, usuarioId);
    }

    @GetMapping("/estadisticas")
    public EstadisticasResponse estadisticas(@RequestHeader(USUARIO) Long usuarioId) {
        return service.estadisticas(usuarioId);
    }
}
