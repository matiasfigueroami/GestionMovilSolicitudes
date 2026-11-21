package cl.duoc.gestion.solicitudes.service;

import cl.duoc.gestion.solicitudes.client.UsuariosClient;
import cl.duoc.gestion.solicitudes.dto.EstadisticasResponse;
import cl.duoc.gestion.solicitudes.dto.HistorialResponse;
import cl.duoc.gestion.solicitudes.dto.SolicitudRequest;
import cl.duoc.gestion.solicitudes.dto.SolicitudResponse;
import cl.duoc.gestion.solicitudes.dto.UsuarioRemoto;
import cl.duoc.gestion.solicitudes.exception.AccesoDenegadoException;
import cl.duoc.gestion.solicitudes.exception.RecursoNoEncontradoException;
import cl.duoc.gestion.solicitudes.exception.ReglaNegocioException;
import cl.duoc.gestion.solicitudes.model.Categoria;
import cl.duoc.gestion.solicitudes.model.EstadoSolicitud;
import cl.duoc.gestion.solicitudes.model.Historial;
import cl.duoc.gestion.solicitudes.model.Perfil;
import cl.duoc.gestion.solicitudes.model.Prioridad;
import cl.duoc.gestion.solicitudes.model.Solicitud;
import cl.duoc.gestion.solicitudes.repository.HistorialRepository;
import cl.duoc.gestion.solicitudes.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reglas de negocio de las solicitudes. Cada operación recibe el id del usuario que la pide
 * (cabecera X-Usuario-Id) y lo valida contra el microservicio de usuarios.
 */
@Service
public class SolicitudService {

    /** Una solicitud está atrasada si sigue sin resolverse a los 7 días de su creación. */
    public static final int DIAS_PARA_ATRASO = 7;
    private static final long DIA_MS = 24L * 60 * 60 * 1000;

    private final SolicitudRepository solicitudes;
    private final HistorialRepository historial;
    private final UsuariosClient usuarios;

    public SolicitudService(SolicitudRepository solicitudes, HistorialRepository historial, UsuariosClient usuarios) {
        this.solicitudes = solicitudes;
        this.historial = historial;
        this.usuarios = usuarios;
    }

    // ---------------------------------------------------------------- Consultas

    /** Solicitudes que ve cada perfil, con filtros opcionales (null = sin filtro). */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(Long usuarioId, EstadoSolicitud estado, Prioridad prioridad,
                                          Long clienteId, Long responsableId, String texto) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        String busqueda = texto == null || texto.isBlank() ? null : texto.trim().toLowerCase(Locale.ROOT);

        return visibles(u).stream()
                .filter(s -> estado == null || s.getEstado() == estado)
                .filter(s -> prioridad == null || s.getPrioridad() == prioridad)
                .filter(s -> clienteId == null || clienteId.equals(s.getClienteId()))
                .filter(s -> responsableId == null || responsableId.equals(s.getResponsableId()))
                .filter(s -> busqueda == null
                        || s.getTitulo().toLowerCase(Locale.ROOT).contains(busqueda)
                        || s.getCodigoSeguimiento().toLowerCase(Locale.ROOT).contains(busqueda))
                .map(SolicitudResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id, Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        Solicitud s = buscar(id);
        verificarAcceso(s, u);
        return SolicitudResponse.desde(s);
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> historial(Long id, Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        verificarAcceso(buscar(id), u);
        return historial.findBySolicitud_IdOrderByFechaHoraAscIdAsc(id).stream()
                .map(HistorialResponse::desde).toList();
    }

    /** Historial de todas las solicitudes visibles para el usuario (la app lo usa al sincronizar). */
    @Transactional(readOnly = true)
    public List<HistorialResponse> historialVisible(Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        List<Long> ids = visibles(u).stream().map(Solicitud::getId).toList();
        if (ids.isEmpty()) return List.of();
        return historial.findBySolicitud_IdInOrderByFechaHoraAscIdAsc(ids).stream()
                .map(HistorialResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public EstadisticasResponse estadisticas(Long usuarioId) {
        exigirAdmin(usuarios.usuarioActual(usuarioId));
        List<Solicitud> todas = solicitudes.findAll();
        long limite = System.currentTimeMillis() - DIAS_PARA_ATRASO * DIA_MS;

        return new EstadisticasResponse(
                todas.size(),
                todas.stream().filter(s -> esAtrasada(s, limite)).count(),
                contar(todas, s -> s.getEstado().name()),
                contar(todas, s -> s.getPrioridad() == null ? "SIN_PRIORIDAD" : s.getPrioridad().name()),
                contar(todas, s -> s.getResponsableId() == null ? "SIN_RESPONSABLE" : s.getResponsableId().toString())
        );
    }

    // ---------------------------------------------------------------- Crear / editar / eliminar

    @Transactional
    public SolicitudResponse crear(SolicitudRequest req, Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        if (u.perfil() != Perfil.CLIENTE || u.clienteId() == null) {
            throw new AccesoDenegadoException("Solo un cliente puede crear solicitudes");
        }

        Solicitud s = new Solicitud();
        s.setCodigoSeguimiento(generarCodigo());
        s.setClienteId(u.clienteId());
        copiarDatos(req, s);
        s.setEstado(EstadoSolicitud.RECIBIDA);
        long ahora = System.currentTimeMillis();
        s.setFechaCreacion(ahora);
        s.setFechaActualizacion(ahora);

        registrar(s, null, EstadoSolicitud.RECIBIDA, "Solicitud creada", null, u);
        return SolicitudResponse.desde(solicitudes.save(s));
    }

    @Transactional
    public SolicitudResponse editar(Long id, SolicitudRequest req, Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        if (u.perfil() == Perfil.TECNICO) {
            throw new AccesoDenegadoException("El técnico no puede editar los datos de la solicitud");
        }
        Solicitud s = buscar(id);
        verificarAcceso(s, u);
        if (s.getEstado() == EstadoSolicitud.CERRADA || s.getEstado() == EstadoSolicitud.CANCELADA) {
            throw new ReglaNegocioException("No se puede editar una solicitud cerrada o cancelada");
        }
        copiarDatos(req, s);
        s.tocar();
        registrar(s, s.getEstado(), s.getEstado(), "Datos de la solicitud editados", null, u);
        return SolicitudResponse.desde(solicitudes.save(s));
    }

    /** Elimina la solicitud junto con su historial. Solo el administrador. */
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        exigirAdmin(usuarios.usuarioActual(usuarioId));
        solicitudes.delete(buscar(id));
    }

    // ---------------------------------------------------------------- Flujo de la solicitud

    @Transactional
    public SolicitudResponse cambiarEstado(Long id, EstadoSolicitud nuevo, String comentario, String accion,
                                           Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        Solicitud s = buscar(id);
        verificarAcceso(s, u);

        EstadoSolicitud anterior = s.getEstado();
        if (!ReglasEstado.puede(u.perfil(), anterior, nuevo)) {
            throw new ReglaNegocioException(
                    "No puedes pasar de " + anterior.etiqueta() + " a " + nuevo.etiqueta());
        }
        String texto = comentario == null || comentario.isBlank() ? null : comentario.trim();
        if (nuevo == EstadoSolicitud.CANCELADA && texto == null) {
            throw new ReglaNegocioException("Debes indicar el motivo de cancelación");
        }

        long ahora = System.currentTimeMillis();
        s.setEstado(nuevo);
        s.setFechaActualizacion(ahora);
        if (nuevo == EstadoSolicitud.RESUELTA) s.setFechaResolucion(ahora);
        s.setConfirmacionCierre(nuevo == EstadoSolicitud.CERRADA && u.perfil() == Perfil.CLIENTE);
        if (nuevo == EstadoSolicitud.CANCELADA) s.setMotivoCancelacion(texto);

        String descripcion = accion == null || accion.isBlank()
                ? "Cambio de estado a " + nuevo.etiqueta()
                : accion.trim();
        registrar(s, anterior, nuevo, descripcion, texto, u);
        return SolicitudResponse.desde(solicitudes.save(s));
    }

    @Transactional
    public SolicitudResponse definirPrioridad(Long id, Prioridad prioridad, Long usuarioId) {
        UsuarioRemoto admin = usuarios.usuarioActual(usuarioId);
        exigirAdmin(admin);
        Solicitud s = buscar(id);
        s.setPrioridad(prioridad);
        s.tocar();
        registrar(s, s.getEstado(), s.getEstado(), "Prioridad definida: " + prioridad.etiqueta(), null, admin);
        return SolicitudResponse.desde(solicitudes.save(s));
    }

    /** Asigna o reasigna el técnico. Si estaba Recibida o En revisión pasa a Asignada. */
    @Transactional
    public SolicitudResponse asignarResponsable(Long id, Long tecnicoId, Long usuarioId) {
        UsuarioRemoto admin = usuarios.usuarioActual(usuarioId);
        exigirAdmin(admin);
        Solicitud s = buscar(id);
        UsuarioRemoto tecnico = usuarios.tecnico(tecnicoId);

        EstadoSolicitud anterior = s.getEstado();
        if (anterior == EstadoSolicitud.CERRADA || anterior == EstadoSolicitud.CANCELADA) {
            throw new ReglaNegocioException("No se puede asignar una solicitud cerrada o cancelada");
        }
        EstadoSolicitud nuevo = (anterior == EstadoSolicitud.RECIBIDA || anterior == EstadoSolicitud.EN_REVISION)
                ? EstadoSolicitud.ASIGNADA
                : anterior;

        s.setResponsableId(tecnico.id());
        s.setEstado(nuevo);
        s.tocar();
        registrar(s, anterior, nuevo, "Asignada a " + tecnico.nombre(), null, admin);
        return SolicitudResponse.desde(solicitudes.save(s));
    }

    @Transactional
    public SolicitudResponse registrarAvance(Long id, String texto, Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        if (u.perfil() != Perfil.TECNICO) {
            throw new AccesoDenegadoException("Solo el técnico responsable puede registrar avances");
        }
        Solicitud s = buscar(id);
        verificarAcceso(s, u);
        if (s.getEstado() != EstadoSolicitud.EN_PROCESO) {
            throw new ReglaNegocioException("Solo se registran avances en solicitudes en proceso");
        }
        s.tocar();
        registrar(s, s.getEstado(), s.getEstado(), "Avance registrado", texto.trim(), u);
        return SolicitudResponse.desde(solicitudes.save(s));
    }

    @Transactional
    public SolicitudResponse agregarComentario(Long id, String texto, Long usuarioId) {
        UsuarioRemoto u = usuarios.usuarioActual(usuarioId);
        Solicitud s = buscar(id);
        verificarAcceso(s, u);
        if (s.getEstado() == EstadoSolicitud.CERRADA || s.getEstado() == EstadoSolicitud.CANCELADA) {
            throw new ReglaNegocioException("No se puede comentar una solicitud cerrada o cancelada");
        }
        s.tocar();
        registrar(s, s.getEstado(), s.getEstado(), "Comentario", texto.trim(), u);
        return SolicitudResponse.desde(solicitudes.save(s));
    }

    // ---------------------------------------------------------------- Reglas internas

    private List<Solicitud> visibles(UsuarioRemoto u) {
        return switch (u.perfil()) {
            case ADMINISTRADOR -> solicitudes.findAllByOrderByFechaActualizacionDesc();
            case TECNICO -> solicitudes.findByResponsableIdOrderByFechaActualizacionDesc(u.id());
            case CLIENTE -> u.clienteId() == null
                    ? List.of()
                    : solicitudes.findByClienteIdOrderByFechaActualizacionDesc(u.clienteId());
        };
    }

    private boolean tieneAcceso(Solicitud s, UsuarioRemoto u) {
        return switch (u.perfil()) {
            case ADMINISTRADOR -> true;
            case CLIENTE -> Objects.equals(s.getClienteId(), u.clienteId());
            case TECNICO -> Objects.equals(s.getResponsableId(), u.id());
        };
    }

    private void verificarAcceso(Solicitud s, UsuarioRemoto u) {
        if (!tieneAcceso(s, u)) throw new AccesoDenegadoException("No tienes acceso a esta solicitud");
    }

    private void exigirAdmin(UsuarioRemoto u) {
        if (u.perfil() != Perfil.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Acción reservada al administrador");
        }
    }

    private Solicitud buscar(Long id) {
        return solicitudes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La solicitud no existe"));
    }

    private String generarCodigo() {
        int anio = Year.now().getValue();
        String ultimo = solicitudes
                .findTopByCodigoSeguimientoStartingWithOrderByCodigoSeguimientoDesc(GeneradorCodigo.prefijo(anio))
                .map(Solicitud::getCodigoSeguimiento)
                .orElse(null);
        return GeneradorCodigo.siguiente(anio, ultimo);
    }

    private void copiarDatos(SolicitudRequest req, Solicitud s) {
        s.setTitulo(req.titulo().trim());
        s.setDescripcion(req.descripcion().trim());
        s.setCategoria(req.categoria());
        s.setServicioRelacionado(vacioANull(req.servicioRelacionado()));
        s.setSitioOActivoRelacionado(vacioANull(req.sitioOActivoRelacionado()));
        s.setEvidenciaUri(vacioANull(req.evidenciaUri()));
        if (requiereServicio(req.categoria()) && s.getServicioRelacionado() == null) {
            throw new ReglaNegocioException("Indica el servicio relacionado");
        }
    }

    /** Categorías en las que el servicio relacionado es obligatorio (igual que ValidadorSolicitud en la app). */
    private static boolean requiereServicio(Categoria c) {
        return switch (c) {
            case DESARROLLO_WEB, MANTENIMIENTO_WEB, HOSTING, SOPORTE_CORREO, MARKETING_DIGITAL -> true;
            default -> false;
        };
    }

    private void registrar(Solicitud s, EstadoSolicitud anterior, EstadoSolicitud nuevo,
                           String accion, String comentario, UsuarioRemoto u) {
        s.getHistorial().add(new Historial(s, anterior, nuevo, accion, comentario,
                u.perfil(), u.id(), u.nombre(), System.currentTimeMillis()));
    }

    static boolean esAtrasada(Solicitud s, long limite) {
        return s.getFechaCreacion() < limite && !s.getEstado().esFinalOResuelta();
    }

    private static Map<String, Long> contar(List<Solicitud> lista, Function<Solicitud, String> clave) {
        return lista.stream()
                .sorted(Comparator.comparing(clave))
                .collect(Collectors.groupingBy(clave, LinkedHashMap::new, Collectors.counting()));
    }

    private static String vacioANull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
