package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Person;
import com.gallery.galleryapplication.security.PersonDetails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Date;

@Service
public class FileUploadHandlerService {
    private final FanImageService fanImageService;
    private final String FILE_DIR = "./imageFanArts";

    public FileUploadHandlerService(FanImageService fanImageService) {
        this.fanImageService = fanImageService;
    }

    public Integer saveNewFile(File file, PersonDetails personDetails) {
        FanArtImage fanArtImage = new FanArtImage();
        Person person = personDetails.getPerson();
        fanArtImage.setCreationDate(new Date());
        String sha256Hash = getSha256Hash(file);
        String extension = FilenameUtils.getExtension(file.getAbsolutePath());
        String newPathFile = FILE_DIR + File.separator + sha256Hash + "." + extension;
        File renameTo = new File(newPathFile);
        boolean b = file.renameTo(renameTo);
        if (b){
            file.delete();
        }
        fanArtImage.setPathToFileOnDisc(renameTo.getPath());
        fanArtImage.setVisible(false);
        fanArtImage.setCreatedBy(person);
        fanArtImage.setSha256(sha256Hash);

        FanArtImage returnedModel = fanImageService.saveFanImage(fanArtImage);
        return returnedModel.getId();
    }

    ;

    private String getSha256Hash(File file) {
        MessageDigest instance = null;
        byte[] hash = new byte[0];
        try {
            instance = MessageDigest.getInstance("SHA-256");
            hash = instance.digest(Files.readAllBytes(file.toPath()));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Provider[] providers = Security.getProviders();
        return new BigInteger(1, hash).toString(16);
    }
}
