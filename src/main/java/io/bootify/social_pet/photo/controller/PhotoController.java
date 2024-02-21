package io.bootify.social_pet.photo.controller;

import io.bootify.social_pet.photo.model.PhotoDTO;
import io.bootify.social_pet.photo.service.PhotoService;
import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.repos.UserRepository;
import io.bootify.social_pet.util.CustomCollectors;
import io.bootify.social_pet.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final UserRepository userRepository;

    public PhotoController(final PhotoService photoService, final UserRepository userRepository) {
        this.photoService = photoService;
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void prepareContext(final Model model) {
        model.addAttribute("userValues", userRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getId, User::getEmail)));
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("photos", photoService.findAll());
        return "photo/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("photo") final PhotoDTO photoDTO) {
        return "photo/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("photo") @Valid final PhotoDTO photoDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "photo/add";
        }
        photoService.create(photoDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("photo.create.success"));
        return "redirect:/photos";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id, final Model model) {
        model.addAttribute("photo", photoService.get(id));
        return "photo/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Integer id,
            @ModelAttribute("photo") @Valid final PhotoDTO photoDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
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
        photoService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("photo.delete.success"));
        return "redirect:/photos";
    }

}
