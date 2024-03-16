package com.gallery.galleryapplication.util.inMemoryVector;

import com.gallery.galleryapplication.models.enums.ImageType;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class InMemoryVectorManager {
    final private String DBNAME = "vectors.json";
    final private String ORIGINAL_DBNAME = "imagedb.json";
    private Map<Integer, float[]> vectorDb;
    private Map<Integer, float[]> normalImageDb;

    public void putIntoVectorIntoDb(Integer id, float[] vector, ImageType imageType) {
        if (vectorDb == null) {
            vectorDb = new HashMap<>();
        }
        if (normalImageDb == null) {
            normalImageDb = new HashMap<>();
        }
        switch (imageType){
            case image -> normalImageDb.put(id,vector);
            case fanimage -> vectorDb.put(id,vector);
        }
    }

    public float[] getVectorFromDb(Integer id, ImageType imageType) {
        if (vectorDb == null) {
            readFromDisk();
        }
        if (normalImageDb == null) {
            readFromDisk();
        }
        return switch (imageType) {
            case fanimage -> vectorDb.getOrDefault(id, null);
            case image -> normalImageDb.getOrDefault(id, null);
        };
    }

    @Scheduled(fixedRate = 600000)
    public void saveDbState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("." + File.separator + DBNAME))) {
            oos.writeObject(vectorDb);
            System.out.println("Map saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("." + File.separator + ORIGINAL_DBNAME))) {
            oos.writeObject(normalImageDb);
            System.out.println("Map saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromDisk() {
        Map<Integer, float[]> tempDB = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("." + File.separator + DBNAME))) {
            tempDB = (Map<Integer, float[]>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LoggerFactory.getLogger(this.getClass()).info("File not found, create new File");
        }
        Map<Integer, float[]> normaldb = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("." + File.separator + ORIGINAL_DBNAME))) {
            normaldb = (Map<Integer, float[]>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LoggerFactory.getLogger(this.getClass()).info("File not found, create new File");
        }
        this.normalImageDb = normaldb;
        this.vectorDb = tempDB;
    }

    ;
}
