package cl.duoc.gestion.solicitudes.controller;

import cl.duoc.gestion.solicitudes.dto.HistorialResponse;
import cl.duoc.gestion.solicitudes.service.SolicitudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Historial de todas las solicitudes que el usuario puede ver (sincronización de la app). */
@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final SolicitudService service;

    public HistorialController(SolicitudService service) {
        this.service = service;
    }

    @GetMapping
    public List<HistorialResponse> historialVisible(@RequestHeader(SolicitudController.USUARIO) Long usuarioId) {
        return service.historialVisible(usuarioId);
    }
}
