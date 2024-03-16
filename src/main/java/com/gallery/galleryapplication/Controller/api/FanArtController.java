package com.gallery.galleryapplication.Controller.api;

import com.gallery.galleryapplication.models.DDO.ImageSettings;
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
@RequestMapping("/api/fan-image")
public class FanArtController {
    private final FanImageService fanImageService;
    private final FanImageApi fanImageApi;

    public FanArtController(FanImageService imageService, FanImageApi fanImageApi) {
        this.fanImageService = imageService;
        this.fanImageApi = fanImageApi;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id, @RequestParam(required = false) Integer width, @RequestParam(required = false) Integer height) {
        return fanImageApi.getImage(id, width, height);
    }
    @PostMapping("/{id}")
    public ResponseEntity SaveEditTags(@PathVariable Integer id, @RequestBody ImageSettings imageSettings) {
        fanImageService.changeImageSettings(imageSettings, id);
        return ResponseEntity.ok("Successfully save");
    }






}
