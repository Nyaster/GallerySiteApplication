package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.Tag;
import com.gallery.galleryapplication.models.enums.TagGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag,Integer> {
    boolean existsByName(String name);
    Tag getTagByName(String name);
    List<Tag> findAllByName(String name);

    List<Tag> findTagsByNameStartsWith(String name);
    List<Tag> findTagsByNameContainsIgnoreCase(String name);
    List<Tag> getTagsByTagGroup(TagGroup tagGroup);
}
