package hospedagem.controller;

import hospedagem.controller.dto.LoginRequest;
import hospedagem.controller.dto.RegisterRequest;
import hospedagem.model.exception.EmailJaCadastradoException;
import hospedagem.model.service.JwtService;
import hospedagem.model.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.cookie-name}")
    private String cookieName;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // ---------- Cadastro ----------

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registrar(request);
        } catch (EmailJaCadastradoException e) {
            model.addAttribute("erro", e.getMessage());
            return "register";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            return "register";
        }

        return "redirect:/login?cadastroSucesso";
    }

    // ---------- Login ----------

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult bindingResult,
            Model model,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
            );
        } catch (BadCredentialsException e) {
            model.addAttribute("erro", "E-mail ou senha invalidos");
            return "login";
        }

        UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        String token = jwtService.gerarToken(userDetails);

        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (expirationMs / 1000));
        // cookie.setSecure(true); // habilitar quando estiver rodando atras de HTTPS
        response.addCookie(cookie);

        return "redirect:/home";
    }
}
