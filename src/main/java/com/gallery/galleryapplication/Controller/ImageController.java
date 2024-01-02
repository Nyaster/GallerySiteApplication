package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.services.ImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@RestController
@RequestMapping("/api/")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
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

}
