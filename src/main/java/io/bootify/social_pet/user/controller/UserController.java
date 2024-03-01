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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final PhotoService photoService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(final UserService userService, final UserRepository userRepository, final PhotoService photoService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.photoService = photoService;
        log.info("UserController initialized");
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("followerUsersValues", userRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getId, User::getEmail)));
        log.info("Context prepared for UserController");
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("users", userService.findAll());
        log.info("Listing all users");
        return "user/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("user") final UserDTO userDTO) {
        log.info("Adding a new user");
        return "user/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("user") @Valid final UserDTO userDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        log.info("Attempting to add a new user");
        if (userService.findByEmail(userDTO.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "user.email.duplicated");
            log.warn("User email is duplicated");
        }
        if (bindingResult.hasErrors()) {
            log.warn("Binding result has errors");
            return "user/add";
        }
        userService.create(userDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("user.create.success"));
        log.info("User added successfully");
        return "redirect:/login";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        log.info("Editing user with id: {}", id);
        model.addAttribute("user", userService.get(id));
        return "user/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("user") @Valid final UserDTO userDTO, final BindingResult bindingResult,
                       final RedirectAttributes redirectAttributes, final Model model) {
        log.info("Attempting to edit user with id: {}", id);
        if (bindingResult.hasErrors()) {
            log.warn("Binding result has errors");
            return "user/edit";
        }
        userService.update(id, userDTO);
        WebUtils.setSession("usuario", userRepository.findById(id).orElse(null));

        model.addAttribute("user", userRepository.findById(id).orElse(null));

        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("user.update.success"));
        log.info("User with id: {} updated successfully", id);
        return "redirect:/users/profile";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Integer id,
                         final RedirectAttributes redirectAttributes) {
        log.info("Attempting to delete user with id: {}", id);
        final ReferencedWarning referencedWarning = userService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
            log.warn("User with id: {} cannot be deleted due to referenced warning", id);
        } else {
            userService.delete(id);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("user.delete.success"));
            log.info("User with id: {} deleted successfully", id);
        }
        return "redirect:/users";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = (User) WebUtils.getRequest().getSession().getAttribute("usuario");
        if (user != null) {
            model.addAttribute("user", user);
            log.info("Displaying profile of user with id: {}", user.getId());
            return "user/profile";
        } else {
            log.warn("User not found in session");
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

            model.addAttribute("isFollowing", false);
            for (User followedUser : followedUsers) {
                if (followedUser.getId().equals(user.getId())) {
                    model.addAttribute("isFollowing", true);
                    break;
                }
            }

            log.info("Displaying profile of user with id: {}", id);
            return "user/profile";
        } else {
            log.warn("User with id: {} not found", id);
            return "redirect:/feed";
        }
    }

    @GetMapping("/feed")
    public String Feed(Model model) {
        User user = (User) WebUtils.getRequest().getSession().getAttribute("usuario");
        if (user != null) {
            Set<User> followedUsers = user.getFollowedUsers();

            model.addAttribute("followedUsers", followedUsers);
            log.info("Displaying feed for user with id: {}", user.getId());
            return "user/feed";
        } else {
            log.warn("User not found in session");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        log.info("Logging out user");
        WebUtils.setSessionLogueado("usuarioLogueado", false);
        WebUtils.getRequest().getSession().invalidate();
        return "redirect:/login";
    }

    @GetMapping("/follow/{userIdToFollow}")
    public String followUser(@PathVariable("userIdToFollow") Integer userIdToFollow) {
        log.info("Attempting to follow user with id: {}", userIdToFollow);
        Integer currentUserId = (Integer) WebUtils.getRequest().getSession().getAttribute("id");

        boolean success = userService.followUser(currentUserId, userIdToFollow);
        WebUtils.setSession("usuario", userRepository.findById(currentUserId).orElse(null));

        if (success) {
            log.info("User with id: {} followed successfully", userIdToFollow);
            return "redirect:/users/profile/" + userIdToFollow;
        } else {
            log.warn("Failed to follow user with id: {}", userIdToFollow);
            return "redirect:/ruta-de-fallo"; // Modifica según tu lógica de aplicación
        }
    }
    @GetMapping("/search")
    public String searchUsers(@RequestParam("searchTerm") String searchTerm, Model model) {
        log.info("Searching users with term: {}", searchTerm);
        // Realiza la búsqueda de usuarios por nombre
        List<User> searchResults = userRepository.findByNombreContainingIgnoreCase(searchTerm);
        if (searchResults.isEmpty()) {
            log.info("No users found with term: {}", searchTerm);
        } else {
            log.info("Found {} users with term: {}", searchResults.size(), searchTerm);
        }
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("searchTerm", searchTerm);

        return "user/list"; // Nombre del archivo HTML en la carpeta de templates
    }


}
