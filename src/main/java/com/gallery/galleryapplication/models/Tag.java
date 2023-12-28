package com.gallery.galleryapplication.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private String name;
    @ManyToMany(mappedBy = "tags", cascade = CascadeType.ALL)
    private List<Image> images;

    public Tag(String val) {
        this.name = val;
    }

    public Tag() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static List<Tag> createTagsFromList(List<String> strings){
        List<Tag> tags = new ArrayList<>();
        for (String val:
             strings) {
            tags.add(new Tag(val));
        }
        return tags;
    }

}
