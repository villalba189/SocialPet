package io.bootify.social_pet.photo.controller;

import io.bootify.social_pet.photo.domain.Photo;
import io.bootify.social_pet.photo.model.PhotoDTO;
import io.bootify.social_pet.photo.repos.PhotoRepository;
import io.bootify.social_pet.photo.service.PhotoService;
import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.repos.UserRepository;
import io.bootify.social_pet.util.CustomCollectors;
import io.bootify.social_pet.util.WebUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

    public PhotoController(final PhotoService photoService, final UserRepository userRepository, final PhotoRepository photoRepository) {
        this.photoService = photoService;
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
        log.info("PhotoController initialized");
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        log.info("Preparing context for PhotoController");
        model.addAttribute("userValues", userRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getId, User::getEmail)));
    }

    @GetMapping
    public String list(final Model model) {
        log.info("Handling GET request for /photos");
        model.addAttribute("photos", photoService.findAll());
        return "photo/list";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable(name = "id") final Integer id, final Model model) {
        log.info("Handling GET request for /photos/{}", id);
        Photo photoOpt = photoRepository.findById(id).orElse(null);
        model.addAttribute("photo", photoOpt);
        return "photo/view";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("photo") final PhotoDTO photoDTO, Model model, HttpSession session) {
        log.info("Handling GET request for /photos/add");
        User currentUser = (User) session.getAttribute("usuario");
        if (currentUser != null) {
            // Asegúrate de que tu DTO de foto tenga un campo para el usuario (por ejemplo, userId) y establece su valor aquí
            photoDTO.setUser(currentUser.getId());
            model.addAttribute("currentUser", currentUser);
        }
        return "photo/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("photo") @Valid final PhotoDTO photoDTO,
                      final BindingResult bindingResult, final RedirectAttributes redirectAttributes, HttpSession session) {
        log.info("Handling POST request for /photos/add");
        if (bindingResult.hasErrors()) {
            return "photo/add";
        }
        photoService.create(photoDTO);
        User currentUser = (User) session.getAttribute("usuario");
        WebUtils.setSession("usuario", userRepository.findById(currentUser.getId()).orElse(null));

        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("photo.create.success"));
        return "redirect:/users/feed";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        log.info("Handling GET request for /photos/edit/{}", id);
        model.addAttribute("photo", photoService.get(id));
        return "photo/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id,
                       @ModelAttribute("photo") @Valid final PhotoDTO photoDTO,
                       final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        log.info("Handling POST request for /photos/edit/{}", id);
        if (bindingResult.hasErrors()) {
            return "photo/edit";
        }
        photoService.update(id, photoDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("photo.update.success"));
        return "redirect:/photos";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Integer id,
                         final RedirectAttributes redirectAttributes) {
        log.info("Handling POST request for /photos/delete/{}", id);
        photoService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("photo.delete.success"));
        return "redirect:/photos";
    }

}