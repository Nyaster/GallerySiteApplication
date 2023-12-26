package com.gallery.galleryapplication.models;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity()
@Table(name = "Image")
public class Image {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @ManyToMany
    @JoinTable(
            name = "Image_Tag",
            joinColumns = @JoinColumn(name = "image_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
    @Column(name = "media_id")
    private int mediaId;
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @ManyToOne()
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private Author author;

}
