package com.gallery.galleryapplication.models;

import com.gallery.galleryapplication.models.Interfaces.ImageProvider;
import com.gallery.galleryapplication.models.Interfaces.ThumbnailProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.cdi.Eager;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
@Cacheable("images")
public class Image implements ThumbnailProvider, ImageProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},fetch = FetchType.EAGER)
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
    @Getter
    @Setter
    private String pathToImageThumbnailOnDisc;
    @Transient
    private String tagsInString;
    @Transient
    private double[] embedding;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return mediaId == image.mediaId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaId);
    }

    public String getTagsInString() {
        return getTags().stream().map(Tag::getName).reduce((x, y)->x+", "+y).orElse("");
    }
}
