package com.gallery.galleryapplication.services.api;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.ImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
public class FanImageApi {
    public static final String CONTENT_TYPE = "image/webp";
    private final FanImageService fanImageService;

    public FanImageApi(FanImageService fanImageService) {
        this.fanImageService = fanImageService;
    }

    public ResponseEntity<byte[]> getImage(int id, Integer width, Integer height){
        FanArtImage image = fanImageService.getById(id).orElse(null);
        if (image == null) {
            return ResponseEntity.badRequest().body(null);
        }
        byte[] bytesFile;
        if (width != null && height != null && height > 0 && width > 0) {
            try {
                bytesFile = Files.readAllBytes(Path.of(image.getPathToImageThumbnailOnDisc()));
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(CONTENT_TYPE)).cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))).body(bytesFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            bytesFile = Files.readAllBytes(Path.of(image.getPathToFileOnDisc()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(CONTENT_TYPE)).cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))).body(bytesFile);
    }
}
