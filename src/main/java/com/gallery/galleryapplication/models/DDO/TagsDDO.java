package com.gallery.galleryapplication.models.DDO;

import com.gallery.galleryapplication.models.enums.TagGroup;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TagsDDO {
    private String value;
    private TagGroup tagGroup;
    private int id;
}