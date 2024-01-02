package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class defaultController {

    private final ImageService imageService;
    private final RequestPageAnalyzer requestPageAnalyzer;

    public defaultController(ImageService imageService, RequestPageAnalyzer requestPageAnalyzer) {
        this.imageService = imageService;
        this.requestPageAnalyzer = requestPageAnalyzer;
    }

    @GetMapping()
    public String indexPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page) {
        int size = 20;
        Pageable paging = PageRequest.of(page - 1, size, Sort.by("creationDate").descending());
        Page<Image> pages;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = null;
        if (!authentication.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ANONYMOUS")) {
            personDetails = (PersonDetails) authentication.getPrincipal();
        }
        if (keyword == null || keyword.isBlank()) {
            pages = imageService.getAll(paging, personDetails!=null);
        } else {
            pages = imageService.getImagesByTags(keyword, paging);
            model.addAttribute("keyword", keyword);
        }
        model.addAttribute("personDetails", personDetails);
        model.addAttribute("images", pages.get().toList());
        model.addAttribute("currentPage", pages.getNumber() + 1);
        model.addAttribute("totalItems", pages.getTotalElements());
        model.addAttribute("totalPages", pages.getTotalPages());
        model.addAttribute("pageSize", size);
        return "index";
    }


    @GetMapping("/admin")
    public String getAdminPage(@RequestParam(required = false) String download) {
        if (download.equals("download")) {
            imageService.saveAll(requestPageAnalyzer.loginAndDownloadImages());
        }
        return "admin/admin";
    }

}
