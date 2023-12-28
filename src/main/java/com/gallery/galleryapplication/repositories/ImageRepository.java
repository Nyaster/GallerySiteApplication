package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image,Integer> {
    Optional<Image> findImageByMediaId(int mediaId);
}
