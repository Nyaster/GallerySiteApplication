package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class TagController {
    @Autowired
    TagService tagService;
    @GetMapping("/api/tags")
    public List<String> getTags(@RequestParam String query) {
        // Ваш код для обработки запроса и возврата подходящих тегов
        // Например, из базы данных или статического списка
        String lastTag = Arrays.stream(query.split(",")).toList().getLast().trim();
        return tagService.findTagsByNameLikeIgnoreCase(lastTag).stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

}
