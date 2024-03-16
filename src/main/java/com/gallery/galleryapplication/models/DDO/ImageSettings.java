package com.gallery.galleryapplication.models.DDO;

import com.gallery.galleryapplication.models.enums.TagGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ImageSettings {
    private Boolean checked;
    private List<TagsDDO> tags;
}