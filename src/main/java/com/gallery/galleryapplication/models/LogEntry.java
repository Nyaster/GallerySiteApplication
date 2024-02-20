package com.gallery.galleryapplication.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String logText;
    private String userName;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateTime;
    private String ip;
    private String role;
}
