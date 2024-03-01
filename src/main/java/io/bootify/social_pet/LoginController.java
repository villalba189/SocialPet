package io.bootify.social_pet;

import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.repos.UserRepository;
import io.bootify.social_pet.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("LoginController initialized");
    }

    @GetMapping()
    public String login() {
        log.info("Handling GET request for /login");
        return "/login";
    }

    @PostMapping()
    public String login(@RequestParam("email") String email, @RequestParam("contrasea") String contrasea, Model model, RedirectAttributes redirectAttributes) {
        log.info("Handling POST request for /login");
        Optional<User> usuarioOpt = userRepository.findByEmail(email);

        if(usuarioOpt.isPresent() && usuarioOpt.get().getContrasea().equals(contrasea)) {
            // Contraseña coincide, manejar el login
            log.info("Successful login for user: {}", email);
            WebUtils.setSession("usuario", usuarioOpt.get());
            WebUtils.setSessionLogueado("usuarioLogueado", true);
            return "redirect:/users/feed";
        } else {
            // Usuario no encontrado o contraseña no coincide, mostrar error
            log.warn("Failed login attempt for user: {}", email);
            model.addAttribute("loginError", "Error en las credenciales");
            return "redirect:/login";
        }
    }
}