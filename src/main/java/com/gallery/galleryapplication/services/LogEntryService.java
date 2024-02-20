package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.LogEntry;
import com.gallery.galleryapplication.repositories.LogEntryRepository;
import org.springframework.stereotype.Service;

@Service
public class LogEntryService {
    final LogEntryRepository logEntryRepository;

    public LogEntryService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public void saveLog(LogEntry logEntry){
        logEntryRepository.save(logEntry);
    }
}
