package pe.edu.pucp.skillbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.pucp.skillbridge.dto.HabilidadColaboradorDTO;
import pe.edu.pucp.skillbridge.entity.*;
import pe.edu.pucp.skillbridge.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/colaborador")
public class ColaboradorController {

    private final UsuarioRepository usuarioRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final HabilidadRepository habilidadRepository;
    private final CertificacionRepository certificacionRepository;
    private final AsignacionRepository asignacionRepository;
    private final ForoRepository foroRepository;
    private final NotificacionRepository notificacionRepository;
    private final MensajeChatRepository mensajeChatRepository;

    public ColaboradorController(UsuarioRepository usuarioRepository,
                                 ColaboradorRepository colaboradorRepository,
                                 HabilidadRepository habilidadRepository,
                                 CertificacionRepository certificacionRepository,
                                 AsignacionRepository asignacionRepository,
                                 ForoRepository foroRepository,
                                 NotificacionRepository notificacionRepository,
                                 MensajeChatRepository mensajeChatRepository) {
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.habilidadRepository = habilidadRepository;
        this.certificacionRepository = certificacionRepository;
        this.asignacionRepository = asignacionRepository;
        this.foroRepository = foroRepository;
        this.notificacionRepository = notificacionRepository;
        this.mensajeChatRepository = mensajeChatRepository;
    }

    private Colaborador colaboradorDemo() {
        List<Usuario> usuarios = usuarioRepository.findByRolNombre("COLABORADOR");
        for (Usuario usuario : usuarios) {
            var colaborador = colaboradorRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
            if (colaborador.isPresent()) return colaborador.get();
        }
        return colaboradorRepository.findAll().stream().findFirst().orElse(null);
    }

    private List<HabilidadColaboradorDTO> habilidades(Integer idColaborador) {
        List<HabilidadColaboradorDTO> lista = new ArrayList<>();
        for (Object[] fila : colaboradorRepository.obtenerHabilidades(idColaborador)) {
            lista.add(new HabilidadColaboradorDTO(
                    ((Number) fila[0]).intValue(),
                    String.valueOf(fila[1]),
                    String.valueOf(fila[2]),
                    String.valueOf(fila[3]),
                    fila[4] == null ? BigDecimal.ZERO : new BigDecimal(fila[4].toString())
            ));
        }
        return lista;
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        Colaborador c = colaboradorDemo();
        model.addAttribute("titulo", "Inicio - Colaborador");
        model.addAttribute("colaborador", c);
        if (c != null) {
            List<Asignacion> asignaciones = asignacionRepository.findByColaboradorIdColaboradorOrderByFechaInicioDesc(c.getIdColaborador());
            model.addAttribute("asignaciones", asignaciones.stream().limit(5).toList());
            model.addAttribute("totalAsignaciones", asignaciones.size());
            model.addAttribute("habilidades", habilidades(c.getIdColaborador()).size());
            model.addAttribute("foros", foroRepository.findAllByOrderByFechaCreacionDesc().stream().limit(4).toList());
            model.addAttribute("notificaciones", notificacionRepository.findByUsuarioIdUsuarioOrderByFechaCreacionDesc(c.getUsuario().getIdUsuario()).stream().limit(3).toList());
        }
        return "colaborador/inicio";
    }

    @GetMapping("/perfil")
    public String perfil(Model model) {
        Colaborador c = colaboradorDemo();
        model.addAttribute("titulo", "Mi perfil");
        model.addAttribute("colaborador", c);
        model.addAttribute("certificaciones", c == null ? List.of()
                : certificacionRepository.findByColaboradorIdColaboradorOrderByFechaEmisionDesc(c.getIdColaborador()));
        model.addAttribute("habilidades", c == null ? List.of() : habilidades(c.getIdColaborador()));
        return "colaborador/perfil";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfil(Model model) {
        model.addAttribute("titulo", "Editar perfil");
        model.addAttribute("colaborador", colaboradorDemo());
        return "colaborador/perfil-form";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(Colaborador colaborador,
                                @RequestParam("nombres") String nombres,
                                @RequestParam("apellidos") String apellidos,
                                @RequestParam("telefono") String telefono) {
        Colaborador actual = colaboradorRepository.findById(colaborador.getIdColaborador()).orElseThrow();
        Usuario usuario = actual.getUsuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setTelefono(telefono);
        usuarioRepository.save(usuario);

        actual.setCargo(colaborador.getCargo());
        actual.setArea(colaborador.getArea());
        actual.setSeniority(colaborador.getSeniority());
        actual.setBiografia(colaborador.getBiografia());
        actual.setInteresesProfesionales(colaborador.getInteresesProfesionales());
        actual.setDisponibilidadBase(colaborador.getDisponibilidadBase());
        colaboradorRepository.save(actual);
        return "redirect:/colaborador/perfil";
    }

    @GetMapping("/certificaciones")
    public String certificaciones(Model model) {
        Colaborador c = colaboradorDemo();
        List<Certificacion> lista = c == null ? List.of() : certificacionRepository.findByColaboradorIdColaboradorOrderByFechaEmisionDesc(c.getIdColaborador());
        model.addAttribute("titulo", "Certificaciones");
        model.addAttribute("certificaciones", lista);
        model.addAttribute("vigentes", lista.stream().filter(cert -> cert.getEstado() == Certificacion.EstadoCertificacion.VIGENTE).count());
        return "colaborador/certificaciones";
    }

    @GetMapping("/habilidades")
    public String habilidades(Model model) {
        Colaborador c = colaboradorDemo();
        model.addAttribute("titulo", "Mis habilidades");
        model.addAttribute("colaborador", c);
        model.addAttribute("habilidades", c == null ? List.of() : habilidades(c.getIdColaborador()));
        model.addAttribute("catalogo", habilidadRepository.findByEstadoTrueOrderByNombreAsc());
        return "colaborador/habilidades";
    }

    @PostMapping("/habilidades/guardar")
    public String guardarHabilidad(@RequestParam("idHabilidad") Integer idHabilidad,
                                   @RequestParam("nivel") String nivel,
                                   @RequestParam("aniosExperiencia") BigDecimal aniosExperiencia) {
        Colaborador c = colaboradorDemo();
        if (c != null) {
            colaboradorRepository.guardarHabilidad(c.getIdColaborador(), idHabilidad, nivel, aniosExperiencia);
        }
        return "redirect:/colaborador/habilidades";
    }

    @GetMapping("/proyectos")
    public String proyectos(Model model) {
        Colaborador c = colaboradorDemo();
        model.addAttribute("titulo", "Mis proyectos");
        model.addAttribute("asignaciones", c == null ? List.of()
                : asignacionRepository.findByColaboradorIdColaboradorOrderByFechaInicioDesc(c.getIdColaborador()));
        return "colaborador/proyectos";
    }

    @GetMapping("/asignaciones")
    public String asignaciones(Model model) {
        Colaborador c = colaboradorDemo();
        model.addAttribute("titulo", "Mis asignaciones");
        model.addAttribute("asignaciones", c == null ? List.of()
                : asignacionRepository.findByColaboradorIdColaboradorOrderByFechaInicioDesc(c.getIdColaborador()));
        return "colaborador/asignaciones";
    }

    @GetMapping("/foros")
    public String foros(Model model) {
        model.addAttribute("titulo", "Foros");
        model.addAttribute("foros", foroRepository.findAllByOrderByFechaCreacionDesc());
        return "colaborador/foros";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Model model) {
        Colaborador c = colaboradorDemo();
        List<Notificacion> lista = c == null ? List.of() : notificacionRepository.findByUsuarioIdUsuarioOrderByFechaCreacionDesc(c.getUsuario().getIdUsuario());
        model.addAttribute("titulo", "Notificaciones");
        model.addAttribute("notificaciones", lista);
        model.addAttribute("noLeidas", lista.stream().filter(n -> !Boolean.TRUE.equals(n.getLeida())).count());
        return "colaborador/notificaciones";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "idProyecto", required = false) Integer idProyecto, Model model) {
        Colaborador c = colaboradorDemo();
        List<Asignacion> asignaciones = c == null ? List.of() : asignacionRepository.findByColaboradorIdColaboradorOrderByFechaInicioDesc(c.getIdColaborador());
        Proyecto proyectoSeleccionado = null;
        if (!asignaciones.isEmpty()) {
            proyectoSeleccionado = asignaciones.get(0).getProyecto();
            if (idProyecto != null) {
                proyectoSeleccionado = asignaciones.stream().map(Asignacion::getProyecto)
                        .filter(p -> p.getIdProyecto().equals(idProyecto)).findFirst().orElse(proyectoSeleccionado);
            }
        }
        model.addAttribute("titulo", "Chat de proyectos");
        model.addAttribute("colaborador", c);
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("proyectoSeleccionado", proyectoSeleccionado);
        model.addAttribute("mensajes", proyectoSeleccionado == null ? List.of() : mensajeChatRepository.findByProyectoIdProyectoOrderByFechaEnvioAsc(proyectoSeleccionado.getIdProyecto()));
        return "colaborador/chat";
    }

    @PostMapping("/chat/enviar")
    public String enviarMensaje(@RequestParam("idProyecto") Integer idProyecto,
                                @RequestParam("contenido") String contenido) {
        Colaborador c = colaboradorDemo();
        if (c != null && contenido != null && !contenido.isBlank()) {
            MensajeChat mensaje = new MensajeChat();
            mensaje.setProyecto(asignacionRepository.findByColaboradorIdColaboradorOrderByFechaInicioDesc(c.getIdColaborador()).stream()
                    .map(Asignacion::getProyecto).filter(p -> p.getIdProyecto().equals(idProyecto)).findFirst().orElseThrow());
            mensaje.setUsuario(c.getUsuario());
            mensaje.setContenido(contenido);
            mensajeChatRepository.save(mensaje);
        }
        return "redirect:/colaborador/chat?idProyecto=" + idProyecto;
    }
}
