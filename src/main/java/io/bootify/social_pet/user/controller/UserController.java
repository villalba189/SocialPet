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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;


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
        return "redirect:/login";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("user", userService.get(id));
        return "user/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id,
            @ModelAttribute("user") @Valid final UserDTO userDTO, final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes, final Model model) {
        if (bindingResult.hasErrors()) {
            return "user/edit";
        }
        userService.update(id, userDTO);
        WebUtils.setSession("usuario", userRepository.findById(id).orElse(null));

        model.addAttribute("user", userRepository.findById(id).orElse(null));

        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("user.update.success"));
        return "redirect:/users/profile";
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
            return "redirect:/feed";
        }
    }

    @GetMapping("/profile/{id}")
    public String profile(@PathVariable(name = "id") final Integer id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            User userSession = (User) WebUtils.getRequest().getSession().getAttribute("usuario");
            Set<User> followedUsers = userSession.getFollowedUsers();

            // Establecer isFollowing a false como valor predeterminado
            model.addAttribute("isFollowing", false);
            for (User followedUser : followedUsers) {
                if (followedUser.getId().equals(user.getId())) {
                    model.addAttribute("isFollowing", true);
                    break; // Romper el bucle cuando se encuentra una coincidencia
                }
            }

            return "user/profile";
        } else {
            // Manejar el caso en que el usuario no se encuentre
            return "redirect:/feed";
        }
    }

    @GetMapping("/feed")
    public String Feed(Model model) {
        User user = (User) WebUtils.getRequest().getSession().getAttribute("usuario");
        if (user != null) {
            Set<User> followedUsers = user.getFollowedUsers();

            model.addAttribute("followedUsers", followedUsers);

            return "user/feed";
        } else {
            // Manejar el caso en que el usuario no se encuentre
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout() {

        WebUtils.setSessionLogueado("usuarioLogueado", false);
        WebUtils.getRequest().getSession().invalidate();
        return "redirect:/login";
    }

    @GetMapping("/follow/{userIdToFollow}")
    public String followUser(@PathVariable("userIdToFollow") Integer userIdToFollow) {

        Integer currentUserId = (Integer) WebUtils.getRequest().getSession().getAttribute("id");

        // Llama al servicio para seguir al usuario
        boolean success = userService.followUser(currentUserId, userIdToFollow);
        WebUtils.setSession("usuario", userRepository.findById(currentUserId).orElse(null));

        // Redirige según si el proceso fue exitoso o no
        if (success) {
            return "redirect:/users/profile/" + userIdToFollow; // Modifica según tu lógica de aplicación
        } else {
            return "redirect:/ruta-de-fallo"; // Modifica según tu lógica de aplicación
        }
    }

    @GetMapping("/unfollow/{userIdToUnfollow}")
    public String unfollowUser(@PathVariable("userIdToUnfollow") Integer userIdToUnfollow) {

        Integer currentUserId = (Integer) WebUtils.getRequest().getSession().getAttribute("id");

        // Llama al servicio para dejar de seguir al usuario
        boolean success = userService.unfollowUser(currentUserId, userIdToUnfollow);
        WebUtils.setSession("usuario", userRepository.findById(currentUserId).orElse(null));

        // Redirige según si el proceso fue exitoso o no
        if (success) {
            return "redirect:/users/profile/" + userIdToUnfollow;
        } else {
            return "redirect:/ruta-de-fallo"; // Modifica según tu lógica de aplicación
        }
    }
    @GetMapping("/search")
    public String searchUsers(@RequestParam("searchTerm") String searchTerm, Model model) {
        // Realiza la búsqueda de usuarios por nombre
        List<User> searchResults = userRepository.findByNombreContainingIgnoreCase(searchTerm);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("searchTerm", searchTerm);

        return "user/list"; // Nombre del archivo HTML en la carpeta de templates
    }


}
