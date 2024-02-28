package io.bootify.social_pet;

import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.repos.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;


@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "users/login";
    }


}
