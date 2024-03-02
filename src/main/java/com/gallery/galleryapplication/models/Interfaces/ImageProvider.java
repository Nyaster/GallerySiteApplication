package com.gallery.galleryapplication.models.Interfaces;

import com.gallery.galleryapplication.models.Tag;

import java.util.List;

public interface ImageProvider {
    public int getMediaId();
    public String getTagsInString();
    public List<Tag> getTags();
    public double[] getEmbedding();
}
