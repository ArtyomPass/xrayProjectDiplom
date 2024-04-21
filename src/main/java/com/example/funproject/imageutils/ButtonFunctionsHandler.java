package com.example.funproject.imageutils;

import com.example.funproject.ImageProcessor;
import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonFunctionsHandler {

    private ImageProcessor imageProcessor;
    private Map<Image, List<LineInfo>> imageLines;
    private boolean peakSelectionMode;
    private Button pickPeaksButton;
    private ComboBox<String> lineSelectionComboBox;
    private TextField elementInput;

    public ButtonFunctionsHandler(ImageProcessor imageProcessor,
                                  Map<Image, List<LineInfo>> imageLines,
                                  boolean peakSelectionMode,
                                  Button pickPeaksButton,
                                  ComboBox<String> lineSelectionComboBox, TextField elementInput) {
        this.imageProcessor = imageProcessor;
        this.imageLines = imageLines;
        this.peakSelectionMode = peakSelectionMode;
        this.pickPeaksButton = pickPeaksButton;
        this.lineSelectionComboBox = lineSelectionComboBox;
        this.elementInput = elementInput;

    }

    /**
     * Сбрасывает настройки ImageView (масштаб и позицию) к значениям по умолчанию.
     *
     * @param imageProcessor - объект для обработки изображений
     */
    protected void resetImageViewSettings(ImageProcessor imageProcessor) {
        ImageView imageView = imageProcessor.getImageView();
        Image selectedImage = imageProcessor.getSelectedImage();

        if (imageView != null && selectedImage != null) {
            imageView.setTranslateX(0);
            imageView.setTranslateY(0);
            imageView.setScaleX(1);
            imageView.setScaleY(1);

            // Обновление состояния изображения в хранилище состояний
            Map<Image, double[]> currentStates = imageProcessor.getImageViewStates();
            currentStates.put(selectedImage, new double[]{1, 1, 0, 0});
            imageProcessor.setImageViewStates(currentStates);

            System.out.println("Состояние изображения сброшено.");
        }
    }

    /**
     * Переключает режим выделения пиков и обрабатывает клики мыши в этом режиме.
     *
     * @return новое состояние режима
     */
    protected void togglePeakSelectionModeAndHandleClicks() {
        this.peakSelectionMode = !peakSelectionMode;
        updateButtonStyle(peakSelectionMode); // Обновление стиля кнопки
        ImageView imageView = imageProcessor.getImageView();

        if (imageView != null) {
            if (peakSelectionMode) {
                System.out.println("Выбран режим активации пиков курсором");
                imageView.setCursor(Cursor.CROSSHAIR); // Изменение курсора мыши

                // Установка обработчика кликов для добавления/удаления линий
                imageView.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) { // Левый клик - добавление линии
                        double xCoordinate = event.getX();
                        System.out.println("Координата для getX (сам imageView): x = " + xCoordinate);
                        addPeakLine(xCoordinate, imageView);
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        // TODO: Обработка правого клика - возможно, удаление линии
                    }
                });
            } else {
                imageView.setCursor(Cursor.DEFAULT);
                imageView.setOnMouseClicked(null); // Удаление обработчика кликов
            }
        }
    }

    /**
     * Добавляет линию для отметки пика на изображении.
     *
     * @param xCrossHair - координата X щелчка мыши
     * @param imageView  - объект ImageView, на котором нужно добавить линию
     */
    public void addPeakLine(double xCrossHair, ImageView imageView) {
        Line peakLine = new Line();
        peakLine.setStroke(Color.BLUE);
        peakLine.setStrokeWidth(2);

        // Ограничение координаты X в пределах изображения
        final double finalXPosition = Math.max(0, Math.min(xCrossHair, imageView.getImage().getWidth()));

        // Привязка startX линии к координате X щелчка с учетом масштабирования
        peakLine.startXProperty().bind(Bindings.createDoubleBinding(() -> {
            double minX = imageView.getBoundsInParent().getMinX();
            return minX + finalXPosition * imageView.getScaleX();
        }, imageView.boundsInParentProperty(), imageView.scaleXProperty()));

        // endX совпадает с startX, чтобы линия была вертикальной
        peakLine.endXProperty().bind(peakLine.startXProperty());

        // Привязка startY и endY линии к верхней и нижней границам изображения
        peakLine.startYProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMinY(), imageView.boundsInParentProperty()));
        peakLine.endYProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMaxY(), imageView.boundsInParentProperty()));

        // Добавление линии на изображение
        Pane imageContainer = (Pane) imageView.getParent();
        if (!imageContainer.getChildren().contains(peakLine)) {
            imageContainer.getChildren().add(peakLine);
        }

        String peakType = lineSelectionComboBox.getValue();
        String elementName = elementInput.getText();
        LineInfo lineInfo = new LineInfo(peakLine, finalXPosition, peakType, elementName);
        List<LineInfo> linesForImage = imageLines.computeIfAbsent(imageProcessor.getSelectedImage(), k -> new ArrayList<>());
        linesForImage.add(lineInfo);

    }

    /**
     * Обновляет стиль кнопки "Отметить пики" в зависимости от режима.
     *
     * @param peakSelectionMode - текущее состояние режима (включен/выключен)
     */
    protected void updateButtonStyle(boolean peakSelectionMode) {
        if (peakSelectionMode) {
            pickPeaksButton.setText("Режим пиков: ВКЛ");
            pickPeaksButton.setStyle("-fx-background-color: lightgreen;");
        } else {
            pickPeaksButton.setText("Отметить Пики");
            pickPeaksButton.setStyle("");
        }
    }
}