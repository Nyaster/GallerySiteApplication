package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FanArtImageRepository extends JpaRepository<FanArtImage, Integer> {
    @Query("SELECT i FROM FanArtImage i JOIN i.tags t WHERE t IN :tags GROUP BY i HAVING COUNT(DISTINCT t) = :tagCount")
    Page<FanArtImage> findImagesByAllGivenTags(@Param("tags") List<Tag> tags, @Param("tagCount") Long tagCount, Pageable pageable);
}