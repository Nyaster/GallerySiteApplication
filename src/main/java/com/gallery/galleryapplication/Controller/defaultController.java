package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.services.TagService;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class defaultController {

    private final ImageService imageService;
    private final RequestPageAnalyzer requestPageAnalyzer;
    private final TagService tagService;
    private final FanImageService fanImageService;

    public defaultController(ImageService imageService, RequestPageAnalyzer requestPageAnalyzer, TagService tagService, FanImageService fanImageService) {
        this.imageService = imageService;
        this.requestPageAnalyzer = requestPageAnalyzer;
        this.tagService = tagService;
        this.fanImageService = fanImageService;
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
        model.addAttribute("pageTitle", "home");
        model.addAttribute("currentPageUrl","/");
        model.addAttribute("apiType","image");
        return "index";
    }
    @GetMapping("/fan-images")
    public String fanArtsPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page){
        int size = 20;
        Pageable paging = PageRequest.of(page - 1, size, Sort.by("creationDate").descending());
        Page<FanArtImage> pages;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = null;
        if (!authentication.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ANONYMOUS")) {
            personDetails = (PersonDetails) authentication.getPrincipal();
        }
        if (keyword == null || keyword.isBlank()) {
            pages = fanImageService.getAll(paging, personDetails!=null);
        } else {
            pages = fanImageService.getImagesByTags(keyword, paging);
            model.addAttribute("keyword", keyword);
        }

        model.addAttribute("personDetails", personDetails);
        model.addAttribute("images", pages.get().toList());
        model.addAttribute("currentPage", pages.getNumber() + 1);
        model.addAttribute("totalItems", pages.getTotalElements());
        model.addAttribute("totalPages", pages.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("pageTitle", "home");
        model.addAttribute("currentPageUrl","/fan-images");
        model.addAttribute("apiType","fan-images");
        return "index";
    }
    @Secured({"ROLE_ADMIN"})
    @GetMapping("api/image/{id}/edit")
    public String getEditPage(@PathVariable Integer id, Model model){
        Optional<Image> image = imageService.getByMediaId(id);
        if (image.isEmpty()){
            return "redirect:/";
        }
        String tags = tagService.getAllInStrin();
        model.addAttribute("image",image.get());
        model.addAttribute("initialWhitelist",tags);
        model.addAttribute("apiType","image");
        return "edit";
    }
    @Secured({"ROLE_EDITOR"})
    @GetMapping("api/fan-images/{id}/edit")
    public String getEditFanArtPage(@PathVariable Integer id, Model model){
        Optional<FanArtImage> image1 = fanImageService.getById(id);
        if (image1.isEmpty()){
            return "redirect:/";
        }
        String tags = tagService.getAllInStrin();
        FanArtImage image = image1.get();
        model.addAttribute("image",image);
        model.addAttribute("initialWhitelist",tags);
        model.addAttribute("apiType","fan-images");
        return "edit";
    }


}
