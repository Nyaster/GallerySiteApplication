package com.gallery.galleryapplication;

import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.WebsiteLoginService;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

@SpringBootApplication
public class GalleryApplication {
    public static void main(String[] args) {
		SpringApplication.run(GalleryApplication.class, args);
	}

}
