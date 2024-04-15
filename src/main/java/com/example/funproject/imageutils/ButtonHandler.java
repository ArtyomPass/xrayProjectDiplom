package com.example.funproject.imageutils;

import com.example.funproject.ImageProcessor;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonHandler {
    private final ImageProcessor imageProcessor;
    private boolean peakSelectionMode = false;
    private Map<Image, List<Line>> imageLines = new HashMap<>();
    private Button pickPeaksButton;

    public ButtonHandler(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
        this.pickPeaksButton = new Button("Отметить Пики");
        updateButtonStyle(); // Initialize the style of the button based on the mode
    }

    public void addButtonsBelowImageView(ScrollPane scrollPane) {
        Button resetButton = new Button("Сброс");
        resetButton.setOnAction(event -> resetImageViewSettings());
        pickPeaksButton.setOnAction(event -> togglePeakSelectionMode());
        HBox buttonBox = new HBox(10, pickPeaksButton, resetButton);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonBox.setMaxHeight(50);
        if (scrollPane.getParent() instanceof StackPane) {
            StackPane parentPane = (StackPane) scrollPane.getParent();
            parentPane.getChildren().add(buttonBox);
            StackPane.setAlignment(buttonBox, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(buttonBox, new Insets(10));
        }
    }

    private void resetImageViewSettings() {
        ImageView imageView = imageProcessor.getImageView();
        Image selectedImage = imageProcessor.getSelectedImage();
        if (imageView != null && selectedImage != null) {
            imageView.setTranslateX(0);
            imageView.setTranslateY(0);
            imageView.setScaleX(1);
            imageView.setScaleY(1);
            System.out.println("Состояние изображения сброшено.");
        }
    }

    private void togglePeakSelectionMode() {
        peakSelectionMode = !peakSelectionMode;
        updateButtonStyle();
        handleCursorChange();
    }

    private void updateButtonStyle() {
        if (peakSelectionMode) {
            pickPeaksButton.setText("Режим пиков: ВКЛ");
            pickPeaksButton.setStyle("-fx-background-color: lightgreen;");
        } else {
            pickPeaksButton.setText("Отметить Пики");
            pickPeaksButton.setStyle("");
        }
    }

    private void handleCursorChange() {
        ImageView imageView = imageProcessor.getImageView();
        if (imageView != null) {
            if (peakSelectionMode) {
                System.out.println("Выбран режим активации пиков курсором");
                imageView.setCursor(Cursor.CROSSHAIR);
                imageView.setOnMouseClicked(this::handlePeakSelectionClick);
            } else {
                imageView.setCursor(Cursor.DEFAULT);
                imageView.setOnMouseClicked(null);
            }
        }
    }

    private void handlePeakSelectionClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            ImageView imageView = imageProcessor.getImageView();
            System.out.println("Расчитанная позиция пика: x = " + event.getX());
            addPeakLine(event.getX(), imageView);

        } else if (event.getButton() == MouseButton.SECONDARY) {
            handleRightClickForDeletion(event);
        }
    }

    public void addPeakLine(double xPosition, ImageView imageView) {
        Line peakLine = new Line();
        peakLine.setStroke(Color.BLUE);
        peakLine.setStrokeWidth(2);

        final double finalXPosition = Math.max(0, Math.min(xPosition, imageView.getImage().getWidth())); // Ограничение в пределах изображения

        peakLine.startXProperty().bind(Bindings.createDoubleBinding(() -> {
            double minX = imageView.getBoundsInParent().getMinX();
            return minX + finalXPosition * imageView.getScaleX(); // Прямое применение масштаба к позиции
        }, imageView.boundsInParentProperty(), imageView.scaleXProperty()));

        peakLine.endXProperty().bind(peakLine.startXProperty());
        // Setup Y bindings as before

        // Привязки для Y координаты линии, чтобы она простирается от верха до низа видимой части изображения
        peakLine.startYProperty().bind(Bindings.createDoubleBinding(() -> {
            return imageView.getBoundsInParent().getMinY();
        }, imageView.boundsInParentProperty()));

        peakLine.endYProperty().bind(Bindings.createDoubleBinding(() -> {
            return imageView.getBoundsInParent().getMaxY();
        }, imageView.boundsInParentProperty()));

        // Добавление линии на изображение
        Pane imageContainer = (Pane) imageView.getParent();
        if (!imageContainer.getChildren().contains(peakLine)) {
            imageContainer.getChildren().add(peakLine);
        }

        // Сохранение линии в списке
        List<Line> lines = imageLines.computeIfAbsent(imageProcessor.getSelectedImage(), k -> new ArrayList<>());
        lines.add(peakLine);
    }




    private void handleRightClickForDeletion(MouseEvent event) {
        double xPosition = event.getX();
        List<Line> lines = imageLines.get(imageProcessor.getSelectedImage());
        lines.removeIf(line -> Math.abs(line.getStartX() - xPosition) < 5);
    }

    public void switchLinesVisibility(Image oldImage, Image newImage) {
        if (oldImage != null) {
            List<Line> oldLines = imageLines.getOrDefault(oldImage, new ArrayList<>());
            oldLines.forEach(line -> line.setVisible(false));
        }
        List<Line> newLines = imageLines.getOrDefault(newImage, new ArrayList<>());
        newLines.forEach(line -> line.setVisible(true));
    }

    public void clearLinesForImage(Image image) {
        List<Line> linesToRemove = imageLines.get(image);
        if (linesToRemove != null) {
            Pane imageContainer = (Pane) imageProcessor.getImageView().getParent();
            imageContainer.getChildren().removeAll(linesToRemove);
            imageLines.remove(image);
        }
    }
}