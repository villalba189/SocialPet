package io.bootify.social_pet.user.controller;

import io.bootify.social_pet.photo.domain.Photo;
import io.bootify.social_pet.photo.service.PhotoService;
import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.model.UserDTO;
import io.bootify.social_pet.user.repos.UserRepository;
import io.bootify.social_pet.user.service.UserService;
import io.bootify.social_pet.util.CustomCollectors;
import io.bootify.social_pet.util.ReferencedWarning;
import io.bootify.social_pet.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    private final PhotoService photoService ;

    public UserController(final UserService userService, final UserRepository userRepository, final PhotoService photoService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.photoService = photoService;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("followerUsersValues", userRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getId, User::getEmail)));
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("users", userService.findAll());
        return "user/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("user") final UserDTO userDTO) {
        return "user/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("user") @Valid final UserDTO userDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (userService.findByEmail(userDTO.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "user.email.duplicated");
        }
        if (bindingResult.hasErrors()) {
            return "user/add";
        }
        userService.create(userDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("user.create.success"));
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("user", userService.get(id));
        return "user/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id,
            @ModelAttribute("user") @Valid final UserDTO userDTO, final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/edit";
        }
        userService.update(id, userDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("user.update.success"));
        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Integer id,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = userService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            userService.delete(id);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("user.delete.success"));
        }
        return "redirect:/users";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = (User) WebUtils.getRequest().getSession().getAttribute("usuario");
        if (user != null) {
            model.addAttribute("user", user);

            return "user/profile";
        } else {
            // Manejar el caso en que el usuario no se encuentre
            return "redirect:/users";
        }
    }

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable(name = "id") final Integer id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);

            return "user/profile";
        } else {
            // Manejar el caso en que el usuario no se encuentre
            return "redirect:/users";
        }
    }

}
