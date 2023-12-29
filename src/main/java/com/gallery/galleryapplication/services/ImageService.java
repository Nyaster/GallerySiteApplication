package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.ImageRepository;
import com.gallery.galleryapplication.repositories.TagRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;

    public ImageService(ImageRepository imageRepository, TagRepository tagRepository) {
        this.imageRepository = imageRepository;
        this.tagRepository = tagRepository;
    }

    public Page<Image> imageList(Integer page) {
        return imageRepository.findAll(PageRequest.of(0, 20));
    }

    @Transactional
    public void saveAll(List<Image> list) {
        Set<Tag> tags = list.stream().flatMap(x -> x.getTags().stream()).collect(Collectors.toSet());
        Set<Tag> tagsFromDb = new HashSet<>(tagRepository.findAll());
        tags.removeAll(tagsFromDb);
        tagRepository.saveAll(tags);
        tagsFromDb.addAll(tags);
        list.stream().forEach(x -> {
            List<Tag> temp = x.getTags();
            List<Tag> replacedTags = new ArrayList<>(tagsFromDb);
            replacedTags.retainAll(temp);
            x.setTags(replacedTags);
        });
        imageRepository.saveAll(list);
    }

    public Page<Image> getAll(Pageable paging) {
        return imageRepository.findAll(paging);
    }
    public Page<Image> getImagesByTags(String tags, Pageable paging){
        tags=tags.toLowerCase();
        List<String> temp = Arrays.stream(tags.split(",")).map(String::trim).toList();
        List<Tag> searchTags = temp.stream().map(tagRepository::getTagByName).toList();
        return imageRepository.findImagesByAllGivenTags(searchTags, (long) searchTags.size(),paging);
    }

    public Optional<Image> getByMediaId(int id) {
        return imageRepository.findImageByMediaId(id);
    }
}
