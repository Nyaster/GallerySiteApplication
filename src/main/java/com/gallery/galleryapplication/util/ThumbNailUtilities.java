package com.gallery.galleryapplication.util;

import com.gallery.galleryapplication.models.Interfaces.ThumbnailProvider;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
public class ThumbNailUtilities {
    final int WIDTH = 560;
    final int HEIGHT = 315;
    public String createThumbnailAndUpdateImage(ThumbnailProvider image, Integer width, Integer height) throws IOException {
        Path filePath = Path.of(image.getPathToFileOnDisc());
        Path parentDirectory = filePath.getParent();
        if (parentDirectory == null) {
            LoggerFactory.getLogger(this.getClass()).error("Something get wrong while creating thumbnail");
        }

        String placeToSave = parentDirectory.toFile().getPath() + "Thumbnail" + File.separator + filePath.toFile().getName();
        File folder = new File(parentDirectory.toFile().getPath() + "Thumbnail");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File tempFile = new File(placeToSave);
        Thumbnails.of(Path.of(image.getPathToFileOnDisc()).toFile()).size(width, height).outputFormat("webp").outputQuality(1f).toFile(tempFile);
        return placeToSave;
    }
    public List<?extends ThumbnailProvider> createThumbnailsAndUpdateImage(List<? extends ThumbnailProvider> thumbnailProviders){
        thumbnailProviders.parallelStream().forEach(x-> {
            try {
                if (x.getPathToImageThumbnailOnDisc() == null || x.getPathToImageThumbnailOnDisc().isEmpty()){
                    x.setPathToImageThumbnailOnDisc(createThumbnailAndUpdateImage(x,WIDTH,HEIGHT));
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Error while creating thumbnail",e);
            }
        });
        return thumbnailProviders;
    }
}
