package com.gallery.galleryapplication.models.Interfaces;

import com.gallery.galleryapplication.models.Person;
import com.gallery.galleryapplication.models.Tag;

import java.util.Date;
import java.util.List;

public interface ImageProvider {
    public int getMediaId();

    public String getTagsInString();

    public List<Tag> getTags();

    public float[] getEmbedding();

    public String getPathToFileOnDisc();

    public boolean isVisible();

    public Date getCreationDate();

    public Person getCreatedBy();

    public Double getTemporalCousineSimiliraty();

    public void setTemporalCousineSimiliraty(Double cousineSimiliraty);
}