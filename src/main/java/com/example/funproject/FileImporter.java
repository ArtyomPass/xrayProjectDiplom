package com.example.funproject;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class FileImporter {

    public List<Image> importImages(Window window) {
        List<Image> images = new ArrayList<>();
        FileChooser fileChooser = createFileChooser("Open X-ray Data File",
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {
            for (File file : files) {
                try {
                    images.add(new Image(new FileInputStream(file)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return images;
    }

    public File importTable(Window window) {
        FileChooser fileChooser = createFileChooser("Open Table Data File",
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        return fileChooser.showOpenDialog(window);
    }

    private FileChooser createFileChooser(String title, FileChooser.ExtensionFilter... filters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(filters);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        return fileChooser;
    }
}