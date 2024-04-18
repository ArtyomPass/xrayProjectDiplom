package com.example.funproject.imageutils;

import com.example.funproject.ImageProcessor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonHandler {

    private final ImageProcessor imageProcessor; // Ссылка на объект для обработки изображений
    private boolean peakSelectionMode = false; // Флаг режима выделения пиков
    private Map<Image, List<LineInfo>> imageLines = new HashMap<>(); // Сопоставление изображений с их линиями пиков
    private Button pickPeaksButton; // Кнопка для переключения режима выделения пиков
    ButtonFunctionsHandler buttonHandlerHelper; // Вспомогательный объект для обработки функций кнопок
    private ComboBox<String> lineSelectionComboBox; // Выпадающий список для выбора типа линии пика

    /**
     * Конструктор класса ButtonHandler.
     * Инициализирует обработчик кнопок и создает вспомогательный объект.
     *
     * @param imageProcessor - объект для обработки изображений
     */
    public ButtonHandler(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
        this.pickPeaksButton = new Button("Отметить Пики");

        // Создание выпадающего списка для выбора типа линии
        this.lineSelectionComboBox = new ComboBox<>();
        this.buttonHandlerHelper = new ButtonFunctionsHandler(imageProcessor, imageLines, peakSelectionMode, pickPeaksButton, lineSelectionComboBox);

    }

    /**
     * Добавляет кнопки "Сброс" и "Отметить пики" и другие под ImageView.
     *
     * @param scrollPane - ScrollPane, содержащий ImageView
     */
    public void addButtonsBelowImageView(ScrollPane scrollPane) {
        Button resetButton = new Button("Сброс");

        // Размещение кнопок в HBox
        HBox buttonBox = new HBox(10, lineSelectionComboBox, pickPeaksButton, resetButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonBox.setMaxHeight(50);

        lineSelectionComboBox.getItems().addAll("K-Alpha 1", "K-Alpha 2", "K-Alpha 5", "K-Beta 1", "K-Beta 2");
        lineSelectionComboBox.setPromptText("Выберите линию");

        resetButton.setOnAction(event -> buttonHandlerHelper.resetImageViewSettings(imageProcessor)); // Обработчик нажатия кнопки сброса
        pickPeaksButton.setOnAction(event -> buttonHandlerHelper.togglePeakSelectionModeAndHandleClicks()); // Обработчик нажатия кнопки "Отметить пики"

        // Добавление HBox в StackPane, содержащий ScrollPane
        if (scrollPane.getParent() instanceof StackPane) {
            StackPane parentPane = (StackPane) scrollPane.getParent();
            parentPane.getChildren().add(buttonBox);
            StackPane.setAlignment(buttonBox, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(buttonBox, new Insets(10));
        }
    }

    /**
     * Переключает видимость линий пиков при смене изображения.
     *
     * @param oldImage - предыдущее изображение
     * @param newImage - новое изображение
     */
    public void switchLinesVisibility(Image oldImage, Image newImage) {
        System.out.println("Testing by clicking");
        System.out.println(imageLines.get(oldImage).get(0));
        // Скрываем линии, связанные со старым изображением
        if (oldImage != null) {
            List<LineInfo> oldLines = imageLines.get(oldImage);
            if (oldLines != null) {
                for (LineInfo lineInfo : oldLines) {
                    lineInfo.getLine().setVisible(false);
                }
            }
        }
        // Показываем линии, связанные с новым изображением
        if (newImage != null) {
            List<LineInfo> newLines = imageLines.get(newImage);
            if (newLines != null) {
                for (LineInfo lineInfo : newLines) {
                    lineInfo.getLine().setVisible(true);
                }
            }
        }
    }
}