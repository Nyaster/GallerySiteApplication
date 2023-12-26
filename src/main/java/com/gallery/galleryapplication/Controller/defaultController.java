package com.gallery.galleryapplication.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class defaultController {
    @GetMapping()
    public String indexPage(){
        return "index";
    }
    @GetMapping("/login")
    public String loginPage(){
        return "auth/login";
    }
}
