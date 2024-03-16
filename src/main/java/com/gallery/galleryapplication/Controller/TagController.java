package com.gallery.galleryapplication.Controller;

import com.gallery.galleryapplication.models.DDO.ImageSettings;
import com.gallery.galleryapplication.models.DDO.TagsDDO;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.services.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class TagController {
    final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/api/tags")
    public @ResponseBody List<String> getTags(@RequestParam String value) {
        String lastTag = Arrays.stream(value.split(",")).toList().getLast().trim();
        return tagService.findTagsByNameLikeIgnoreCase(lastTag).stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/tags")
    public @ResponseBody List<TagsDDO> getAllTags(){
        return tagService.getAll().parallelStream().map(x->{
            TagsDDO imageSettings = new TagsDDO();
            imageSettings.setValue(x.getName());
            imageSettings.setTagGroup(x.getTagGroup());
            imageSettings.setId(x.getId());
            return imageSettings;
        }).collect(Collectors.toList());
    }

}
