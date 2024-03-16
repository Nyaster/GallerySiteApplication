package com.gallery.galleryapplication.Controller.api;

import com.gallery.galleryapplication.models.DDO.ImageSettings;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.services.TagService;
import com.gallery.galleryapplication.services.api.ImageApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private final ImageService imageService;
    private final TagService tagService;
    private final ImageApi imageApi;

    public ImageController(ImageService imageService, TagService tagService, ImageApi imageApi) {
        this.imageService = imageService;
        this.tagService = tagService;
        this.imageApi = imageApi;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id, @RequestParam(required = false) Integer width, @RequestParam(required = false) Integer height) {
            return imageApi.getImage(id,width,height);
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> SaveEditTags(@PathVariable Integer id, @RequestBody ImageSettings imageSettings) {
        List<Tag> tagsSet = imageSettings.getTags().stream().map(x -> {
            Tag tag = new Tag();
            tag.setName(x.getValue().toLowerCase());
            return tag;

        }).collect(Collectors.toList());
        List<Tag> newOriginalTags = new ArrayList<>(tagsSet);
        List<Tag> allTags = tagService.getAll();
        tagsSet.removeAll(allTags);
        if (!tagsSet.isEmpty()){
            tagService.addNewTags(tagsSet);
        }
        if (newOriginalTags.stream().anyMatch(x -> x.getName().length() > 50)) {
            return ResponseEntity.badRequest().body("Error in length tags");
        }
        try {
            newOriginalTags.forEach(x -> {
                Tag finalX = x;
                Tag tag = allTags.stream().filter(j -> j.getName().equalsIgnoreCase(finalX.getName())).findAny().orElseThrow();
                x.setTagGroup(tag.getTagGroup());
                x.setId(tag.getId());
                x.setName(x.getName());
            });
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("Something happened while saving tags");
        }
        Optional<Image> image = imageService.getByMediaId(id);
        image.get().setTags(newOriginalTags);
        imageService.editImageData(image.get());
        return ResponseEntity.ok("Succefuly save");
    }

}
