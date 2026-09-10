package pe.edu.pucp.skillbridge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Selector temporal de rol para la demostración de vistas.
    // La autenticación real se implementará cuando el curso cubra seguridad.
    @PostMapping("/login")
    public String entrar(@RequestParam("rol") String rol) {
        return switch (rol) {
            case "ADMIN" -> "redirect:/admin/inicio";
            case "PM" -> "redirect:/pm/inicio";
            case "RM" -> "redirect:/resource/inicio";
            default -> "redirect:/colaborador/inicio";
        };
    }
}
