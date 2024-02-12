package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.services.TagService;
import com.gallery.galleryapplication.util.ModelPreparatorForPages;
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

import java.util.Optional;

@Controller
public class PageController {
    private final int PAGESIZE = 20;
    private final ImageService imageService;
    private final TagService tagService;
    private final FanImageService fanImageService;
    private final ModelPreparatorForPages modelPreparatorForPages;

    public PageController(ImageService imageService, TagService tagService, FanImageService fanImageService, ModelPreparatorForPages modelPreparatorForPages) {
        this.imageService = imageService;
        this.tagService = tagService;
        this.fanImageService = fanImageService;
        this.modelPreparatorForPages = modelPreparatorForPages;
    }
    @GetMapping("/")
    public String redirectToImages(){
        return "redirect:/image";
    }
    @GetMapping("/image"
    )
    public String indexPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page) {
        Pageable paging = PageRequest.of(page - 1, PAGESIZE, Sort.by("creationDate").descending().and(Sort.by("mediaId").descending()));
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
        model = modelPreparatorForPages.prepareModelForIndexAndFanArtPage(model,pages, "/image",PAGESIZE);
        return "index";
    }
    @GetMapping("/fan-images")
    public String fanArtsPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page){
        Pageable paging = PageRequest.of(page - 1, PAGESIZE, Sort.by("creationDate").descending().and(Sort.by("id").descending()));
        Page<FanArtImage> pages;
        if (keyword == null || keyword.isBlank()) {
            pages = fanImageService.getAll(paging, true);
        } else {
            pages = fanImageService.getImagesByTags(keyword, paging);
            model.addAttribute("keyword", keyword);
        }
        model = modelPreparatorForPages.prepareModelForIndexAndFanArtPage(model,pages, "/fan-images",PAGESIZE);
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
