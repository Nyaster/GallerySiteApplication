package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.Person;
import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.services.RegistrationService;
import com.gallery.galleryapplication.util.PersonValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class defaultController {

    private final ImageService imageService;

    public defaultController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping()
    public String indexPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        model.addAttribute("personDetails", personDetails);
        model.addAttribute("images",imageService.getAll());
        return "index";
    }


}
