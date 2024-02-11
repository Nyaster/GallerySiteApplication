package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.services.RegistrationService;
import com.gallery.galleryapplication.util.PersonValidator;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class authController {
    private final PersonValidator personValidator;
    private final RegistrationService registrationService;

    public authController(PersonValidator personValidator, RegistrationService registrationService) {
        this.personValidator = personValidator;
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String loginPage() {
        LoggerFactory.getLogger(this.getClass()).info("USER LOGGED IN");
        return "auth/login";
    }

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
            model.addAttribute("images", imageService.getAll());
            return "index";
        }

    }
}

