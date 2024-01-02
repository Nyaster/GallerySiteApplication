package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.Image;
import com.gallery.galleryapplication.models.Tag;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ImageRepository extends JpaRepository<Image,Integer> {
    Optional<Image> findImageByMediaId(int mediaId);
    @Query("SELECT i FROM Image i JOIN i.tags t WHERE t IN :tags GROUP BY i HAVING COUNT(DISTINCT t) = :tagCount")
    Page<Image> findImagesByAllGivenTags(@Param("tags") List<Tag> tags, @Param("tagCount") Long tagCount, Pageable pageable);
    @Query("SELECT DISTINCT i FROM Image i LEFT JOIN i.tags t WHERE t.name != 'nsfw'")
    Page<Image> findAllExcludingNSFW(Pageable pageable);

    List<Image> findByPathToImageThumbnailOnDiscNull();
}
