package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.ImageRepository;
import com.gallery.galleryapplication.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;

    public ImageService(ImageRepository imageRepository, TagRepository tagRepository) {
        this.imageRepository = imageRepository;
        this.tagRepository = tagRepository;
    }
    public Page<Image> imageList(Integer page){
       return imageRepository.findAll(PageRequest.of(0,10));
    }
    @Transactional
    public void saveAll(List<Image> list) {
        for (Image img : list) {
            List<Tag> tags = img.getTags().stream().map(tag -> {
                Tag existingTag = tagRepository.getTagByName(tag.getName());
                return existingTag != null ? existingTag : tagRepository.save(tag);
            }).collect(Collectors.toList());

            img.setTags(tags); // Установите обновленный список тегов для изображения
            imageRepository.save(img);
        }
    }
    public List<Image> getAll() {
        return imageRepository.findAll();
    }

    public Optional<Image> getByMediaId(int id) {
        return imageRepository.findImageByMediaId(id);
    }
}
