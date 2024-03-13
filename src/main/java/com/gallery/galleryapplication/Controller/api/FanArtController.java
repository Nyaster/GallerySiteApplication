package com.gallery.galleryapplication.Controller.api;

import com.gallery.galleryapplication.models.DDO.TagsDDO;
import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.TagService;
import com.gallery.galleryapplication.services.api.FanImageApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fan-images")
public class FanArtController {
    private final TagService tagService;
    private final FanImageService fanImageService;
    private final FanImageApi fanImageApi;

    public FanArtController(TagService tagService, FanImageService imageService, FanImageApi fanImageApi) {
        this.tagService = tagService;
        this.fanImageService = imageService;
        this.fanImageApi = fanImageApi;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id, @RequestParam(required = false) Integer width, @RequestParam(required = false) Integer height) {
        return fanImageApi.getImage(id, width, height);
    }

    @PostMapping("/{id}")
    public ResponseEntity SaveEditTags(@PathVariable Integer id, @RequestBody List<TagsDDO> tagsDDO) {
        List<Tag> tagsSet = tagsDDO.stream().map(x -> {
            Tag tag = new Tag();
            tag.setName(x.getValue().toLowerCase());
            return tag;

        }).collect(Collectors.toList());
        List<Tag> newOriginalTags = new ArrayList<>(tagsSet);
        if (newOriginalTags.stream().anyMatch(x -> x.getName().length() > 50)) {
            return ResponseEntity.badRequest().body("Error in length tags");
        }
        List<Tag> allTags = tagService.getAll();
        tagsSet.removeAll(allTags);
        if (!tagsSet.isEmpty()) {
            tagService.addNewTags(tagsSet);
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
        Optional<FanArtImage> image = fanImageService.getById(id);
        image.get().setTags(newOriginalTags);
        fanImageService.editImageData(image.get());
        return ResponseEntity.ok("Successfully save");
    }


}
