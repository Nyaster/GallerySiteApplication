package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.services.FileUploadHandlerService;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/fan-images")
public class FileUploadController {
    private static final String UPLOAD_DIR = "./uploaded";
    private final FileUploadHandlerService fileUploadHandlerService;

    public FileUploadController(FileUploadHandlerService fileUploadHandlerService) {
        this.fileUploadHandlerService = fileUploadHandlerService;
    }

    @GetMapping("/upload")
    public String getUploadPage(Model model) {
        return "uploadNewImage";
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity handleFileUpload(@RequestParam("files") MultipartFile[] file) {
        if (file == null || file.length == 0) {
            return ResponseEntity.badRequest().body("No files selected to upload");
        }
        List<String> acceptedFiles = new ArrayList<>(){};
        acceptedFiles.add("image/jpeg");
        acceptedFiles.add("image/png");
        acceptedFiles.add("image/webp");
        if (file[0].getSize() > 30_000_000 || !acceptedFiles.contains(file[0].getContentType())) {
            return ResponseEntity.badRequest().body("Wrong filetype or file too big");
        }
        Integer redirectTo = null;
        try {
            redirectTo = fileUploadHandlerService.saveNewFile(file, (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        } catch (IOException e) {
            LoggerFactory.getLogger(FileUploadController.class).error("Error while saving file", e);
            return ResponseEntity.badRequest().body("Error while saving file");
        }
        return ResponseEntity.ok().body(redirectTo);
    }
}
