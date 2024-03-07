package com.example.funproject;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class DataPreprocessing {

    public List<Image> preprocessImage(List<Image> originalImages) {
        List<Image> processedImages = new ArrayList<>();
        for (Image originalImage : originalImages) {
            int width = (int) originalImage.getWidth();
            int height = (int) originalImage.getHeight();
            WritableImage processedImage = new WritableImage(width, height);
            PixelReader pixelReader = originalImage.getPixelReader();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = pixelReader.getColor(x, y);
                    double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                    Color grayColor = new Color(gray, gray, gray, color.getOpacity());
                    processedImage.getPixelWriter().setColor(x, y, grayColor);
                }
            }
            processedImages.add(processedImage);
        }
        return processedImages;
    }


}
