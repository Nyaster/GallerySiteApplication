package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.enums.ImageType;
import com.gallery.galleryapplication.services.PageService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {
    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/")
    public String redirectToImages() {
        return "redirect:/image";
    }

    @GetMapping("/image")
    public String indexPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page) {
        pageService.getImages(model, keyword, page, ImageType.image);
        return "index";
    }

    @GetMapping("/fan-image")
    public String fanArtsPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page) {
        pageService.getImages(model, keyword, page, ImageType.fanimage);
        return "index";
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("api/image/{id}/edit")
    public String getEditPage(@PathVariable Integer id, Model model) {
        pageService.prepareModelForEditPage(model,ImageType.image,id);
        return "edit";
    }

    @Secured({"ROLE_EDITOR"})
    @GetMapping("api/fan-image/{id}/edit")
    public String getEditFanArtPage(@PathVariable Integer id, Model model) {
        pageService.prepareModelForEditPage(model,ImageType.fanimage,id);
        return "edit";
    }


    @GetMapping("api/image/{id}/show")
    public String showPage(@PathVariable Integer id, Model model) {
        pageService.prepareModelForEditPage(model, ImageType.image, id);
        return "show";
    }

    @GetMapping("api/fan-image/{id}/show")
    public String showFanImagesPage(@PathVariable Integer id, Model model) {
        pageService.prepareModelForEditPage(model, ImageType.fanimage, id);
        return "show";
    }


}