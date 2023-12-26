package com.gallery.galleryapplication.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;
    @OneToOne()
    @JoinColumn(name = "person_id",referencedColumnName = "id")
    private Person person;
    @OneToMany(mappedBy = "image")
    private List<Image> images;
}
