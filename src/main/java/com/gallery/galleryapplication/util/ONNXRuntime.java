package com.gallery.galleryapplication.util;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.gallery.galleryapplication.models.FanArtImage;
import com.gallery.galleryapplication.util.inMemoryVector.InMemoryVectorManager;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class ONNXRuntime {
    public static final int SIZE = 448;
    private final String PATH_TO_MODEL = "./model/model.onnx";
    private final InMemoryVectorManager inMemoryVectorManager;

    public ONNXRuntime(InMemoryVectorManager inMemoryVectorManager) {
        this.inMemoryVectorManager = inMemoryVectorManager;
    }

    public void generateEmbeedings(List<FanArtImage> all) {
        OrtEnvironment ortEnvironment = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        if (!new File(PATH_TO_MODEL).exists()) {
            LoggerFactory.getLogger(this.getClass()).info("Please add model");
            return;
        }

        try {
            OrtSession session = ortEnvironment.createSession(PATH_TO_MODEL);
            all.stream().filter(y -> inMemoryVectorManager.getVectorFromDb(y.getId()) == null).forEach(x -> {
                inMemoryVectorManager.putIntoVectorIntoDb(x.getId(), evaluateEmbedding(x, ortEnvironment, session));
            });
            inMemoryVectorManager.saveDbState();
            session.close();
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }

    }

    private float[] evaluateEmbedding(FanArtImage image, OrtEnvironment ortEnvironment, OrtSession session) {
        float[] output = new float[0];
        try {
            BufferedImage imageToData = resizeImage(image);
            float[][][][] toModelFormatImage = getToModelFormatImage(imageToData);
            OrtSession.Result result = session.run(Collections.singletonMap("input", OnnxTensor.createTensor(ortEnvironment, toModelFormatImage)));
            float[][] outputScores = (float[][]) result.get(0).getValue();
            return outputScores[0];
        } catch (IOException | OrtException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage resizeImage(FanArtImage image) throws IOException {
        BufferedImage resizedImg = Thumbnails.of(new File(image.getPathToFileOnDisc())) .size(SIZE, SIZE).keepAspectRatio(true).asBufferedImage();
        BufferedImage finalImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = finalImage.createGraphics();
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, SIZE, SIZE);
        int x = (SIZE - resizedImg.getWidth()) / 2;
        int y = (SIZE - resizedImg.getHeight()) / 2;
        graphics.drawImage(resizedImg, x, y, null);
        graphics.dispose();
        return finalImage;
    }

    private float[][][][] getToModelFormatImage(BufferedImage image) {
        float[][][][] result = new float[1][SIZE][SIZE][3];
        for (int x = 0; x < image.getHeight(); x++) {
            for (int y = 0; y < image.getWidth(); y++) {
                Color color = new Color(image.getRGB(x, y));
                result[0][y][x][2] = color.getRed();
                result[0][y][x][1] = color.getGreen();
                result[0][y][x][0] = color.getBlue();
            }
        }
        return result;
    }
}
