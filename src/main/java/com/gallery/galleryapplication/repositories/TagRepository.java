package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag,Integer> {
    boolean existsByName(String name);
    Tag getTagByName(String name);
}
