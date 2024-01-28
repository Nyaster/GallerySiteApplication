package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
@Transactional
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
    @Transactional
    public void addNewTags(List<Tag> tag){
        tagRepository.saveAll(tag);
        tagRepository.flush();
    }
    @Transactional
    public List<Tag> findTagsByNameLikeIgnoreCase(String tag) {
        return tagRepository.findTagsByNameStartsWith(tag);
    }
    public String getAllInStrin(){
        return tagRepository.findAll().stream().map(Tag::getName).reduce((x,y)->x+","+y).orElse("");
    }

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }
}
