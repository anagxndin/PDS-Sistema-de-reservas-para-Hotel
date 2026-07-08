package hospedagem.controller;
import hospedagem.model.entity.User;

import hospedagem.model.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // Rota protegida: so acessivel com um JWT valido (ver SecurityConfig + JwtAuthFilter).
    // A partir daqui, o restante do grupo pluga as telas de acomodacoes/reservas.
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("nome", user.getNome());
        model.addAttribute("email", user.getEmail());
        return "home";
    }
}
