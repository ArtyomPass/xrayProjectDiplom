package com.example.funproject;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

public class ImageUtils {
    private final ImageProcessor imageProcessor;
    private ImageView selectedThumbnailView;


    public ImageUtils(ImageProcessor imageProcessor, ImageView selectedThumbnailView) {
        this.imageProcessor = imageProcessor;
        this.selectedThumbnailView = selectedThumbnailView;
    }

    public void createThumbnail(Tab currentTab, Image img, TilePane thumbnailsTilePane,
                                HelloController controller, Map<Image, double[]> imageViewStates) {
        imageViewStates.putIfAbsent(img, new double[]{1.0, 1.0, 0.0, 0.0});
        ImageView thumbnailImageView = new ImageView(img);
        thumbnailImageView.setFitWidth(150);
        thumbnailImageView.setFitHeight(150);
        thumbnailImageView.setPreserveRatio(true);

        thumbnailImageView.setOnMouseClicked(
                event -> handleThumbnailClick(img,
                        thumbnailImageView,
                        controller,
                        imageViewStates));

        Button deleteButton = createDeleteButton(img, currentTab, controller);
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(thumbnailImageView, deleteButton);
        thumbnailsTilePane.getChildren().add(hbox);
    }

    public void handleThumbnailClick(Image img,
                                     ImageView thumbnailImageView,
                                     HelloController controller,
                                     Map<Image, double[]> imageViewStates) {

        // Получаем текущее выбранное изображение (предполагаем, что imageProcessor имеет поле selectedImage)
        Image currentImage = imageProcessor.selectedImage;

        // Устанавливаем новое изображение
        imageProcessor.selectedImage = img;
        imageProcessor.getImageView().setImage(img);

        // Восстанавливаем состояние масштабирования и позиции для нового изображения
        double[] state = imageViewStates.getOrDefault(img, new double[]{1.0, 1.0, 0.0, 0.0});
        ImageView imageView = imageProcessor.getImageView();
        imageView.setScaleX(state[0]);
        imageView.setScaleY(state[1]);
        imageView.setTranslateX(state[2]);
        imageView.setTranslateY(state[3]);

        // Управление видимостью линий (switchLinesVisibility принимает controller)
        if (currentImage != null && img != null) {
            switchLinesVisibility(currentImage, img, controller);
        }

        // Очищаем эффект выделения с предыдущей миниатюры
        clearSelectedImageEffect(this.selectedThumbnailView);

        // Применяем эффект выделения к текущей миниатюре
        applySelectedEffect(thumbnailImageView);
        this.selectedThumbnailView = thumbnailImageView; // Сохраняем текущий

    }

    public Button createDeleteButton(Image img,
                                     Tab currentTab,
                                     HelloController controller) {
        Button deleteButton = new Button("\u274C");
        deleteButton.getStyleClass().add("button-delete");
        deleteButton.setOnAction(event -> {
            List<Image> imageList = controller.xRayImages.get(currentTab);
            if (imageList != null) {
                imageList.remove(img);
                this.imageProcessor.getImageView().setImage(null);
                controller.xRayImages.put(currentTab, imageList);
            }
            imageProcessor.putImagesAndButtonsOnTabPane(controller.xRayImages, currentTab);
        });
        deleteButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        deleteButton.setMaxSize(20, 20);
        return deleteButton;
    }

    public void applySelectedEffect(ImageView imageView) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.GREY);
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.7);
        imageView.setEffect(dropShadow);
    }

    public void clearSelectedImageEffect(ImageView imageView) {
        if (imageView != null) {
            imageView.setEffect(null);
        }
    }

    public void switchLinesVisibility(Image oldImage,
                                      Image newImage,
                                      HelloController controller) {
        if (oldImage != null) {
            List<LineInfo> oldLines = controller.imageLines.get(oldImage);
            if (oldLines != null) {
                oldLines.forEach(lineInfo -> lineInfo.getLine().setVisible(false));
            }
        }
        if (newImage != null) {
            List<LineInfo> newLines = controller.imageLines.get(newImage);
            if (newLines != null) {
                newLines.forEach(lineInfo -> lineInfo.getLine().setVisible(true));
            }
        }
    }

    public void setupZoom(ImageView imageView, Map<Image, double[]> imageViewStates) {
        imageView.setOnScroll(event -> {
            if (event.isControlDown() && imageView.getImage() != null) {
                double zoomFactor = 1.05;
                double deltaY = event.getDeltaY();
                if (deltaY < 0) {
                    zoomFactor = 2.0 - zoomFactor;
                }
                imageView.setScaleX(imageView.getScaleX() * zoomFactor);
                imageView.setScaleY(imageView.getScaleY() * zoomFactor);
                if (imageProcessor.getSelectedImage() != null) {
                    imageViewStates.put(imageProcessor.getSelectedImage(), new double[]{
                            imageView.getScaleX(),
                            imageView.getScaleY(),
                            imageView.getTranslateX(),
                            imageView.getTranslateY()
                    });
                }
                event.consume();
            }
        });
    }

    public void setupImageDrag(ImageView imageView, Map<Image, double[]> imageViewStates) {
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        imageView.setOnMousePressed(event -> {
            if (event.isControlDown() && imageView.getImage() != null) {
                xOffset[0] = imageView.getTranslateX() - event.getSceneX();
                yOffset[0] = imageView.getTranslateY() - event.getSceneY();
            }
        });
        imageView.setOnMouseDragged(event -> {
            if (event.isControlDown()) {
                imageView.setTranslateX(event.getSceneX() + xOffset[0]);
                imageView.setTranslateY(event.getSceneY() + yOffset[0]);
                if (imageProcessor.getSelectedImage() != null) {
                    imageViewStates.put(imageProcessor.getSelectedImage(), new double[]{
                            imageView.getScaleX(),
                            imageView.getScaleY(),
                            imageView.getTranslateX(),
                            imageView.getTranslateY()
                    });
                }
            }
        });
    }
}