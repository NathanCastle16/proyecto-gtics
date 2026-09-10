package pe.edu.pucp.skillbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.pucp.skillbridge.entity.*;
import pe.edu.pucp.skillbridge.repository.*;

@Controller
@RequestMapping("/foros")
public class ForoController {

    private final ForoRepository foroRepository;
    private final RespuestaForoRepository respuestaForoRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    public ForoController(ForoRepository foroRepository,
                          RespuestaForoRepository respuestaForoRepository,
                          ProyectoRepository proyectoRepository,
                          UsuarioRepository usuarioRepository) {
        this.foroRepository = foroRepository;
        this.respuestaForoRepository = respuestaForoRepository;
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuario autorDemo(String vista) {
        String rol = "PM".equals(vista) ? "PROJECT_MANAGER" : "COLABORADOR";
        return usuarioRepository.findByRolNombre(rol).stream().findFirst()
                .orElseGet(() -> usuarioRepository.findAll().stream().findFirst().orElse(null));
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Integer id,
                          @RequestParam(value = "vista", required = false, defaultValue = "COL") String vista,
                          Model model) {
        Foro foro = foroRepository.findById(id).orElseThrow();
        model.addAttribute("titulo", "Detalle del foro");
        model.addAttribute("foro", foro);
        model.addAttribute("respuestas", respuestaForoRepository.findByForoIdForoOrderByFechaCreacionAsc(id));
        model.addAttribute("rolVista", "PM".equals(vista) ? "PM" : "COL");
        return "foro/detalle";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(value = "vista", required = false, defaultValue = "COL") String vista,
                        Model model) {
        model.addAttribute("titulo", "Nuevo tema");
        model.addAttribute("foro", new Foro());
        model.addAttribute("proyectos", proyectoRepository.findAll());
        model.addAttribute("rolVista", "PM".equals(vista) ? "PM" : "COL");
        return "foro/form";
    }

    @PostMapping("/guardar")
    public String guardar(Foro foro,
                          @RequestParam(value = "idProyecto", required = false) Integer idProyecto,
                          @RequestParam(value = "vista", required = false, defaultValue = "COL") String vista) {
        foro.setAutor(autorDemo(vista));
        if (idProyecto != null) {
            foro.setProyecto(proyectoRepository.findById(idProyecto).orElse(null));
        }
        foroRepository.save(foro);
        return "PM".equals(vista) ? "redirect:/pm/foros" : "redirect:/colaborador/foros";
    }

    @PostMapping("/{id}/responder")
    public String responder(@PathVariable("id") Integer id,
                            @RequestParam("contenido") String contenido,
                            @RequestParam(value = "vista", required = false, defaultValue = "COL") String vista) {
        RespuestaForo respuesta = new RespuestaForo();
        respuesta.setForo(foroRepository.findById(id).orElseThrow());
        respuesta.setAutor(autorDemo(vista));
        respuesta.setContenido(contenido);
        respuesta.setEsSolucion(false);
        respuestaForoRepository.save(respuesta);
        return "redirect:/foros/" + id + "?vista=" + vista;
    }

    @PostMapping("/{idForo}/solucion/{idRespuesta}")
    public String marcarSolucion(@PathVariable Integer idForo,
                                 @PathVariable Integer idRespuesta,
                                 @RequestParam(value = "vista", required = false, defaultValue = "COL") String vista) {
        RespuestaForo respuesta = respuestaForoRepository.findById(idRespuesta).orElseThrow();
        respuesta.setEsSolucion(true);
        respuestaForoRepository.save(respuesta);

        Foro foro = foroRepository.findById(idForo).orElseThrow();
        foro.setEstado(Foro.EstadoForo.RESUELTO);
        foroRepository.save(foro);
        return "redirect:/foros/" + idForo + "?vista=" + vista;
    }
}
