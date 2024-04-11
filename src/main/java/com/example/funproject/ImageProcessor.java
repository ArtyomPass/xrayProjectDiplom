package com.example.funproject;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageProcessor {

    private final HelloController controller; // Ссылка на контроллер, используется для получения доступа к переменным контроллера
    protected ImageView getImageView; // ImageView для отображения обрабатываемого изображения.
    protected Image selectedImage; // Текущее выбранное изображение.
    private ImageView selectedThumbnailView; // ImageView для отслеживания применения эффекта DropShadow к выбранному миниатюрному изображению.
    private final Map<Image, double[]> imageViewStates = new HashMap<>(); // Состояния масштабирования и позиции для каждого изображения.

    /**
     * Конструктор класса ImageProcessor.
     *
     * @param controller Контроллер приложения, через который осуществляется доступ к данным.
     */
    public ImageProcessor(HelloController controller) {
        this.controller = controller;
        System.out.println("Image processor is initialized" + this.controller);
    }

    /**
     * Размещает изображения на панели вкладок.
     *
     * @param images Словарь, связывающий каждую вкладку с списком изображений для отображения.
     * @param currentTab Текущая вкладка, на которой будут отображаться изображения.
     */
    public void putImagesOnTabPane(Map<Tab, List<Image>> images, Tab currentTab) {
        if (currentTab != null && currentTab.getContent() instanceof SplitPane mainSplitPane) {
            ScrollPane thumbnailsScrollPane = (ScrollPane) ((SplitPane) mainSplitPane.getItems().get(1)).getItems().get(1);
            TilePane thumbnailsTilePane = (TilePane) thumbnailsScrollPane.getContent();

            thumbnailsTilePane.getChildren().clear(); // Clearing previous thumbnails
            thumbnailsTilePane.setPrefColumns(1);
            thumbnailsTilePane.setVgap(10);
            thumbnailsTilePane.setHgap(10);

            ScrollPane mainImageScrollPane = (ScrollPane) ((SplitPane) mainSplitPane.getItems().get(1)).getItems().get(0);
            ImageView mainImageView = (ImageView) mainImageScrollPane.getContent();
            this.getImageView = mainImageView; // Ensure this.getImageView is always the current ImageView

            List<Image> tabImages = images.getOrDefault(currentTab, new ArrayList<>());
            for (Image img : tabImages) {
                imageViewStates.putIfAbsent(img, new double[]{1.0, 1.0, 0.0, 0.0});
                ImageView thumbnailImageView = new ImageView(img);
                thumbnailImageView.setFitWidth(150);
                thumbnailImageView.setFitHeight(150);
                thumbnailImageView.setPreserveRatio(true);

                thumbnailImageView.setOnMouseClicked(event -> {
                    double[] state = imageViewStates.getOrDefault(img, new double[]{1.0, 1.0, 0.0, 0.0}); // Получаем состояние для img
                    getImageView.setScaleX(state[0]);
                    getImageView.setScaleY(state[1]);
                    getImageView.setTranslateX(state[2]);
                    getImageView.setTranslateY(state[3]);

                    getImageView.setImage(img); // Установка выбранного изображения
                    selectedImage = img; // Обновление текущего выбранного изображения
                    clearSelectedImage(); // Очистка эффекта для предыдущего выбранного миниатюрного изображения
                    applySelectedEffect(thumbnailImageView); // Применение эффекта к текущему выбранному миниатюрному изображению
                    this.selectedThumbnailView = thumbnailImageView; // Обновление ссылки на текущее выбранное миниатюрное изображение
                });


                Button deleteButton = new Button("\u274C");
                deleteButton.getStyleClass().add("button-delete");
                deleteButton.setOnAction(event -> {
                    List<Image> imageList = controller.xRayImages.get(currentTab);
                    if (imageList != null) {
                        imageList.remove(img);
                        this.getImageView.setImage(null);
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
            // Call zoom and drag setup methods outside the loop to ensure they're set up only once for the mainImageView.
            setupZoom(this.getImageView);
            setupImageDrag(this.getImageView);
            addResetButton(mainImageScrollPane);
        }
    }


    private void addResetButton(ScrollPane scrollPane) {
        Button resetButton = new Button("RESET");
        resetButton.setOnAction(event -> {
            if (getImageView != null && selectedImage != null) {
                getImageView.setTranslateX(0);
                getImageView.setTranslateY(0);
                getImageView.setScaleX(1);
                getImageView.setScaleY(1);

                // Обновляем состояние в imageViewStates для текущего выбранного изображения
                imageViewStates.put(selectedImage, new double[]{1.0, 1.0, 0.0, 0.0});
            }
        });

        resetButton.getStyleClass().add("reset-button");
        if (scrollPane.getParent() instanceof StackPane) {
            StackPane parentPane = (StackPane) scrollPane.getParent();
            parentPane.getChildren().add(resetButton);
            StackPane.setAlignment(resetButton, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(resetButton, new Insets(10));
        }
    }


    //function for setup zoom for ImageView
    private void setupZoom(ImageView imageView) {
        imageView.setOnScroll(event -> {
            if (event.isControlDown() && imageView.getImage() != null) {
                double zoomFactor = 1.05;
                double deltaY = event.getDeltaY();
                if (deltaY < 0) {
                    zoomFactor = 2.0 - zoomFactor;
                }

                imageView.setScaleX(imageView.getScaleX() * zoomFactor);
                imageView.setScaleY(imageView.getScaleY() * zoomFactor);

                // Обновляем состояние для текущего изображения
                if(selectedImage != null) {
                    imageViewStates.put(selectedImage, new double[]{
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


    //function for setup dragging for ImageView
    private void setupImageDrag(ImageView imageView) {
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

                // Обновляем состояние для текущего изображения
                if(selectedImage != null) {
                    imageViewStates.put(selectedImage, new double[]{
                            imageView.getScaleX(),
                            imageView.getScaleY(),
                            imageView.getTranslateX(),
                            imageView.getTranslateY()
                    });
                }
            }
        });
    }

    // To apply DropShadow Effect
    private void applySelectedEffect(ImageView imageView) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.GREY);
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.7);
        imageView.setEffect(dropShadow);
    }

    // To clear DropShadow
    private void clearSelectedImage() {
        if (selectedThumbnailView != null) {
            selectedThumbnailView.setEffect(null);
        }
    }

}
