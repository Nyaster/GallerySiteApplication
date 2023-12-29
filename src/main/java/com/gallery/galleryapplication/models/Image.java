package com.gallery.galleryapplication.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Setter
@Getter
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "Image_Tag",
            joinColumns = @JoinColumn(name = "image_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
    @Getter
    @Column(unique = true)
    private int mediaId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private Author author;
    private String pathToFileOnDisc;

}
