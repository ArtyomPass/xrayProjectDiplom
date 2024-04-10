package com.example.funproject;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageProcessor {

    private final HelloController controller;
    protected ImageView getImageView;
    protected Image selectedImage;
    private ImageView selectedThumbnailView;

    public ImageProcessor(HelloController controller) {
        this.controller = controller;
        System.out.println("Image processor is initialized" + this.controller);
    }

    public void putImagesOnTabPane(Map<Tab, List<Image>> images, Tab currentTab) {

        if (currentTab != null && currentTab.getContent() instanceof SplitPane mainSplitPane) {

            ScrollPane thumbnailsScrollPane = (ScrollPane) ((SplitPane) mainSplitPane.getItems().get(1)).getItems().get(1);
            TilePane thumbnailsTilePane = (TilePane) thumbnailsScrollPane.getContent();
            thumbnailsTilePane.getChildren().clear(); // Очистка предыдущих миниатюр

            thumbnailsTilePane.setPrefColumns(1);
            thumbnailsTilePane.setVgap(10);
            thumbnailsTilePane.setHgap(10);

            ScrollPane mainImageScrollPane = (ScrollPane) ((SplitPane) mainSplitPane.getItems().get(1)).getItems().get(0);
            // Поскольку mainImageView является единственным содержимым mainImageScrollPane, мы можем получить к нему доступ напрямую.
            ImageView mainImageView = (ImageView) mainImageScrollPane.getContent();
            this.getImageView = mainImageView;
            System.out.println(mainImageView);

            List<Image> tabImages = images.getOrDefault(currentTab, new ArrayList<>());
            for (Image img : tabImages) {
                ImageView thumbnailImageView = new ImageView(img);
                thumbnailImageView.setFitWidth(150);
                thumbnailImageView.setFitHeight(150);
                thumbnailImageView.setPreserveRatio(true);

                thumbnailImageView.setOnMouseClicked(event -> {
                    mainImageView.setImage(img);
                    selectedImage = img;
                    clearSelectedImage();
                    applySelectedEffect(thumbnailImageView);
                    this.selectedThumbnailView = thumbnailImageView;
                });

                Button deleteButton = new Button("\u274C");
                deleteButton.getStyleClass().add("button-delete");
                deleteButton.setOnAction(event -> {
                    List<Image> imageList = controller.xRayImages.get(currentTab);
                    if (imageList != null) {
                        imageList.remove(img);
                        mainImageView.setImage(null);
                        controller.xRayImages.put(currentTab, imageList);
                    }
                    putImagesOnTabPane(controller.xRayImages, currentTab);
                });
                deleteButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
                deleteButton.setMaxSize(20, 20);
                HBox hbox = new HBox(10); // 10 is space between elements in HBox
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.getChildren().addAll(thumbnailImageView, deleteButton);
                thumbnailsTilePane.getChildren().add(hbox);
            }
        }
    }

    // To apply DropShadow Effect
    private void applySelectedEffect(ImageView imageView) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.GREY);
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.7);
        imageView.setEffect(dropShadow);
    }

    private void clearSelectedImage() {
        if (selectedThumbnailView != null) {
            selectedThumbnailView.setEffect(null);
        }
    }

}
