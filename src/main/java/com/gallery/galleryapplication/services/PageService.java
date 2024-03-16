package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Interfaces.ImageProvider;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.models.enums.ImageType;
import com.gallery.galleryapplication.models.enums.TagGroup;
import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.util.MathProcessor;
import com.gallery.galleryapplication.util.inMemoryVector.InMemoryVectorManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PageService {
    private final int PAGE_SIZE = 20;
    private final ImageService imageService;
    private final FanImageService fanImageService;
    private final TagService tagService;
    private final InMemoryVectorManager inMemoryVectorManager;

    public PageService(ImageService imageService, FanImageService fanImageService, TagService tagService, InMemoryVectorManager inMemoryVectorManager) {
        this.imageService = imageService;
        this.fanImageService = fanImageService;
        this.tagService = tagService;
        this.inMemoryVectorManager = inMemoryVectorManager;
    }

    public void getImages(Model model, String tagsToSearch, @RequestParam(defaultValue = "1") int page, ImageType imageType) {
        model.addAttribute(getAuthenticatedUser());
        Page<? extends ImageProvider> images = getImages(preparePageForImage(page, imageType), imageType, tagsToSearch);
        model.addAttribute("images", images);
        model.addAttribute("apiType", getImageApiType(imageType));
        model.addAttribute("currentPage", images.getNumber() + 1);
        model.addAttribute("totalItems", images.getTotalElements());
        model.addAttribute("totalPages", images.getTotalPages());
        model.addAttribute("pageSize", PAGE_SIZE);
    }

    private String getImageApiType(ImageType imageType) {
        return switch (imageType) {
            case fanimage -> "fan-image";
            case image -> "image";
        };
    }

    private Pageable preparePageForImage(Integer chosenPage, ImageType imageType) {
        return switch (imageType) {
            case image ->
                    PageRequest.of(chosenPage - 1, PAGE_SIZE, Sort.by("creationDate").descending().and(Sort.by("mediaId").descending()));
            case fanimage ->
                    PageRequest.of(chosenPage - 1, PAGE_SIZE, Sort.by("creationDate").descending().and(Sort.by("id").descending()));
        };
    }

    private PersonDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = null;
        if (!authentication.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ANONYMOUS")) {

            personDetails = (PersonDetails) authentication.getPrincipal();
        }
        return personDetails;
    }

    private Page<? extends ImageProvider> getImages(Pageable pageable, ImageType imageType, String tagsToSearch) {
        Page<? extends ImageProvider> images;
        if (tagsToSearch == null || tagsToSearch.isEmpty()) {
            images = switch (imageType) {
                case image -> imageService.getAll(pageable, true);
                case fanimage -> fanImageService.getAll(pageable, true);
            };
        } else {
            images = switch (imageType) {
                case image -> imageService.getImagesByTags(tagsToSearch, pageable);
                case fanimage -> fanImageService.getImagesByTags(tagsToSearch, pageable);
            };
        }
        return images;
    }

    public void prepareModelForEditPage(Model model, ImageType imageType, int id) {
        Optional<? extends ImageProvider> image = switch (imageType) {
            case image -> imageService.getByMediaId(id);
            case fanimage -> fanImageService.getById(id);
        };
        List<Tag> neededTags = tagService.getImagesWithDesiredGroup(TagGroup.CHARACTER);
        neededTags.addAll(tagService.getImagesWithDesiredGroup(TagGroup.RATING));
        String tags = neededTags.stream().map(Tag::getName).reduce((x, y) -> x + "," + y).orElse("");
        model.addAttribute("image", image.get());
        model.addAttribute("initialWhitelist", tags);
        model.addAttribute("tags", image.get().getTags());
        model.addAttribute("apiType", getImageApiType(imageType));
        model.addAttribute("near", calculateNearImages(image.get(), imageType));
    }

    private List<? extends ImageProvider> calculateNearImages(ImageProvider image, ImageType imageType) {
        List<? extends ImageProvider> all = switch (imageType) {
            case image -> imageService.getAll();
            case fanimage -> fanImageService.getAll();
        };
        float[] imageEmbedding = (inMemoryVectorManager.getVectorFromDb(image.getMediaId(), imageType));
        all.remove(image);
        List<? extends ImageProvider> nearImages = all.parallelStream().map((fanImage -> {
            float[] vectorFromDb = inMemoryVectorManager.getVectorFromDb(fanImage.getMediaId(), imageType);
            if (vectorFromDb != null) {
                double cosineDistance = MathProcessor.cosineDistance(vectorFromDb, imageEmbedding);
                fanImage.setTemporalCousineSimiliraty(cosineDistance);
            }
            return fanImage;
        })).filter(x -> x.getTemporalCousineSimiliraty() != null).sorted(Comparator.comparing(ImageProvider::getTemporalCousineSimiliraty)).limit(24).toList();
        return nearImages;
    }
}