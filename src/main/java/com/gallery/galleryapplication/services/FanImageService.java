package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.DDO.ImageSettings;
import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.models.enums.ImageType;
import com.gallery.galleryapplication.repositories.FanArtImageRepository;
import com.gallery.galleryapplication.repositories.TagRepository;
import com.gallery.galleryapplication.util.ImageImporter;
import com.gallery.galleryapplication.util.LessonInLoveDonwloader.RequestPageAnalyzer;
import com.gallery.galleryapplication.util.ThumbNailUtilities;
import com.gallery.galleryapplication.util.inMemoryVector.InMemoryVectorManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FanImageService {
    final TagRepository tagRepository;
    final RequestPageAnalyzer requestPageAnalyzer;
    final FanArtImageRepository fanArtImageRepository;
    final private ImageImporter imageImporter;
    final private ThumbNailUtilities thumbNailUtilities;
    final private InMemoryVectorManager inMemoryVectorManager;
    final private TagService tagService;

    public FanImageService(TagRepository tagRepository, RequestPageAnalyzer requestPageAnalyzer, FanArtImageRepository fanArtImageRepository, ImageImporter imageImporter, ThumbNailUtilities thumbNailUtilities, InMemoryVectorManager inMemoryVectorManager, TagService tagService) {
        this.tagRepository = tagRepository;
        this.requestPageAnalyzer = requestPageAnalyzer;
        this.fanArtImageRepository = fanArtImageRepository;
        this.imageImporter = imageImporter;
        this.thumbNailUtilities = thumbNailUtilities;
        this.inMemoryVectorManager = inMemoryVectorManager;
        this.tagService = tagService;
    }

    public void scanImagesFromFolder() {
        List<FanArtImage> images = requestPageAnalyzer.scanImagesFromFolder();
        if (!images.isEmpty()) {
            fanArtImageRepository.saveAll(images);
        }

    }

    public Page<FanArtImage> getAll(Pageable paging, boolean b) {
        Page<FanArtImage> all = fanArtImageRepository.findByIsVisibleTrue(paging);
        List<FanArtImage> thumbnailsAndUpdateImage = (List<FanArtImage>) thumbNailUtilities.createThumbnailsAndUpdateImage(all.getContent());
        fanArtImageRepository.saveAll(thumbnailsAndUpdateImage);
        all = fanArtImageRepository.findByIsVisibleTrue(paging);
        return all;
    }

    public Page<FanArtImage> getImagesByTags(String tags, Pageable paging) {
        tags = tags.toLowerCase();
        List<String> temp = Arrays.stream(tags.split(",")).map(String::trim).toList();
        List<Tag> searchTags = temp.stream().map(tagRepository::getTagByName).toList();
        Page<FanArtImage> all = fanArtImageRepository.findImagesByAllGivenTags(searchTags, (long) searchTags.size(), paging);
        List<FanArtImage> thumbnailsAndUpdateImage = (List<FanArtImage>) thumbNailUtilities.createThumbnailsAndUpdateImage(all.getContent());
        fanArtImageRepository.saveAll(thumbnailsAndUpdateImage);
        all = fanArtImageRepository.findByIsVisibleTrue(paging);
        return all;
    }

    public Optional<FanArtImage> getById(int id) {

        Optional<FanArtImage> byId = fanArtImageRepository.findById(id);
        return byId;
    }

    public void editImageData(FanArtImage image) {
        fanArtImageRepository.save(image);
    }

    public void importImages() {
        List<FanArtImage> images = imageImporter.parseImagesFromImportFolder();
        fanArtImageRepository.saveAll(images);
        images.forEach(x -> inMemoryVectorManager.putIntoVectorIntoDb(x.getId(), x.getEmbedding(), ImageType.fanimage));
        inMemoryVectorManager.saveDbState();

    }

    private void saveAll(List<FanArtImage> list) {
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
        HashSet<FanArtImage> entities = new HashSet<>(list);
        fanArtImageRepository.saveAll(entities);
    }

    public FanArtImage saveFanImage(FanArtImage fanArtImage) {
        fanArtImageRepository.save(fanArtImage);
        return fanArtImage;
    }

    public List<FanArtImage> getImagesWithoutTags() {
        return fanArtImageRepository.findByTagsNullOrTagsEmpty();
    }

    public List<FanArtImage> getAll() {
        return fanArtImageRepository.findByIsVisibleTrue();
    }
    public void changeImageSettings(ImageSettings imageSettings, Integer id) {
        Optional<FanArtImage> image = getById(id);
        if(imageSettings.getTags() != null){
            image.get().setTags(tagService.getTagsFromDDO(imageSettings.getTags()    ));
        }
        if (imageSettings.getChecked() != null){
            image.get().setVisible(imageSettings.getChecked());
        }
        editImageData(image.get());
    }

}
