package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author,Integer> {
}
