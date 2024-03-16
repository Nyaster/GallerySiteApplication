package com.gallery.galleryapplication.models;

import com.gallery.galleryapplication.models.Interfaces.ImageProvider;
import com.gallery.galleryapplication.models.Interfaces.ThumbnailProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.annotation.Cacheable;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class FanArtImage implements ThumbnailProvider, ImageProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "Fan_Art_Image_Tag", joinColumns = @JoinColumn(name = "image_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn()
    private Author author;
    private String pathToFileOnDisc;
    private String pathToImageThumbnailOnDisc;
    @Transient
    private String tagsInString;
    @Transient
    private float[] embedding;
    @Transient
    private Double TemporalCousineSimiliraty;
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Person createdBy;
    private boolean isVisible;
    private String sha256;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FanArtImage that = (FanArtImage) o;
        return id == that.id;
    }

    public int getMediaId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getTagsInString() {
        return getTags().stream().map(Tag::getName).reduce((x, y) -> x + ", " + y).orElse("");
    }
}
