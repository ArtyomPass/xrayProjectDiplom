package com.example.funproject.imageutils;

import com.example.funproject.ImageProcessor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

public class ButtonUtils {
    /**
     * Создает кнопку удаления для изображения на указанной вкладке.
     *
     * @param img        - изображение для удаления
     * @param currentTab - текущая вкладка
     * @return кнопку удаления
     */
    public Button createDeleteButton(Image img, Tab currentTab, ImageProcessor imageProcessor) {
        Button deleteButton = new Button("\u274C"); // Символ крестика
        deleteButton.getStyleClass().add("button-delete"); // Добавляем стиль

        // Обработчик нажатия на кнопку удаления
        deleteButton.setOnAction(event -> {
            List<Image> imageList = imageProcessor.getController().xRayImages.get(currentTab); // Получаем список изображений для вкладки
            if (imageList != null) {
                imageList.remove(img); // Удаляем изображение из списка
                imageProcessor.getImageView().setImage(null); // Очищаем ImageView
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
    public void addButtonsBelowImageView(ScrollPane scrollPane,  imageProcessor) {
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
}
