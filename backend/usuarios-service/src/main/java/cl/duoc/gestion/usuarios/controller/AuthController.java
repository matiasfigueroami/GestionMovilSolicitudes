package cl.duoc.gestion.usuarios.controller;

import cl.duoc.gestion.usuarios.dto.LoginRequest;
import cl.duoc.gestion.usuarios.dto.UsuarioResponse;
import cl.duoc.gestion.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    /** POST /api/auth/login  → 200 con el usuario, o 401 si las credenciales no coinciden. */
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }
}
