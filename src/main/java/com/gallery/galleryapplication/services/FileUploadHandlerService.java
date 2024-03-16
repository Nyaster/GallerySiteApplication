package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.models.Person;
import com.gallery.galleryapplication.models.enums.ImageType;
import com.gallery.galleryapplication.security.PersonDetails;
import com.gallery.galleryapplication.util.ONNXRuntime;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.Date;

@Service
public class FileUploadHandlerService {
    private final ONNXRuntime onnxRuntime;
    private final FanImageService fanImageService;
    private final String FILE_DIR = "./imageFanArts";
    private final String UPLOAD_DIR = "./upload";

    public FileUploadHandlerService(ONNXRuntime onnxRuntime, FanImageService fanImageService) {
        this.onnxRuntime = onnxRuntime;
        this.fanImageService = fanImageService;
    }

    public File saveImageToDisk(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File serverFile = new File(directory.getCanonicalPath() + File.separator + fileName);
        multipartFile.transferTo(serverFile);
        return serverFile;
    }

    public Integer saveNewFile(MultipartFile[] multipartFiles, PersonDetails personDetails) throws IOException {
        File savedImageToDisk = saveImageToDisk(multipartFiles[0]);
        FanArtImage fanArtImage = new FanArtImage();
        Person person = personDetails.getPerson();
        fanArtImage.setCreationDate(new Date());
        String sha256Hash = getSha256Hash(savedImageToDisk);
        String extension = FilenameUtils.getExtension(savedImageToDisk.getPath());
        fanArtImage.setVisible(false);
        fanArtImage.setCreatedBy(person);
        fanArtImage.setSha256(sha256Hash);
        FanArtImage returnedModel = fanImageService.saveFanImage(fanArtImage);
        String newPathFile = FILE_DIR + File.separator + fanArtImage.getCreatedBy().getLogin() + File.separator + fanArtImage.getId() + "." + extension;
        new File(FILE_DIR + File.separator + fanArtImage.getCreatedBy().getLogin()).mkdirs();
        File renameTo = new File(newPathFile);
        if (savedImageToDisk.renameTo(renameTo)) {
            savedImageToDisk.delete();
        }
        renameTo.createNewFile();
        fanArtImage.setPathToFileOnDisc(renameTo.getCanonicalPath());
        fanImageService.saveFanImage(fanArtImage);
        onnxRuntime.generateEmbeddings(Collections.singletonList(returnedModel), ImageType.fanimage);
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
