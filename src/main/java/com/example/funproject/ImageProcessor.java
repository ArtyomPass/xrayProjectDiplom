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
    protected Image selectedImage;
    private ImageView selectedThumbnailView;
    private Map<Image, double[]> imageViewStates = new HashMap<>();

    private final ImageUtils imageUtils;
    protected final ImageButtonsHandler buttonHandler;

    public ImageProcessor(HelloController controller) {
        this.controller = controller;
        this.selectedThumbnailView = new ImageView();
        this.buttonHandler = new ImageButtonsHandler(this, controller.imageLines);
        this.imageUtils = new ImageUtils(this, selectedThumbnailView);
    }

    public void putImagesAndButtonsOnTabPane(Map<Tab, List<Image>> images, Tab currentTab) {
        if (currentTab != null && currentTab.getContent() instanceof SplitPane mainSplitPane) {

            // Получаем элементы интерфейса
            TilePane thumbnailsTilePane = (TilePane) getScrollPaneFromSplitPane(mainSplitPane, 1, 1).getContent();
            this.imageView = (ImageView) getScrollPaneFromSplitPane(mainSplitPane, 1, 0).getContent();

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

            // Настраиваем зум, перетаскивание и кнопку сброса c другими кнопками
            imageUtils.setupZoom(imageView, imageViewStates);
            imageUtils.setupImageDrag(imageView, imageViewStates);
            buttonHandler.addButtonsBelowImageView(getScrollPaneFromSplitPane(mainSplitPane, 1, 0), imageViewStates);
        }
    }

    private ScrollPane getScrollPaneFromSplitPane(SplitPane splitPane, int splitPaneIndex, int itemIndex) {
        return (ScrollPane) ((SplitPane) splitPane.getItems().get(splitPaneIndex)).getItems().get(itemIndex);
    }

    // Геттеры и сеттеры
    public ImageView getImageView() {
        return imageView;
    }

    public Image getSelectedImage() {
        return selectedImage;
    }
}