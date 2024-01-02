package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.repositories.ImageRepository;
import com.gallery.galleryapplication.repositories.TagRepository;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer.CURRENT_DIRECTORY;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    @Autowired
    public ImageService(ImageRepository imageRepository, TagRepository tagRepository, RequestPageAnalyzer requestPageAnalyzer) {
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

    public Page<Image> getAll(Pageable paging, boolean nsfw) {
        Page<Image> pageable = null;
        if (nsfw){
            pageable = imageRepository.findAll(paging);
        }else {
            pageable = imageRepository.findAllExcludingNSFW(paging);
        }
        return pageable;
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
    public void createThumbnailAndUpdateImage(Image image,Integer width, Integer height) throws IOException {

        String placeToSave = CURRENT_DIRECTORY + File.separator +"imageThumbnail"+File.separator+image.getMediaId()+".jpeg";
        File folder = new File(CURRENT_DIRECTORY + File.separator + "imageThumbnail");
        if (!folder.exists()){
            folder.mkdir();
        }
        File tempFile = new File(placeToSave);
        Thumbnails.of(new File(image.getPathToFileOnDisc()))
                .size(width,height)
                .outputFormat("jpeg")
                .outputQuality(0.85f).toFile(tempFile);
        image.setPathToImageThumbnailOnDisc(placeToSave);
    }
    @PostConstruct
    public void createThumbnailsImagesForAllImages(){
        List<Image> images= imageRepository.findByPathToImageThumbnailOnDiscNull();
        images.parallelStream().forEach(x-> {
            try {
                createThumbnailAndUpdateImage(x,300,169);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        imageRepository.saveAll(images);
    }
}
