package com.gallery.galleryapplication.Controller.api;

import com.gallery.galleryapplication.models.DDO.TagsDDO;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.TagRepository;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.services.TagService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private final ImageService imageService;
    private final TagService tagService;

    public ImageController(ImageService imageService, TagService tagService) {
        this.imageService = imageService;
        this.tagService = tagService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id, @RequestParam(required = false) Integer width, @RequestParam(required = false) Integer height) {
        Image image = imageService.getByMediaId(id).orElse(null);
        if (image == null) {
            return ResponseEntity.badRequest().body(null);
        }
        byte[] bytesFile;
        if (width != null && height != null && height > 0 && width > 0) {
            try {
                bytesFile = Files.readAllBytes(Path.of(image.getPathToImageThumbnailOnDisc()));
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))).body(bytesFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            bytesFile = Files.readAllBytes(Path.of(image.getPathToFileOnDisc()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))).body(bytesFile);
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> SaveEditTags(@PathVariable Integer id, @RequestBody List<TagsDDO> tagsDDO) {
        List<Tag> tagsSet = tagsDDO.stream().map(x -> {
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
            newOriginalTags.forEach(x -> x.setId(allTags.stream().filter(j -> j.getName().equalsIgnoreCase(x.getName())).map(Tag::getId).findAny().orElseThrow()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("Something happened while saving tags");
        }
        Optional<Image> image = imageService.getByMediaId(id);
        image.get().setTags(newOriginalTags);
        imageService.editImageData(image.get());
        return ResponseEntity.ok("Succefuly save");
    }

}
