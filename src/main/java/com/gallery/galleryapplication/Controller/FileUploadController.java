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
    private static final String UPLOAD_DIR = "./testing";
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
        List<String> acceptedFiles = new ArrayList<>();
        acceptedFiles.add("image/jpeg");
        acceptedFiles.add("image/png");
        acceptedFiles.add("image/webp");
        if (file[0].getSize() > 30_000_000 || !acceptedFiles.contains(file[0].getContentType())) {
            return ResponseEntity.badRequest().body("Wrong filetype or file too big");
        }
        try {
            System.out.println(file[0].getContentType());
            String fileName = file[0].getOriginalFilename();
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdir();
            }
            File serverFile = new File(directory.getAbsolutePath() + File.separator + fileName);
            file[0].transferTo(serverFile);
            Integer i = fileUploadHandlerService.saveNewFile(serverFile, (PersonDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return ResponseEntity.ok(i);
        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).error("Error while loading file", e);
            return ResponseEntity.status(500).body("Error while uploading files");
        }
    }
}
