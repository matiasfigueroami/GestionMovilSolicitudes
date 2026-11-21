package cl.duoc.gestion.usuarios.service;

import cl.duoc.gestion.usuarios.dto.ClienteDto;
import cl.duoc.gestion.usuarios.exception.RecursoNoEncontradoException;
import cl.duoc.gestion.usuarios.exception.ReglaNegocioException;
import cl.duoc.gestion.usuarios.model.Cliente;
import cl.duoc.gestion.usuarios.repository.ClienteRepository;
import cl.duoc.gestion.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;

    public ClienteService(ClienteRepository clientes, UsuarioRepository usuarios) {
        this.clientes = clientes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<ClienteDto> listar() {
        return clientes.findAllByOrderByNombreClienteAsc().stream().map(ClienteDto::desde).toList();
    }

    @Transactional(readOnly = true)
    public ClienteDto obtener(Long id) {
        return ClienteDto.desde(buscar(id));
    }

    @Transactional
    public ClienteDto crear(ClienteDto dto) {
        Cliente c = new Cliente();
        copiar(dto, c);
        return ClienteDto.desde(clientes.save(c));
    }

    @Transactional
    public ClienteDto actualizar(Long id, ClienteDto dto) {
        Cliente c = buscar(id);
        copiar(dto, c);
        return ClienteDto.desde(clientes.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente c = buscar(id);
        if (usuarios.existsByCliente_Id(id)) {
            throw new ReglaNegocioException("No se puede eliminar: el cliente tiene cuentas de usuario asociadas");
        }
        clientes.delete(c);
    }

    Cliente buscar(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente " + id));
    }

    private void copiar(ClienteDto dto, Cliente c) {
        c.setNombreCliente(dto.nombreCliente().trim());
        c.setNombreEmpresa(dto.nombreEmpresa().trim());
        c.setCorreo(dto.correo().trim().toLowerCase());
        c.setTelefono(dto.telefono().trim());
        c.setServiciosAsociados(dto.serviciosAsociados());
    }
}
