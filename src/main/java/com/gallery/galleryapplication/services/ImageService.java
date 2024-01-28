package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.ImageRepository;
import com.gallery.galleryapplication.repositories.TagRepository;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer.CURRENT_DIRECTORY;

@Service
@Transactional
public class ImageService {
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    @Autowired
    private RequestPageAnalyzer requestPageAnalyzer;

    @Autowired
    public ImageService(ImageRepository imageRepository, TagRepository tagRepository) {
        this.imageRepository = imageRepository;
        this.tagRepository = tagRepository;
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
        list.stream().forEach(x -> {
            List<Tag> temp = x.getTags();
            List<Tag> replacedTags = new ArrayList<>(tagsFromDb);
            replacedTags.retainAll(temp);
            x.setTags(replacedTags);
        });
        imageRepository.saveAll(new HashSet<>(list));
        imageRepository.flush();
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

    public void createThumbnailAndUpdateImage(Image image, Integer width, Integer height) throws IOException {

        String placeToSave = CURRENT_DIRECTORY + File.separator + "imageThumbnail" + File.separator + image.getMediaId() + ".jpeg";
        File folder = new File(CURRENT_DIRECTORY + File.separator + "imageThumbnail");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File tempFile = new File(placeToSave);
        Thumbnails.of(new File(image.getPathToFileOnDisc())).size(width, height).outputFormat("jpeg").outputQuality(0.85f).toFile(tempFile);
        image.setPathToImageThumbnailOnDisc(placeToSave);
    }

    @Transactional
    public void update(int id, Image updatedImage) {
        updatedImage.setId(id);
        imageRepository.save(updatedImage);
    }

    @PostConstruct
    public void createThumbnailsImagesForAllImages() {
        List<Image> images = imageRepository.findByPathToImageThumbnailOnDiscNull();
        images.parallelStream().forEach(x -> {
            try {
                createThumbnailAndUpdateImage(x, 300, 169);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        imageRepository.saveAll(images);
    }

    public void analyzeRequestPages() {
        requestPageAnalyzer.loginAndDownloadImages();
    }



    public void checkUpdates() {
        try {
            List<Image> allImages = getAll();
            List<Image> images = requestPageAnalyzer.checkUpdates(allImages);
            images.removeAll(allImages);
            saveAll(images);
            createThumbnailsImagesForAllImages();
        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).error("Error while chekigns update");
        }
    }
}
