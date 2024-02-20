package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Interfaces.ThumbnailProvider;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.ImageRepository;
import com.gallery.galleryapplication.repositories.TagRepository;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import com.gallery.galleryapplication.util.ThumbNailUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImageService {
    public static final int WIDTH = 300;
    public static final int HEIGHT = 169;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final ThumbNailUtilities thumbNailUtilities;
    private final RequestPageAnalyzer requestPageAnalyzer;
    @Autowired
    public ImageService(ImageRepository imageRepository, TagRepository tagRepository, ThumbNailUtilities thumbNailUtilities, RequestPageAnalyzer requestPageAnalyzer) {
        this.imageRepository = imageRepository;
        this.tagRepository = tagRepository;
        this.thumbNailUtilities = thumbNailUtilities;
        this.requestPageAnalyzer = requestPageAnalyzer;
    }

    public Page<Image> imageList(Integer page) {
        return imageRepository.findAll(PageRequest.of(0, 20));
    }
    public List<Image> getAll() {
        return imageRepository.findAll();
    }
    public void editImageData(Image image){
        imageRepository.save(image);
    }
    @Transactional
    public void saveAll(List<Image> list) {
        Set<Tag> tags = list.stream().flatMap(x -> x.getTags().stream()).collect(Collectors.toSet());
        Set<Tag> tagsFromDb = new HashSet<>(tagRepository.findAll());
        tags.removeAll(tagsFromDb);
        tagRepository.saveAll(tags);
        tagsFromDb.addAll(tags);
        list.forEach(x -> {
            List<Tag> temp = x.getTags();
            List<Tag> replacedTags = new ArrayList<>(tagsFromDb);
            replacedTags.retainAll(temp);
            x.setTags(replacedTags);
        });
        HashSet<Image> entities = new HashSet<>(list);
        entities.parallelStream().forEach(x-> {
            try {
                thumbNailUtilities.createThumbnailAndUpdateImage(x,WIDTH,HEIGHT);
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).error("Error while generating thumbnails", e);
            }
            //x.setPathToImageThumbnailOnDisc(((ThumbnailProvider)x).getPathToImageThumbnailOnDisc());
        });
        imageRepository.saveAll(entities);
    }

    public Page<Image> getAll(Pageable paging, boolean nsfw) {
        Page<Image> pageable = null;
        if (nsfw) {
            pageable = imageRepository.findAll(paging);
        } else {
            pageable = imageRepository.findAllExcludingNSFW(paging);
        }
        return pageable;
    }

    public Page<Image> getImagesByTags(String tags, Pageable paging) {
        tags = tags.toLowerCase();
        List<String> temp = Arrays.stream(tags.split(",")).map(String::trim).toList();
        List<Tag> searchTags = temp.stream().map(tagRepository::getTagByName).toList();
        return imageRepository.findImagesByAllGivenTags(searchTags, (long) searchTags.size(), paging);
    }

    public Optional<Image> getByMediaId(int id) {
        return imageRepository.findImageByMediaId(id);
    }



    @Transactional
    public void update(int id, Image updatedImage) {
        updatedImage.setId(id);
        imageRepository.save(updatedImage);
    }

    public void createThumbnailsImagesForAllImages() {
        List<Image> images = imageRepository.findByPathToImageThumbnailOnDiscNull();
        images.parallelStream().forEach(x -> {
            try {
                thumbNailUtilities.createThumbnailAndUpdateImage(x, WIDTH, HEIGHT);
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).error("Something happened while generating thumbnails");
            }
        });
        imageRepository.saveAll(images);
    }

    public void analyzeRequestPages() {
        List<Image> images = requestPageAnalyzer.loginAndDownloadImages();
        saveAll(images);
    }



    public void checkUpdates() {
        try {
            List<Image> allImages = getAll();
            List<Image> images = requestPageAnalyzer.checkUpdates(allImages);
            images.removeAll(allImages);
            saveAll(images);
            createThumbnailsImagesForAllImages();
        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).error("Error while cheking update");
        }
    }
}
