package io.bootify.social_pet;

import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.repos.UserRepository;
import io.bootify.social_pet.util.WebUtils;
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

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping()
    public String login() {
        return "/login";
    }
    @PostMapping()
    public String login(@RequestParam("email") String email, @RequestParam("contrasea") String contrasea, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> usuarioOpt = userRepository.findByEmail(email);

        if(usuarioOpt.isPresent() && usuarioOpt.get().getContrasea().equals(contrasea)) {
            // Contraseña coincide, manejar el login
            WebUtils.setSession("usuario", usuarioOpt.get());
            return "redirect:/users";
        } else {
            // Usuario no encontrado o contraseña no coincide, mostrar error
            model.addAttribute("loginError", "Error en las credenciales");
            return "redirect:/login";
        }
    }



}
