package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    @Transactional
    public void addNewTag(Tag tag){
        if (!tagRepository.existsByName(tag.getName())){
            tagRepository.save(tag);
        }
    }

}
