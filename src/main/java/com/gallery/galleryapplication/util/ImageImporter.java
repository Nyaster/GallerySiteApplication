package com.gallery.galleryapplication.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gallery.galleryapplication.models.Author;
import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.services.AuthorService;
import com.gallery.galleryapplication.util.inMemoryVector.InMemoryVectorManager;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ImageImporter {
    private final AuthorService authorService;
    private final InMemoryVectorManager inMemoryVectorManager;

    public ImageImporter(AuthorService authorService, InMemoryVectorManager inMemoryVectorManager) {
        this.authorService = authorService;
        this.inMemoryVectorManager = inMemoryVectorManager;
    }

    public List<FanArtImage> parseImagesFromImportFolder() {
        File importFolder = new File("./import");
        List<File> jsonToRead = Arrays.stream(importFolder.listFiles()).filter(x -> x.getName().endsWith("json")).toList();
        List<JsonNode> jsonNodeList = new ArrayList<>();
        for (File json :
                jsonToRead) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                var node = objectMapper.readTree(json);
                for (int i = 0; i < node.size(); i++) {
                    jsonNodeList.add(node.get(i));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<Author> authors = jsonNodeList.stream().map(x -> x.get("author").asText()).distinct().map(x -> {
            Author author = new Author();
            author.setName(x);
            return author;
        }).toList();
        authorService.addNewAuthors(authors);
        List<Author> allAuthor = authorService.findAll();
        List<FanArtImage> imageList = jsonNodeList.stream().map(x -> {
            FanArtImage image = new FanArtImage();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                image.setCreationDate(simpleDateFormat.parse(x.get("date").asText()));
            } catch (ParseException e) {
                LoggerFactory.getLogger(this.getClass()).error("Error while parsing date", e);
                throw new RuntimeException(e);
            }
            image.setAuthor(allAuthor.stream().filter(j -> j.getName().equalsIgnoreCase(x.get("author").asText())).findAny().get());
            image.setPathToFileOnDisc(x.get("file_path").asText());
            image.setTags(new ArrayList<>());
            float[] embeddings = convertJsonNodeArrayToDoubleArray(x.get("embedding").get(0));
            image.setEmbedding(embeddings);
            FanArtImage temp = moveFileToCorrectFolder(image);
            return temp;
        }).toList();
        return imageList;
    }

    public FanArtImage moveFileToCorrectFolder(FanArtImage image) {
        Path pathToFanFolder = Path.of("./imageFanArts/");
        if (image.getAuthor() == null) {
            pathToFanFolder = Path.of("./imageFanArts/");
            File file = pathToFanFolder.toFile();
            if (!file.exists()) {
                file.mkdir();
            }

        } else {
            pathToFanFolder = Path.of("./imageFanArts/" + image.getAuthor().getName());
            File file = pathToFanFolder.toFile();
            if (!file.exists()) {
                file.mkdir();
            }
        }
        try {
            Path source = Path.of("./import/" + image.getPathToFileOnDisc().substring(1));
            Path target = Path.of(pathToFanFolder.toFile().getPath() + "/" + source.getFileName());
            if (target.toFile().exists()) {
                image.setPathToFileOnDisc(target.toFile().getPath());
                return image;
            }
            Files.copy(source, target);

        } catch (IOException e) {
            LoggerFactory.getLogger(this.getClass()).error("Error while parsing date", e);
            throw new RuntimeException("fuck");
        }

        return image;
    }

    public float[] convertJsonNodeArrayToDoubleArray(JsonNode jsonNodes) {
        if (jsonNodes == null || jsonNodes.size() == 0) {
            return new float[0]; // Return empty array if input is null or empty
        }

        float[] doubleArray = new float[jsonNodes.size()];
        for (int i = 0; i < jsonNodes.size(); i++) {
            JsonNode node = jsonNodes.get(i);
            doubleArray[i] = node != null ? ((float) node.asDouble()) : 0.0F; // Set default value to 0.0 if node is null
        }

        return doubleArray;
    }
}
