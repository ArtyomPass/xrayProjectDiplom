package com.example.funproject.imageutils;

import com.example.funproject.HelloController;
import com.example.funproject.ImageProcessor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

public class ImageUtils {

    private ImageProcessor imageProcessor;
    private ButtonHandler buttonHandler;
    private HelloController controller;

    public ImageUtils(ImageProcessor imageProcessor, ButtonHandler buttonHandler, HelloController controller) {
        this.imageProcessor = imageProcessor;
        this.buttonHandler = buttonHandler;
        this.controller = controller;
    }

    /**
     * Создает кнопку удаления для изображения на указанной вкладке.
     *
     * @param img        - изображение для удаления
     * @param currentTab - текущая вкладка
     * @return кнопку удаления
     */
    public Button createDeleteButton(Image img, Tab currentTab) {
        Button deleteButton = new Button("\u274C"); // Символ крестика
        deleteButton.getStyleClass().add("button-delete"); // Добавляем стиль

        // Обработчик нажатия на кнопку удаления
        deleteButton.setOnAction(event -> {
            List<Image> imageList = imageProcessor.getController().xRayImages.get(currentTab); // Получаем список изображений для вкладки
            if (imageList != null) {
                imageList.remove(img); // Удаляем изображение из списка
                this.imageProcessor.getImageView().setImage(null); // Очищаем ImageView
                imageProcessor.getController().xRayImages.put(currentTab, imageList); // Обновляем список изображений для вкладки
            }
            imageProcessor.putImagesOnTabPane(imageProcessor.getController().xRayImages, currentTab); // Обновляем отображение изображений
        });

        // Настраиваем размеры кнопки
        deleteButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        deleteButton.setMaxSize(20, 20);
        return deleteButton;
    }

    /**
     * Настраивает зум для ImageView с помощью прокрутки мыши при нажатой клавише Ctrl.
     *
     * @param imageView - ImageView для настройки зума
     */
    public void setupZoom(ImageView imageView) {
        imageView.setOnScroll(event -> {
            if (event.isControlDown() && imageView.getImage() != null) {
                double zoomFactor = 1.05;
                double deltaY = event.getDeltaY();
                if (deltaY < 0) {
                    zoomFactor = 2.0 - zoomFactor;
                }
                imageView.setScaleX(imageView.getScaleX() * zoomFactor);
                imageView.setScaleY(imageView.getScaleY() * zoomFactor);

                // Сохраняем состояние масштабирования в imageViewStates
                if (imageProcessor.getSelectedImage() != null) {
                    Map<Image, double[]> states = imageProcessor.getImageViewStates();
                    states.put(imageProcessor.getSelectedImage(), new double[]{
                            imageView.getScaleX(),
                            imageView.getScaleY(),
                            imageView.getTranslateX(),
                            imageView.getTranslateY()
                    });
                    imageProcessor.setImageViewStates(states);
                    System.out.println("Zoom Updated: ScaleX=" + imageView.getScaleX() + ", ScaleY=" + imageView.getScaleY());
                }
                event.consume();
            }
        });
    }

    /**
     * Настраивает перетаскивание для ImageView с помощью перетаскивания мыши при нажатой клавише Ctrl.
     *
     * @param imageView - ImageView для настройки перетаскивания
     */
    public void setupImageDrag(ImageView imageView) {
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

                // Сохраняем состояние позиции в imageViewStates
                if (imageProcessor.getSelectedImage() != null) {
                    Map<Image, double[]> states = imageProcessor.getImageViewStates();
                    states.put(imageProcessor.getSelectedImage(), new double[]{
                            imageView.getScaleX(),
                            imageView.getScaleY(),
                            imageView.getTranslateX(),
                            imageView.getTranslateY()
                    });
                    imageProcessor.setImageViewStates(states);
                    System.out.println("Drag Updated: TranslateX=" + imageView.getTranslateX() + ", TranslateY=" + imageView.getTranslateY());
                }
            }
        });
    }

    /**
     * Создает миниатюру изображения и добавляет ее на панель миниатюр.
     *
     * @param img                - изображение для создания миниатюры
     * @param currentTab         - текущая вкладка
     * @param thumbnailsTilePane - панель для размещения миниатюр
     */
    public void createThumbnail(Image img, Tab currentTab, TilePane thumbnailsTilePane) {
        // Получаем текущие состояния изображений и проверяем, существует ли уже состояние для этого изображения
        Map<Image, double[]> currentStates = imageProcessor.getImageViewStates();
        currentStates.putIfAbsent(img, new double[]{1.0, 1.0, 0.0, 0.0}); // Добавляем состояние, если его нет

        // Обновляем состояния через сеттер
        imageProcessor.setImageViewStates(currentStates);

        ImageView thumbnailImageView = new ImageView(img);
        thumbnailImageView.setFitWidth(150);
        thumbnailImageView.setFitHeight(150);
        thumbnailImageView.setPreserveRatio(true);

        // Обработчик клика по миниатюре
        thumbnailImageView.setOnMouseClicked(event -> {
            handleThumbnailClick(img, thumbnailImageView);
        });

        // Создаем кнопку удаления и HBox для размещения миниатюры и кнопки
        Button deleteButton = createDeleteButton(img, currentTab);
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(thumbnailImageView, deleteButton);
        thumbnailsTilePane.getChildren().add(hbox);
    }

    /**
     * Обрабатывает нажатие на миниатюру изображения.
     * Восстанавливает состояние масштабирования и позицию для изображения,
     * устанавливает изображение в ImageView и применяет эффект выделения к миниатюре.
     *
     * @param img                - выбранное изображение
     * @param thumbnailImageView - ImageView миниатюры
     */
    public void handleThumbnailClick(Image img, ImageView thumbnailImageView) {

        // Сохраняем текущее выбранное изображение
        Image currentImage = imageProcessor.getSelectedImage();

        // Установка нового изображения
        imageProcessor.setSelectedImage(img);
        imageProcessor.getImageView().setImage(img);

        // Восстанавливаем состояние масштабирования и позиции для нового изображения
        Map<Image, double[]> imageViewStates = imageProcessor.getImageViewStates();
        double[] state = imageViewStates.getOrDefault(img, new double[]{1.0, 1.0, 0.0, 0.0});
        ImageView imageView = imageProcessor.getImageView();
        imageView.setScaleX(state[0]);
        imageView.setScaleY(state[1]);
        imageView.setTranslateX(state[2]);
        imageView.setTranslateY(state[3]);

        // Управление видимостью линий через ButtonHandler
        if (buttonHandler != null && currentImage != null && img != null) {
            switchLinesVisibility(currentImage, img);
        }

        // Очищаем эффект выделения с предыдущей миниатюры
        ImageView previouslySelectedThumbnail = imageProcessor.getSelectedThumbnailView();
        if (previouslySelectedThumbnail != null) {
            clearSelectedImageEffect(previouslySelectedThumbnail);
        }

        // Применяем эффект выделения к текущей миниатюре
        applySelectedEffect(thumbnailImageView);
        imageProcessor.setSelectedThumbnailView(thumbnailImageView);
    }

    /**
     * Применяет эффект тени (DropShadow) к ImageView.
     *
     * @param imageView - ImageView для применения эффекта
     */
    public void applySelectedEffect(ImageView imageView) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.GREY);
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.7);
        imageView.setEffect(dropShadow);
    }

    /**
     * Очищает эффект тени (DropShadow) с ImageView.
     */
    public void clearSelectedImageEffect(ImageView imageView) {
        if (imageView != null) {
            imageView.setEffect(null);
        }
    }

    /**
     * Переключает видимость линий пиков при смене изображения.
     *
     * @param oldImage - предыдущее изображение
     * @param newImage - новое изображение
     */
    public void switchLinesVisibility(Image oldImage, Image newImage) {
        // Скрываем линии, связанные со старым изображением
        if (oldImage != null) {
            List<LineInfo> oldLines = controller.getImageLines().get(oldImage);
            if (oldLines != null) {
                for (LineInfo lineInfo : oldLines) {
                    lineInfo.getLine().setVisible(false);
                }
            }
        }
        // Показываем линии, связанные с новым изображением
        if (newImage != null) {
            List<LineInfo> newLines = controller.getImageLines().get(newImage);
            if (newLines != null) {
                for (LineInfo lineInfo : newLines) {
                    lineInfo.getLine().setVisible(true);
                }
            }
        }
    }
}