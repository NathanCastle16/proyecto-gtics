package pe.edu.pucp.skillbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.pucp.skillbridge.dto.CargaColaboradorDTO;
import pe.edu.pucp.skillbridge.entity.*;
import pe.edu.pucp.skillbridge.repository.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pm")
public class ProjectManagerController {

    private final UsuarioRepository usuarioRepository;
    private final ProyectoRepository proyectoRepository;
    private final AsignacionRepository asignacionRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final RecomendacionIARepository recomendacionIARepository;
    private final ForoRepository foroRepository;

    public ProjectManagerController(UsuarioRepository usuarioRepository,
                                    ProyectoRepository proyectoRepository,
                                    AsignacionRepository asignacionRepository,
                                    ColaboradorRepository colaboradorRepository,
                                    RecomendacionIARepository recomendacionIARepository,
                                    ForoRepository foroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.proyectoRepository = proyectoRepository;
        this.asignacionRepository = asignacionRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.recomendacionIARepository = recomendacionIARepository;
        this.foroRepository = foroRepository;
    }

    private Usuario pmDemo() {
        return usuarioRepository.findFirstByRolNombreOrderByIdUsuarioAsc("PROJECT_MANAGER")
                .orElseGet(() -> usuarioRepository.findAll().stream().findFirst().orElse(null));
    }

    private int intValue(Object value) { return value == null ? 0 : ((Number) value).intValue(); }

    private List<CargaColaboradorDTO> cargas() {
        List<CargaColaboradorDTO> resultado = new ArrayList<>();
        for (Object[] fila : colaboradorRepository.obtenerCargaColaboradores()) {
            resultado.add(new CargaColaboradorDTO(
                    intValue(fila[0]), String.valueOf(fila[1]),
                    fila[2] == null ? "-" : String.valueOf(fila[2]),
                    fila[3] == null ? "-" : String.valueOf(fila[3]),
                    intValue(fila[4]), intValue(fila[5]), intValue(fila[6])
            ));
        }
        return resultado;
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        Usuario pm = pmDemo();
        List<Proyecto> proyectos = pm == null ? List.of()
                : proyectoRepository.findByProjectManagerIdUsuarioOrderByFechaCreacionDesc(pm.getIdUsuario());
        List<Asignacion> asignaciones = pm == null ? List.of()
                : asignacionRepository.findByProyectoProjectManagerIdUsuarioOrderByFechaInicioDesc(pm.getIdUsuario());

        model.addAttribute("titulo", "Inicio - Project Manager");
        model.addAttribute("pm", pm);
        model.addAttribute("proyectosActivos", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.ACTIVE).count());
        model.addAttribute("proyectosPlanning", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.PLANNING).count());
        model.addAttribute("asignaciones", asignaciones.size());
        model.addAttribute("colaboradores", asignaciones.stream().map(a -> a.getColaborador().getIdColaborador()).distinct().count());
        model.addAttribute("proyectos", proyectos.stream().limit(5).toList());
        model.addAttribute("ultimasAsignaciones", asignaciones.stream().limit(5).toList());
        return "pm/inicio";
    }

    @GetMapping("/proyectos")
    public String proyectos(@RequestParam(value = "q", required = false) String q, Model model) {
        Usuario pm = pmDemo();
        List<Proyecto> proyectos = pm == null ? List.of()
                : proyectoRepository.findByProjectManagerIdUsuarioOrderByFechaCreacionDesc(pm.getIdUsuario());
        if (q != null && !q.isBlank()) {
            String texto = q.toLowerCase();
            proyectos = proyectos.stream().filter(p -> p.getNombre().toLowerCase().contains(texto)).toList();
        }
        model.addAttribute("titulo", "Proyectos");
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("q", q);
        model.addAttribute("activos", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.ACTIVE).count());
        model.addAttribute("planning", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.PLANNING).count());
        model.addAttribute("completados", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.COMPLETED).count());
        return "pm/proyectos";
    }

    @GetMapping("/proyectos/nuevo")
    public String nuevoProyecto(Model model) {
        Proyecto proyecto = new Proyecto();
        proyecto.setEstado(Proyecto.EstadoProyecto.PLANNING);
        proyecto.setPrioridad(Proyecto.Prioridad.MEDIA);
        proyecto.setProgreso(0);
        model.addAttribute("titulo", "Nuevo proyecto");
        model.addAttribute("proyecto", proyecto);
        return "pm/proyecto-form";
    }

    @GetMapping("/proyectos/editar/{id}")
    public String editarProyecto(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("titulo", "Editar proyecto");
        model.addAttribute("proyecto", proyectoRepository.findById(id).orElseThrow());
        return "pm/proyecto-form";
    }

    @PostMapping("/proyectos/guardar")
    public String guardarProyecto(Proyecto proyecto) {
        proyecto.setProjectManager(pmDemo());
        proyectoRepository.save(proyecto);
        return "redirect:/pm/proyectos";
    }

    @GetMapping("/asignaciones")
    public String asignaciones(Model model) {
        Usuario pm = pmDemo();
        List<Asignacion> lista = pm == null ? List.of()
                : asignacionRepository.findByProyectoProjectManagerIdUsuarioOrderByFechaInicioDesc(pm.getIdUsuario());
        model.addAttribute("titulo", "Asignaciones");
        model.addAttribute("asignaciones", lista);
        model.addAttribute("activas", lista.stream().filter(a -> a.getEstado() == Asignacion.EstadoAsignacion.ACTIVA).count());
        model.addAttribute("planificadas", lista.stream().filter(a -> a.getEstado() == Asignacion.EstadoAsignacion.PLANIFICADA).count());
        return "pm/asignaciones";
    }

    @GetMapping("/asignaciones/nueva")
    public String nuevaAsignacion(Model model) {
        Usuario pm = pmDemo();
        model.addAttribute("titulo", "Nueva asignación");
        model.addAttribute("asignacion", new Asignacion());
        model.addAttribute("proyectos", pm == null ? List.of()
                : proyectoRepository.findByProjectManagerIdUsuarioOrderByFechaCreacionDesc(pm.getIdUsuario()));
        model.addAttribute("colaboradores", colaboradorRepository.findAll());
        return "pm/asignacion-form";
    }

    @PostMapping("/asignaciones/guardar")
    public String guardarAsignacion(Asignacion asignacion,
                                    @RequestParam("idProyecto") Integer idProyecto,
                                    @RequestParam("idColaborador") Integer idColaborador) {
        asignacion.setProyecto(proyectoRepository.findById(idProyecto).orElseThrow());
        asignacion.setColaborador(colaboradorRepository.findById(idColaborador).orElseThrow());
        asignacionRepository.save(asignacion);
        return "redirect:/pm/asignaciones";
    }

    @GetMapping("/colaboradores")
    public String colaboradores(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Colaborador> lista = (q == null || q.isBlank()) ? colaboradorRepository.findAll() : colaboradorRepository.buscar(q);
        model.addAttribute("titulo", "Colaboradores");
        model.addAttribute("colaboradores", lista);
        model.addAttribute("q", q);
        return "pm/colaboradores";
    }

    @GetMapping("/disponibilidad")
    public String disponibilidad(Model model) {
        List<CargaColaboradorDTO> lista = cargas();
        model.addAttribute("titulo", "Disponibilidad de colaboradores");
        model.addAttribute("cargas", lista);
        model.addAttribute("disponibles", lista.stream().filter(c -> c.getDisponibilidad() >= 50).count());
        model.addAttribute("ocupados", lista.stream().filter(c -> c.getDisponibilidad() < 50).count());
        return "pm/disponibilidad";
    }

    @GetMapping("/recomendaciones")
    public String recomendaciones(Model model) {
        model.addAttribute("titulo", "Recomendaciones IA");
        model.addAttribute("recomendaciones", recomendacionIARepository.findAllByOrderByPorcentajeMatchDesc());
        model.addAttribute("proyectos", proyectoRepository.findAll());
        return "pm/recomendaciones";
    }

    @GetMapping("/foros")
    public String foros(Model model) {
        model.addAttribute("titulo", "Espacios de discusión");
        model.addAttribute("foros", foroRepository.findAllByOrderByFechaCreacionDesc());
        return "pm/foros";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        Usuario pm = pmDemo();
        List<Proyecto> proyectos = pm == null ? List.of() : proyectoRepository.findByProjectManagerIdUsuarioOrderByFechaCreacionDesc(pm.getIdUsuario());
        List<Asignacion> asignaciones = pm == null ? List.of() : asignacionRepository.findByProyectoProjectManagerIdUsuarioOrderByFechaInicioDesc(pm.getIdUsuario());
        model.addAttribute("titulo", "Reportes");
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("totalProyectos", proyectos.size());
        model.addAttribute("proyectosActivos", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.ACTIVE).count());
        model.addAttribute("proyectosCompletados", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.COMPLETED).count());
        model.addAttribute("totalAsignaciones", asignaciones.size());
        return "pm/reportes";
    }

    @GetMapping("/historial")
    public String historial(Model model) {
        Usuario pm = pmDemo();
        List<Proyecto> proyectos = pm == null ? List.of() : proyectoRepository.findByProjectManagerIdUsuarioOrderByFechaCreacionDesc(pm.getIdUsuario());
        model.addAttribute("titulo", "Historial de proyectos");
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("finalizados", proyectos.stream().filter(p -> p.getEstado() == Proyecto.EstadoProyecto.COMPLETED || p.getEstado() == Proyecto.EstadoProyecto.CANCELLED).count());
        return "pm/historial";
    }
}
