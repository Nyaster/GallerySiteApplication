package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
}