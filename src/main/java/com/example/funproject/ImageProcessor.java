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

    private final HelloController controller; // Ссылка на контроллер для доступа к его переменным
    protected ImageView getImageView; // ImageView для отображения обрабатываемого изображения
    protected Image selectedImage; // Текущее выбранное изображение
    private ImageView selectedThumbnailView; // ImageView для отслеживания эффекта DropShadow на миниатюре

    // Сохраняем состояния масштабирования и позиции для каждого изображения (масштаб X, масштаб Y, сдвиг X, сдвиг Y)
    private final Map<Image, double[]> imageViewStates = new HashMap<>();

    public ImageProcessor(HelloController controller) {
        this.controller = controller;
        System.out.println("Image processor is initialized" + this.controller);
    }

    /**
     * Размещает изображения на вкладке.
     *
     * @param images     - карта, где ключ - вкладка, значение - список изображений для этой вкладки
     * @param currentTab - текущая активная вкладка
     */
    public void putImagesOnTabPane(Map<Tab, List<Image>> images, Tab currentTab) {
        if (currentTab != null && currentTab.getContent() instanceof SplitPane mainSplitPane) {
            // 1. Получаем элементы интерфейса
            ScrollPane thumbnailsScrollPane = (ScrollPane) ((SplitPane) mainSplitPane.getItems().get(1)).getItems().get(1); // Панель с миниатюрами
            TilePane thumbnailsTilePane = (TilePane) thumbnailsScrollPane.getContent(); // Контейнер для миниатюр
            ScrollPane mainImageScrollPane = (ScrollPane) ((SplitPane) mainSplitPane.getItems().get(1)).getItems().get(0); // Панель с основным изображением
            ImageView mainImageView = (ImageView) mainImageScrollPane.getContent(); // ImageView для основного изображения
            this.getImageView = mainImageView; // Сохраняем ссылку на ImageView

            // 2. Очищаем панель с миниатюрами и настраиваем ее
            thumbnailsTilePane.getChildren().clear();
            thumbnailsTilePane.setPrefColumns(1); // Одна колонка
            thumbnailsTilePane.setVgap(10); // Вертикальный отступ
            thumbnailsTilePane.setHgap(10); // Горизонтальный отступ

            // 3. Обрабатываем изображения и создаем миниатюры
            List<Image> tabImages = images.getOrDefault(currentTab, new ArrayList<>()); // Получаем список изображений для вкладки
            for (Image img : tabImages) {
                // Сохраняем начальное состояние для изображения, если его еще нет
                imageViewStates.putIfAbsent(img, new double[]{1.0, 1.0, 0.0, 0.0});

                ImageView thumbnailImageView = new ImageView(img); // Создаем ImageView для миниатюры
                thumbnailImageView.setFitWidth(150); // Ширина миниатюры
                thumbnailImageView.setFitHeight(150); // Высота миниатюры
                thumbnailImageView.setPreserveRatio(true); // Сохраняем пропорции

                // Обработчик клика по миниатюре
                thumbnailImageView.setOnMouseClicked(event -> {
                    handleThumbnailClick(img, thumbnailImageView);
                });

                // Создаем кнопку удаления и HBox для размещения миниатюры и кнопки
                Button deleteButton = createDeleteButton(img, currentTab);
                HBox hbox = new HBox(10);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.getChildren().addAll(thumbnailImageView, deleteButton);

                thumbnailsTilePane.getChildren().add(hbox); // Добавляем миниатюру и кнопку на панель
            }

            // 4. Настраиваем зум, перетаскивание и кнопку сброса для основного изображения
            setupZoom(this.getImageView);
            setupImageDrag(this.getImageView);
            addResetButton(mainImageScrollPane);
        }
    }

    // --- Вспомогательные методы ---

    /**
     * Обрабатывает клик по миниатюре.
     *
     * @param img               - изображение, соответствующее миниатюре
     * @param thumbnailImageView - ImageView миниатюры
     */
    private void handleThumbnailClick(Image img, ImageView thumbnailImageView) {
        // Восстанавливаем состояние масштабирования и позиции для изображения
        double[] state = imageViewStates.getOrDefault(img, new double[]{1.0, 1.0, 0.0, 0.0});
        getImageView.setScaleX(state[0]);
        getImageView.setScaleY(state[1]);
        getImageView.setTranslateX(state[2]);
        getImageView.setTranslateY(state[3]);
        getImageView.setImage(img); // Устанавливаем изображение в ImageView
        selectedImage = img; // Сохраняем выбранное изображение

        // Очищаем эффект DropShadow с предыдущей миниатюры
        clearSelectedImage();

        // Применяем эффект DropShadow к новой миниатюре
        applySelectedEffect(thumbnailImageView);
        this.selectedThumbnailView = thumbnailImageView; // Сохраняем ссылку на ImageView миниатюры
    }

    /**
     * Создает кнопку удаления для изображения.
     *
     * @param img       - изображение для удаления
     * @param currentTab - текущая вкладка
     * @return кнопку удаления
     */
    private Button createDeleteButton(Image img, Tab currentTab) {
        Button deleteButton = new Button("\u274C"); // Символ крестика
        deleteButton.getStyleClass().add("button-delete"); // Добавляем стиль

        // Обработчик нажатия на кнопку удаления
        deleteButton.setOnAction(event -> {
            List<Image> imageList = controller.xRayImages.get(currentTab); // Получаем список изображений для вкладки
            if (imageList != null) {
                imageList.remove(img); // Удаляем изображение из списка
                this.getImageView.setImage(null); // Очищаем ImageView
                controller.xRayImages.put(currentTab, imageList); // Обновляем список изображений для вкладки
            }
            putImagesOnTabPane(controller.xRayImages, currentTab); // Обновляем отображение изображений
        });

        // Настраиваем размеры кнопки
        deleteButton.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        deleteButton.setMaxSize(20, 20);

        return deleteButton;
    }

    /**
     * Добавляет кнопку сброса на панель с изображением.
     *
     * @param scrollPane - панель с изображением
     */
    private void addResetButton(ScrollPane scrollPane) {
        Button resetButton = new Button("RESET");
        resetButton.setOnAction(event -> {
            if (getImageView != null && selectedImage != null) {
                // Сбрасываем масштаб и позицию изображения
                getImageView.setTranslateX(0);
                getImageView.setTranslateY(0);
                getImageView.setScaleX(1);
                getImageView.setScaleY(1);

                // Обновляем состояние в imageViewStates для текущего выбранного изображения
                imageViewStates.put(selectedImage, new double[]{1.0, 1.0, 0.0, 0.0});
            }
        });

        resetButton.getStyleClass().add("reset-button"); // Добавляем стиль

        // Размещаем кнопку в правом нижнем углу панели
        if (scrollPane.getParent() instanceof StackPane) {
            StackPane parentPane = (StackPane) scrollPane.getParent();
            parentPane.getChildren().add(resetButton);
            StackPane.setAlignment(resetButton, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(resetButton, new Insets(10));
        }
    }

    /**
     * Настраивает зум для ImageView.
     *
     * @param imageView - ImageView для настройки зума
     */
    private void setupZoom(ImageView imageView) {
        imageView.setOnScroll(event -> {
            if (event.isControlDown() && imageView.getImage() != null) {
                double zoomFactor = 1.05; // Коэффициент масштабирования
                double deltaY = event.getDeltaY();
                if (deltaY < 0) {
                    zoomFactor = 2.0 - zoomFactor; // Уменьшаем масштаб
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

    /**
     * Настраивает перетаскивание для ImageView.
     *
     * @param imageView - ImageView для настройки перетаскивания
     */
    private void setupImageDrag(ImageView imageView) {
        final double[] xOffset = new double[1]; // Смещение по X
        final double[] yOffset = new double[1]; // Смещение по Y

        imageView.setOnMousePressed(event -> {
            if (event.isControlDown() && imageView.getImage() != null) {
                // Запоминаем начальные смещения
                xOffset[0] = imageView.getTranslateX() - event.getSceneX();
                yOffset[0] = imageView.getTranslateY() - event.getSceneY();
            }
        });

        imageView.setOnMouseDragged(event -> {
            if (event.isControlDown()) {
                // Перемещаем изображение
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

    /**
     * Применяет эффект DropShadow к ImageView.
     *
     * @param imageView - ImageView для применения эффекта
     */
    private void applySelectedEffect(ImageView imageView) {
        DropShadow dropShadow = new DropShadow(); // Создаем эффект тени
        dropShadow.setColor(Color.GREY); // Цвет тени
        dropShadow.setRadius(20); // Радиус размытия
        dropShadow.setSpread(0.7); // Распространение тени
        imageView.setEffect(dropShadow); // Применяем эффект к ImageView
    }

    /**
     * Очищает эффект DropShadow с ImageView.
     */
    private void clearSelectedImage() {
        if (selectedThumbnailView != null) {
            selectedThumbnailView.setEffect(null); // Удаляем эффект тени
        }
    }
}