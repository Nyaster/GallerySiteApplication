package com.gallery.galleryapplication.Controller.api;

import com.gallery.galleryapplication.models.DDO.TagsDDO;
import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.services.FanImageService;
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

    public FanArtController(TagService tagService, FanImageService imageService1, ImageService imageService) {
        this.tagService = tagService;
        this.fanImageService = imageService1;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id, @RequestParam(required = false) Integer width, @RequestParam(required = false) Integer height) {
        FanArtImage image = fanImageService.getById(id).orElse(null);
        if (image == null) {
            return ResponseEntity.badRequest().body(null);
        }
        byte[] bytesFile;
        if (image.getPathToImageThumbnailOnDisc() !=null && width != null && height != null && height > 0 && width > 0) {
            try {
                if (!image.getPathToImageThumbnailOnDisc().isEmpty()) {
                    bytesFile = Files.readAllBytes(Path.of(image.getPathToImageThumbnailOnDisc()));
                    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))).body(bytesFile);
                }
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
        if (newOriginalTags.stream().anyMatch(x -> x.getName().length() > 50)) {
            return ResponseEntity.badRequest().body("Error in length tags");
        }
        List<Tag> allTags = tagService.getAll();
        tagsSet.removeAll(allTags);
        if (!tagsSet.isEmpty()) {
            tagService.addNewTags(tagsSet);
        }
        try {
            newOriginalTags.forEach(x -> x.setId(allTags.stream().filter(j -> j.getName().equalsIgnoreCase(x.getName())).map(Tag::getId).findAny().orElseThrow()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("Something happened while saving tagsg");
        }
        Optional<FanArtImage> image = fanImageService.getById(id);
        image.get().setTags(newOriginalTags);
        fanImageService.editImageData(image.get());
        return ResponseEntity.ok("Succefuly save");
    }


}
