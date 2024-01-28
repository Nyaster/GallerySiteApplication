package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class adminController {
    final ImageService imageService;
    final FanImageService fanImageService;
    private volatile boolean operationInProgress = false;

    public adminController(ImageService imageService, FanImageService fanImageService) {
        this.imageService = imageService;
        this.fanImageService = fanImageService;
    }

    @GetMapping("/admin")
    public String getAdminPage(@RequestParam(required = false) String download) {
        return "admin/admin";
    }

    @PostMapping("/admin")
    public ResponseEntity controlPanelFunctions(@RequestBody String body) {
        if (operationInProgress){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Operation Still Ongoing");
        }
        operationInProgress = true;
        switch (body) {
            case "scan_images" -> {
                fanImageService.scanImagesFromFolder();
            }
            case "download_images"->{
                imageService.analyzeRequestPages();
            }
            case "check_updates" ->{
                imageService.checkUpdates();
            }
            case null, default -> {
                operationInProgress = false;
                return ResponseEntity.badRequest().body("error");
            }
        }
        operationInProgress = false;
        return ResponseEntity.ok("Hi");
    }

}
