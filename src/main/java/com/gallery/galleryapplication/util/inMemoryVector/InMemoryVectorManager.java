package com.gallery.galleryapplication.util.inMemoryVector;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class InMemoryVectorManager {
    final private String DBNAME = "vectors.json";
    private Map<Integer, double[]> vectorDb;

    public void putIntoVectorIntoDb(Integer id, double[] vector) {
        if (vectorDb == null) {
            vectorDb = new HashMap<>();
        }
        vectorDb.put(id, vector);
    }

    public double[] getVectorFromDb(Integer id) {
        if (vectorDb == null) {
            readFromDisk();
        }
        if (vectorDb.containsKey(id)) {
            return vectorDb.get(id);
        }
        return null;
    }

    @Scheduled(fixedRate = 600000)
    public void saveDbState() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            objectMapper.writeValue(new File("." + File.separator + DBNAME), vectorDb);
//        } catch (IOException e) {
//            LoggerFactory.getLogger(this.getClass()).error("Error while trying to save file", e);
//        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("." + File.separator + DBNAME))) {
            oos.writeObject(vectorDb);
            System.out.println("Map saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromDisk() {
        Map<Integer, double[]> tempDB = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("." + File.separator + DBNAME))) {
            tempDB = (Map<Integer, double[]>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
       /* ObjectMapper objectMapper = new ObjectMapper();
        Map<Integer, double[]> tempDB = new HashMap<>();
        File file = new File("." + File.separator + DBNAME);
        if (file.exists()) {
            try {
                String s = Files.readString(file.toPath());
                Map<String, ArrayList> stringMap = objectMapper.readValue(s, Map.class);
                stringMap.forEach((key1, value) -> {
                    int key = Integer.parseInt(key1);
                    double[] array = new double[value.size()];
                    for (int i = 0; i < value.size();i++){
                        array[i] = (double) value.get(i);
                    }
                    tempDB.put(key, array);
                });
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).error("Error while trying to load file", e);
            }
        }*/
        this.vectorDb = tempDB;
    }

    ;
}
