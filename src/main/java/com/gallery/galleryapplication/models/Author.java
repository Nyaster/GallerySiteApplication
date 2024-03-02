package com.gallery.galleryapplication.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne()
    @JoinColumn(name = "person_id",referencedColumnName = "id")
    private Person person;
    @OneToMany(mappedBy = "author")
    private List<Image> images;
    @OneToMany(mappedBy = "author")
    private List<FanArtImage> fanImages;
    private String Name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Name.equalsIgnoreCase(author.Name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Name);
    }
}
