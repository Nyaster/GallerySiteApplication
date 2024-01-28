package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.FanArtImageRepository;
import com.gallery.galleryapplication.repositories.TagRepository;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FanImageService {
    final TagRepository tagRepository;
    final RequestPageAnalyzer requestPageAnalyzer;
    final FanArtImageRepository fanArtImageRepository;

    public FanImageService(TagRepository tagRepository, RequestPageAnalyzer requestPageAnalyzer, FanArtImageRepository fanArtImageRepository) {
        this.tagRepository = tagRepository;
        this.requestPageAnalyzer = requestPageAnalyzer;
        this.fanArtImageRepository = fanArtImageRepository;
    }

    public void scanImagesFromFolder() {
        List<FanArtImage> images =requestPageAnalyzer.scanImagesFromFolder();
        if (!images.isEmpty()){
            fanArtImageRepository.saveAll(images);
        }

    }

    public Page<FanArtImage> getAll(Pageable paging, boolean b) {
        Page<FanArtImage> all = fanArtImageRepository.findAll(paging);
        return all;
    }

    public Page<FanArtImage> getImagesByTags(String tags, Pageable paging) {
        tags = tags.toLowerCase();
        List<String> temp = Arrays.stream(tags.split(",")).map(String::trim).toList();
        List<Tag> searchTags = temp.stream().map(tagRepository::getTagByName).toList();
        return fanArtImageRepository.findImagesByAllGivenTags(searchTags, (long) searchTags.size(), paging);
    }

    public Optional<FanArtImage> getById(int id) {
        return fanArtImageRepository.findById(id);
    }

    public void editImageData(FanArtImage image) {
        fanArtImageRepository.save(image);
    }
}
