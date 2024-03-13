package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Interfaces.ImageProvider;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.models.enums.TagGroup;
import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.services.TagService;
import com.gallery.galleryapplication.util.ModelPreparatorForPages;
import com.gallery.galleryapplication.util.inMemoryVector.InMemoryVectorManager;
import org.apache.commons.math3.linear.ArrayRealVector;
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

import java.util.*;

@Controller
public class PageController {
    private final int PAGESIZE = 20;
    private final ImageService imageService;
    private final TagService tagService;
    private final FanImageService fanImageService;
    private final ModelPreparatorForPages modelPreparatorForPages;
    private final InMemoryVectorManager inMemoryVectorManager;

    public PageController(ImageService imageService, TagService tagService, FanImageService fanImageService, ModelPreparatorForPages modelPreparatorForPages, InMemoryVectorManager inMemoryVectorManager) {
        this.imageService = imageService;
        this.tagService = tagService;
        this.fanImageService = fanImageService;
        this.modelPreparatorForPages = modelPreparatorForPages;
        this.inMemoryVectorManager = inMemoryVectorManager;
    }

    @GetMapping("/")
    public String redirectToImages() {
        return "redirect:/image";
    }

    @GetMapping("/image")
    public String indexPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page) {
        Pageable paging = PageRequest.of(page - 1, PAGESIZE, Sort.by("creationDate").descending().and(Sort.by("mediaId").descending()));
        Page<Image> pages;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = null;
        if (!authentication.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ANONYMOUS")) {
            personDetails = (PersonDetails) authentication.getPrincipal();
        }
        if (keyword == null || keyword.isBlank()) {
            pages = imageService.getAll(paging, personDetails != null);
        } else {
            pages = imageService.getImagesByTags(keyword, paging);
            model.addAttribute("keyword", keyword);
        }
        model = modelPreparatorForPages.prepareModelForIndexAndFanArtPage(model, pages, "/image", PAGESIZE);
        return "index";
    }

    @GetMapping("/fan-images")
    public String fanArtsPage(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "1") int page) {
        Pageable paging = PageRequest.of(page - 1, PAGESIZE, Sort.by("creationDate").descending().and(Sort.by("id").descending()));
        Page<FanArtImage> pages;
        if (keyword == null || keyword.isBlank()) {
            pages = fanImageService.getAll(paging, true);
        } else {
            pages = fanImageService.getImagesByTags(keyword, paging);
            model.addAttribute("keyword", keyword);
        }
        model = modelPreparatorForPages.prepareModelForIndexAndFanArtPage(model, pages, "/fan-images", PAGESIZE);
        return "index";
    }

    @Secured({"ROLE_ADMIN"})
    @GetMapping("api/image/{id}/edit")
    public String getEditPage(@PathVariable Integer id, Model model) {
        prepareModelForEditPage(model, "image", id);
        return "edit";
    }

    @Secured({"ROLE_EDITOR"})
    @GetMapping("api/fan-images/{id}/edit")
    public String getEditFanArtPage(@PathVariable Integer id, Model model) {
        prepareModelForEditPage(model, "fan-images", id);
        List<FanArtImage> imagesWithoutTags = fanImageService.getImagesWithoutTags();
        imagesWithoutTags.remove(model.getAttribute("image"));
        Collections.shuffle(imagesWithoutTags);
        model.addAttribute("randomImage", imagesWithoutTags.stream().findAny().get());
        return "edit";
    }

    private void prepareModelForEditPage(Model model, String apiType, int id) {
        Optional<? extends ImageProvider> image = apiType.equalsIgnoreCase("image") ? imageService.getByMediaId(id) : fanImageService.getById(id);
        List<Tag> neededTags = tagService.getImagesWithDesiredGroup(TagGroup.CHARACTER);
        neededTags.addAll(tagService.getImagesWithDesiredGroup(TagGroup.RATING));
        String tags = neededTags.stream().map(Tag::getName).reduce((x, y) -> x + "," + y).orElse("");
        model.addAttribute("image", image.get());
        model.addAttribute("initialWhitelist", tags);
        model.addAttribute("tags", image.get().getTags());
        model.addAttribute("apiType", apiType);
    }

    @GetMapping("api/image/{id}/show")
    public String showPage(@PathVariable Integer id, Model model) {
        prepareModelForEditPage(model, "image", id);
        return "show";
    }

    @GetMapping("api/fan-images/{id}/show")
    public String showFanImagesPage(@PathVariable Integer id, Model model) {
        prepareModelForEditPage(model, "fan-images", id);
        FanArtImage image = (FanArtImage) model.getAttribute("image");
        image.setEmbedding(inMemoryVectorManager.getVectorFromDb(image.getId()));
        if (image.getEmbedding() == null) {
            model.addAttribute("near", new ArrayList<FanArtImage>());
            return "show";
        }
        List<FanArtImage> all = fanImageService.getAll();
        all.remove(image);
        all.forEach(x -> x.setEmbedding(inMemoryVectorManager.getVectorFromDb(x.getId())));
        all.parallelStream().filter(x -> x.getEmbedding() != null).forEach((fanImage -> {
            double temp = cosinusDistance(fanImage.getEmbedding(), image.getEmbedding());
            fanImage.setTemporalCousineSimiliraty(temp);
        }));
        List<FanArtImage> list = all.stream().filter(x -> x.getEmbedding() != null).sorted(Comparator.comparing(FanArtImage::getTemporalCousineSimiliraty)).limit(24).toList();
        model.addAttribute("near", list);
        return "show";
    }


    double cosinusDistance(float[] vectorA, float[] vectorB) {
        double[] doubles = floatToDouble(vectorA);
        double[] doubles2 = floatToDouble(vectorB);
        ArrayRealVector arrayRealVectorA = new ArrayRealVector(doubles);
        ArrayRealVector arrayRealVectorB = new ArrayRealVector(doubles2);
        double dotProduct = arrayRealVectorA.dotProduct(arrayRealVectorB);
        double otherNorm = arrayRealVectorA.getNorm();
        double imageNorm = arrayRealVectorB.getNorm();
        double similiraty = dotProduct / (imageNorm * otherNorm);
        return 1 - similiraty;
    }

    private double[] floatToDouble(float[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i];
        }
        return result;
    }
}