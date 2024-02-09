package com.gallery.galleryapplication.models;

import com.gallery.galleryapplication.Controller.interfaces.imageInterface;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Objects;
@Entity
@Getter
@Setter
public class FanArtImage implements imageInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "Fan_Art_Image_Tag",
            joinColumns = @JoinColumn(name = "image_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private Author author;
    private String pathToFileOnDisc;
    private String pathToImageThumbnailOnDisc;
    @Transient
    private String tagsInString;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FanArtImage that = (FanArtImage) o;
        return id == that.id;
    }
    public int getMediaId(){
        return getId();
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getTagsInString() {
        return getTags().stream().map(x->x.getName()).reduce((x,y)->x+", "+y).orElse("");
    }
}