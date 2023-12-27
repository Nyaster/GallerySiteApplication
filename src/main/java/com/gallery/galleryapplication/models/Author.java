package com.gallery.galleryapplication.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
@Entity
@Table(name = "Author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @OneToOne()
    @JoinColumn(name = "person_id",referencedColumnName = "id")
    private Person person;
    @OneToMany(mappedBy = "author")
    private List<Image> images;
}
