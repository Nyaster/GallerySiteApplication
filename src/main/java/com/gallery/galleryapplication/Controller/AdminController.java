package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.util.ONNXRuntime;
import com.gallery.galleryapplication.util.inMemoryVector.InMemoryVectorManager;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AdminController {
    final ImageService imageService;
    final FanImageService fanImageService;
    private final ONNXRuntime onnxRuntime;
    private volatile boolean operationInProgress = false;

    public AdminController(ImageService imageService, FanImageService fanImageService, ONNXRuntime onnxRuntime) {
        this.imageService = imageService;
        this.fanImageService = fanImageService;
        this.onnxRuntime = onnxRuntime;
    }

    @GetMapping("/admin")
    public String getAdminPage() {
        return "admin/admin";
    }

    @PostMapping("/admin")
    public ResponseEntity controlPanelFunctions(@RequestBody String body) {
        if (operationInProgress) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Operation Still Ongoing");
        }
        operationInProgress = true;
        try {
            switch (body) {
                case "scan_images" -> fanImageService.scanImagesFromFolder();
                case "download_images" -> imageService.analyzeRequestPages();
                case "check_updates" -> imageService.checkUpdates();
                case "import_images" -> fanImageService.importImages();
                case "generate_embeddings" -> onnxRuntime.generateEmbeedings(fanImageService.getAll());
                case null, default -> {
                    operationInProgress = false;
                    return ResponseEntity.badRequest().body("error");
                }
            }
            return ResponseEntity.ok("Hi");
        }catch (Exception e){
            e.printStackTrace();
            LoggerFactory.getLogger(this.getClass()).error("WTF",e);
        }
        finally {
            operationInProgress = false;
            return ResponseEntity.ok("Hi");
        }
    }

}
