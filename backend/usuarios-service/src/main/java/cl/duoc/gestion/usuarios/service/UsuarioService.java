package cl.duoc.gestion.usuarios.service;

import cl.duoc.gestion.usuarios.dto.LoginRequest;
import cl.duoc.gestion.usuarios.dto.UsuarioRequest;
import cl.duoc.gestion.usuarios.dto.UsuarioResponse;
import cl.duoc.gestion.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.gestion.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.gestion.usuarios.exception.ReglaNegocioException;
import cl.duoc.gestion.usuarios.model.Perfil;
import cl.duoc.gestion.usuarios.model.Usuario;
import cl.duoc.gestion.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final ClienteService clienteService;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, ClienteService clienteService, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.clienteService = clienteService;
        this.encoder = encoder;
    }

    /** Valida correo y contraseña. Devuelve el usuario sin su contraseña. */
    @Transactional(readOnly = true)
    public UsuarioResponse login(LoginRequest req) {
        Usuario u = usuarios.findByCorreoIgnoreCase(req.correo().trim())
                .filter(Usuario::isActivo)
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        return UsuarioResponse.desde(u);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Perfil perfil) {
        List<Usuario> lista = perfil == null
                ? usuarios.findAllByOrderByIdAsc()
                : usuarios.findByPerfilOrderByNombreAsc(perfil);
        return lista.stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria");
        }
        if (usuarios.existsByCorreoIgnoreCase(req.correo().trim())) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + req.correo());
        }
        Usuario u = new Usuario();
        copiar(req, u);
        return UsuarioResponse.desde(usuarios.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        Usuario u = buscar(id);
        usuarios.findByCorreoIgnoreCase(req.correo().trim())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ReglaNegocioException("Ya existe un usuario con el correo " + req.correo());
                });
        copiar(req, u);
        return UsuarioResponse.desde(usuarios.save(u));
    }

    @Transactional
    public void eliminar(Long id) {
        usuarios.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + id));
    }

    private void copiar(UsuarioRequest req, Usuario u) {
        u.setNombre(req.nombre().trim());
        u.setCorreo(req.correo().trim().toLowerCase());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(encoder.encode(req.password()));
        }
        u.setPerfil(req.perfil());
        if (req.perfil() == Perfil.CLIENTE) {
            if (req.clienteId() == null) {
                throw new ReglaNegocioException("Una cuenta de cliente debe indicar su ficha de cliente (clienteId)");
            }
            u.setCliente(clienteService.buscar(req.clienteId()));
        } else {
            u.setCliente(null);
        }
        u.setEmpresaAsociada(req.empresaAsociada());
        u.setActivo(req.activo() == null || req.activo());
    }
}
