package com.example.funproject;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FileImporter {

    public List<Image> importData(Window window) {
        List<Image> images = new ArrayList<>();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open X-ray Data File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {
            for (File file : files) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    Image image = new Image(fileInputStream);
                    images.add(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace(); // Handle the exception as appropriate
                }
            }
        }
        return images;
    }
}
