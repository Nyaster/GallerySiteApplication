package com.gallery.galleryapplication.util;

import com.gallery.galleryapplication.models.Interfaces.ThumbnailProvider;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
@Component
public class ThumbNailUtilities {
    public void createThumbnailAndUpdateImage(ThumbnailProvider image, Integer width, Integer height) throws IOException {
        Path filePath = Path.of(image.getPathToFileOnDisc());
        Path parentDirectory = filePath.getParent();
        if (parentDirectory == null) {
            LoggerFactory.getLogger(this.getClass()).error("Something get wrong while creating thumbnail");
        }

        String placeToSave = parentDirectory.toFile().getPath() + "Thumbnail" + File.separator + filePath.toFile().getName() + ".jpeg";
        File folder = new File(parentDirectory.toFile().getPath() + "Thumbnail");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File tempFile = new File(placeToSave);
        Thumbnails.of(new File(image.getPathToFileOnDisc())).size(width, height).outputFormat("jpeg").outputQuality(0.85f).toFile(tempFile);
        image.setPathToImageThumbnailOnDisc(placeToSave);
    }
}
