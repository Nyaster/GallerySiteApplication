package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.enums.ImageType;
import com.gallery.galleryapplication.services.FanImageService;
import com.gallery.galleryapplication.services.ImageService;
import com.gallery.galleryapplication.util.ONNXRuntime;
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
                case "generate_embeddings" -> onnxRuntime.generateEmbeddings(fanImageService.getAll(), ImageType.fanimage);
                case "generate_embeddings_image" ->onnxRuntime.generateEmbeddings(imageService.getAll(),ImageType.image);
                case null, default -> {
                    operationInProgress = false;
                    return ResponseEntity.badRequest().body("error");
                }
            }
            operationInProgress = false;
            return ResponseEntity.ok("Hi");
        }catch (Exception e){
            operationInProgress = false;
            LoggerFactory.getLogger(this.getClass()).error("WTF",e);
        }
        finally {
            operationInProgress = false;
            return ResponseEntity.ok("Hi");
        }

    }

}
