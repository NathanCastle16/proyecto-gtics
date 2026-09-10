package pe.edu.pucp.skillbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.pucp.skillbridge.entity.*;
import pe.edu.pucp.skillbridge.repository.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final HabilidadRepository habilidadRepository;
    private final AuditoriaRepository auditoriaRepository;

    public AdminController(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PermisoRepository permisoRepository,
                           ColaboradorRepository colaboradorRepository,
                           HabilidadRepository habilidadRepository,
                           AuditoriaRepository auditoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.habilidadRepository = habilidadRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("titulo", "Panel de administración");
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("usuariosActivos", usuarios.stream()
                .filter(u -> u.getEstado() == Usuario.EstadoUsuario.ACTIVO).count());
        model.addAttribute("totalRoles", rolRepository.count());
        model.addAttribute("totalHabilidades", habilidadRepository.count());
        model.addAttribute("auditorias", auditoriaRepository.findTop50ByOrderByFechaDesc().stream().limit(6).toList());
        model.addAttribute("ultimosUsuarios", usuarios.stream().limit(5).toList());
        return "admin/inicio";
    }

    @GetMapping("/usuarios")
    public String usuarios(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Usuario> lista = usuarioRepository.findAll();
        if (q != null && !q.isBlank()) {
            String texto = q.toLowerCase();
            lista = lista.stream().filter(u ->
                    u.getNombreCompleto().toLowerCase().contains(texto)
                            || u.getCorreo().toLowerCase().contains(texto)
                            || u.getRol().getNombre().toLowerCase().contains(texto)
            ).toList();
        }
        model.addAttribute("titulo", "Usuarios");
        model.addAttribute("usuarios", lista);
        model.addAttribute("q", q);
        model.addAttribute("activos", lista.stream().filter(u -> u.getEstado() == Usuario.EstadoUsuario.ACTIVO).count());
        model.addAttribute("inactivos", lista.stream().filter(u -> u.getEstado() == Usuario.EstadoUsuario.INACTIVO).count());
        model.addAttribute("bloqueados", lista.stream().filter(u -> u.getEstado() == Usuario.EstadoUsuario.BLOQUEADO).count());
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        Usuario usuario = new Usuario();
        usuario.setEstado(Usuario.EstadoUsuario.ACTIVO);
        model.addAttribute("titulo", "Nuevo usuario");
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/usuario-form";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("titulo", "Editar usuario");
        model.addAttribute("usuario", usuarioRepository.findById(id).orElseThrow());
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(Usuario usuario, @RequestParam("idRol") Integer idRol) {
        Rol rol = rolRepository.findById(idRol).orElseThrow();
        usuario.setRol(rol);
        Usuario guardado = usuarioRepository.save(usuario);

        // Relación del modelo final: usuario 1 ---- 0..1 colaborador.
        if (!"ADMINISTRADOR".equals(rol.getNombre())
                && colaboradorRepository.findByUsuarioIdUsuario(guardado.getIdUsuario()).isEmpty()) {
            Colaborador colaborador = new Colaborador();
            colaborador.setUsuario(guardado);
            colaborador.setCargo("Por definir");
            colaborador.setArea("Por definir");
            colaborador.setDisponibilidadBase(100);
            colaboradorRepository.save(colaborador);
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/roles")
    public String roles(Model model) {
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("titulo", "Roles y permisos");
        model.addAttribute("roles", roles);
        model.addAttribute("permisos", permisoRepository.findAll());
        model.addAttribute("totalPermisos", permisoRepository.count());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        return "admin/roles";
    }

    @GetMapping("/habilidades")
    public String habilidades(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Habilidad> habilidades = (q == null || q.isBlank())
                ? habilidadRepository.findAll()
                : habilidadRepository.findByNombreContainingIgnoreCase(q);
        model.addAttribute("titulo", "Catálogo de habilidades");
        model.addAttribute("habilidades", habilidades);
        model.addAttribute("q", q);
        model.addAttribute("activas", habilidades.stream().filter(h -> Boolean.TRUE.equals(h.getEstado())).count());
        model.addAttribute("categorias", habilidades.stream().map(Habilidad::getCategoria).filter(c -> c != null && !c.isBlank()).distinct().count());
        return "admin/habilidades";
    }

    @GetMapping("/habilidades/nueva")
    public String nuevaHabilidad(Model model) {
        Habilidad habilidad = new Habilidad();
        habilidad.setEstado(true);
        model.addAttribute("titulo", "Nueva habilidad");
        model.addAttribute("habilidad", habilidad);
        return "admin/habilidad-form";
    }

    @GetMapping("/habilidades/editar/{id}")
    public String editarHabilidad(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("titulo", "Editar habilidad");
        model.addAttribute("habilidad", habilidadRepository.findById(id).orElseThrow());
        return "admin/habilidad-form";
    }

    @PostMapping("/habilidades/guardar")
    public String guardarHabilidad(Habilidad habilidad) {
        habilidadRepository.save(habilidad);
        return "redirect:/admin/habilidades";
    }

    @GetMapping("/categorias")
    public String categorias(Model model) {
        Map<String, Long> categorias = new LinkedHashMap<>();
        for (Habilidad h : habilidadRepository.findAll()) {
            String categoria = (h.getCategoria() == null || h.getCategoria().isBlank()) ? "Sin categoría" : h.getCategoria();
            categorias.put(categoria, categorias.getOrDefault(categoria, 0L) + 1);
        }
        model.addAttribute("titulo", "Categorías");
        model.addAttribute("categorias", categorias);
        model.addAttribute("totalCategorias", categorias.size());
        model.addAttribute("totalHabilidades", habilidadRepository.count());
        return "admin/categorias";
    }

    @GetMapping("/auditoria")
    public String auditoria(Model model) {
        model.addAttribute("titulo", "Auditoría");
        model.addAttribute("auditorias", auditoriaRepository.findTop50ByOrderByFechaDesc());
        return "admin/auditoria";
    }

    @GetMapping("/monitoreo")
    public String monitoreo(Model model) {
        List<Auditoria> eventos = auditoriaRepository.findTop50ByOrderByFechaDesc();
        model.addAttribute("titulo", "Monitoreo del sistema");
        model.addAttribute("usuarios", usuarioRepository.count());
        model.addAttribute("colaboradores", colaboradorRepository.count());
        model.addAttribute("eventos", eventos.size());
        model.addAttribute("habilidades", habilidadRepository.count());
        model.addAttribute("actividad", eventos.stream().limit(8).toList());
        return "admin/monitoreo";
    }
}
