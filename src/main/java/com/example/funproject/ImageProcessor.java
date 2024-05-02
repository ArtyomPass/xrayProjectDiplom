package com.example.funproject;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageProcessor {
    private final HelloController controller;
    protected ImageView imageView;
    protected TilePane thumbnailsTilePane;

    protected Image selectedImage;
    private ImageView selectedThumbnailView;
    protected Map<Image, double[]> imageViewStates = new HashMap<>();

    private final ImageUtils imageUtils;


    public ImageProcessor(HelloController controller, ImageView mainImageView, TilePane thumbnailsTilePane) {
        this.controller = controller;
        this.imageView = mainImageView;
        this.thumbnailsTilePane = thumbnailsTilePane;

        this.selectedThumbnailView = new ImageView();
        this.imageUtils = new ImageUtils(this, selectedThumbnailView);
    }

    public void putImagesAndButtonsOnTabPane(Map<Tab, List<Image>> images, Tab currentTab) {
        if (currentTab != null) {
            if (images != null) {
                thumbnailsTilePane.getChildren().clear();
                thumbnailsTilePane.setPrefColumns(1);
                thumbnailsTilePane.setVgap(10);
                thumbnailsTilePane.setHgap(10);

                List<Image> tabImages = images.getOrDefault(currentTab, new ArrayList<>());
                for (Image img : tabImages) {
                    imageUtils.createThumbnail(currentTab, img, thumbnailsTilePane, controller, imageViewStates);
                }
            }
            // Настраиваем зум, перетаскивание и кнопку сброса
            imageUtils.setupZoom(imageView, imageViewStates);
            imageUtils.setupImageDrag(imageView, imageViewStates);
        }
    }

    // Геттеры и сеттеры
    public ImageView getImageView() {
        return imageView;
    }

    public Image getSelectedImage() {
        return selectedImage;
    }
}