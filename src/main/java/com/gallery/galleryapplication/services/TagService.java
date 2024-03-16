package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.DDO.TagsDDO;
import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.models.enums.TagGroup;
import com.gallery.galleryapplication.repositories.TagRepository;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional
@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public void addNewTag(Tag tag) {
        if (!tagRepository.existsByName(tag.getName())) {
            tagRepository.save(tag);
        }
    }

    @Transactional
    public void addNewTags(List<Tag> tag) {
        tagRepository.saveAll(tag);
    }

    public List<Tag> findTagsByNameLikeIgnoreCase(String tag) {
        return tagRepository.findTagsByNameContainsIgnoreCase(tag);
    }

    public String getAllInStrin() {
        return tagRepository.findAll().stream().map(Tag::getName).reduce((x, y) -> x + "," + y).orElse("");
    }

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public List<Tag> getImagesWithDesiredGroup(TagGroup group) {
        return tagRepository.getTagsByTagGroup(group);
    }

    public List<Tag> getTagsFromDDO(List<TagsDDO> tags) throws RuntimeException {
        List<Tag> tagsSet = tags.stream().map(x -> {
            Tag tag = new Tag();
            tag.setName(x.getValue().toLowerCase());
            return tag;

        }).collect(Collectors.toList());
        List<Tag> newOriginalTags = new ArrayList<>(tagsSet);
        if (newOriginalTags.stream().anyMatch(x -> x.getName().length() > 50)) {
            throw new RuntimeException("Error in length tags");
        }
        List<Tag> allTags = getAll();
        tagsSet.removeAll(allTags);
        if (!tagsSet.isEmpty()) {
            addNewTags(tagsSet);
        }
        try {
            newOriginalTags.forEach(x -> {
                Tag tag = allTags.stream().filter(j -> j.getName().equalsIgnoreCase(x.getName())).findAny().orElseThrow();
                x.setTagGroup(tag.getTagGroup());
                x.setId(tag.getId());
                x.setName(x.getName());
            });
        } catch (NoSuchElementException e) {
            LoggerFactory.getLogger(this.getClass().getName()).info("No tags found", e);
        }
        return newOriginalTags;

    }
}
