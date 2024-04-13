package com.example.funproject.imageutils;

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

    public ImageUtils(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    /**
     * Создает миниатюру изображения и добавляет ее на панель миниатюр.
     *
     * @param img            - изображение для создания миниатюры
     * @param currentTab      - текущая вкладка
     * @param thumbnailsTilePane - панель для размещения миниатюр
     */
    public void createThumbnail(Image img, Tab currentTab, TilePane thumbnailsTilePane) {
        // Получаем текущие состояния изображений и проверяем, существует ли уже состояние для этого изображения
        Map<Image, double[]> currentStates = imageProcessor.getImageViewStates();
        currentStates.putIfAbsent(img, new double[]{1.0, 1.0, 0.0, 0.0}); // Добавляем состояние, если его нет

        // Теперь обновляем состояния через сеттер
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
     * @param img               - выбранное изображение
     * @param thumbnailImageView - ImageView миниатюры
     */
    public void handleThumbnailClick(Image img, ImageView thumbnailImageView) {
        // Восстанавливаем состояние масштабирования и позиции для изображения
        Map<Image, double[]> imageViewStates = imageProcessor.getImageViewStates();
        double[] state = imageViewStates.getOrDefault(img, new double[]{1.0, 1.0, 0.0, 0.0});
        ImageView imageView = imageProcessor.getImageView();
        imageView.setScaleX(state[0]);
        imageView.setScaleY(state[1]);
        imageView.setTranslateX(state[2]);
        imageView.setTranslateY(state[3]);
        imageView.setImage(img);
        imageProcessor.setSelectedImage(img);

        // Очищаем эффект выделения с предыдущей миниатюры
        ImageView previouslySelectedThumbnail = imageProcessor.getSelectedThumbnailView();
        if (previouslySelectedThumbnail != null) {
            clearSelectedImageEffect(previouslySelectedThumbnail);
            System.out.println("DropShadow cleared");
        }
        // Применяем эффект выделения к текущей миниатюре
        applySelectedEffect(thumbnailImageView);
        imageProcessor.setSelectedThumbnailView(thumbnailImageView);
    }

    /**
     * Создает кнопку удаления для изображения на указанной вкладке.
     *
     * @param img       - изображение для удаления
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
     * Добавляет кнопки сброса и выбора пиков на панель с изображением.
     *
     * @param scrollPane - панель с изображением
     */
    public void addButtonsBelowImageView(ScrollPane scrollPane) {
        // Кнопка сброса масштаба и позиции изображения
        Button resetButton = new Button("Сброс");
        resetButton.setOnAction(event -> {
            ImageView imageView = imageProcessor.getImageView();
            Image selectedImage = imageProcessor.getSelectedImage();
            if (imageView != null && selectedImage != null) {
                // Сбрасываем масштаб и позицию изображения
                imageView.setTranslateX(0);
                imageView.setTranslateY(0);
                imageView.setScaleX(1);
                imageView.setScaleY(1);

                // Создаем новый объект состояний с сброшенными значениями
                Map<Image, double[]> newStates = imageProcessor.getImageViewStates();
                newStates.put(selectedImage, new double[]{1.0, 1.0, 0.0, 0.0});
                imageProcessor.setImageViewStates(newStates); // Обновляем состояние через сеттер
                System.out.println("Состояние изображения сброшено.");
            }
        });

        // Кнопка для выбора пиков (пока не реализована)
        Button pickPeaksButton = new Button("Отметить Пики");
        pickPeaksButton.setOnAction(event -> {
            System.out.println("Режим выбора пиков активирован");
        });

        // Добавляем стили кнопкам
        resetButton.getStyleClass().add("reset-button");
        pickPeaksButton.getStyleClass().add("button-pick-peaks");

        // Используем HBox для горизонтального размещения кнопок
        HBox buttonBox = new HBox(10, pickPeaksButton, resetButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonBox.setMaxHeight(50);

        // Размещаем HBox в правом нижнем углу панели
        if (scrollPane.getParent() instanceof StackPane) {
            StackPane parentPane = (StackPane) scrollPane.getParent();
            parentPane.getChildren().add(buttonBox);
            StackPane.setAlignment(buttonBox, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(buttonBox, new Insets(10));
        }
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
}