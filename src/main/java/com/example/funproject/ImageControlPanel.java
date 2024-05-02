package com.example.funproject;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageControlPanel extends HBox {

    private final ImageProcessor imageProcessor;
    private final Map<Image, List<LineInfo>> imageLines;
    private List<Line> peakLines = new ArrayList<>();
    private final TabPane innerTabPane;
    private final ComboBox<String> lineSelectionComboBox;
    private final TextField elementInput;
    private final Button pickPeaksButton;
    private final Button resetButton;
    private boolean peakSelectionMode = false;

    public ImageControlPanel(HelloController controller, ImageProcessor imageProcessor,
                             ImageView mainImageView, TabPane innerTabPane) {
        this.imageProcessor = imageProcessor;
        this.imageLines = controller.imageLines;
        this.innerTabPane = innerTabPane;

        // Инициализация элементов управления
        lineSelectionComboBox = new ComboBox<>();
        elementInput = new TextField();
        pickPeaksButton = new Button("Отметить Пики");
        resetButton = new Button("Сброс");

        // Настройка ComboBox
        lineSelectionComboBox.getItems().addAll("K-Alpha 1", "K-Alpha 2", "K-Beta 1", "K-Beta 2");
        lineSelectionComboBox.setPromptText("Выберите линию");

        // Настройка TextField
        elementInput.setPromptText("Название элемента");

        // Обработчики событий
        resetButton.setOnAction(event -> handleResetButtonClick(mainImageView));
        pickPeaksButton.setOnAction(event -> togglePeakSelectionModeAndHandleClicks());

        // Добавление элементов в панель
        getChildren().addAll(elementInput, lineSelectionComboBox, pickPeaksButton, resetButton);

        // Дополнительные настройки
        setSpacing(10);
        setAlignment(Pos.CENTER);
    }

    // Обработчик нажатия кнопки сброса
    private void handleResetButtonClick(ImageView imageView) {
        Image selectedImage = imageProcessor.selectedImage;
        if (imageView != null && selectedImage != null) {
            imageView.setTranslateX(0);
            imageView.setTranslateY(0);
            imageView.setScaleX(1);
            imageView.setScaleY(1);
            imageProcessor.imageViewStates.put(selectedImage, new double[]{1, 1, 0, 0});
        }
    }

    private void togglePeakSelectionModeAndHandleClicks() {
        peakSelectionMode = !peakSelectionMode;
        updateButtonStyle();

        LineChart<Number, Number> lineChart = getActiveLineChart();
        ImageView imageView = imageProcessor.imageView;

        if (peakSelectionMode) {
            setupPeakSelectionMode(lineChart, imageView);
        } else {
            disablePeakSelectionMode(lineChart, imageView);
        }
    }

    private void setupPeakSelectionMode(LineChart<Number, Number> lineChart, ImageView imageView) {
        if (lineChart != null) {
            lineChart.setCursor(Cursor.CROSSHAIR);
            lineChart.setOnMouseClicked(event -> handleLineChartClick(event, lineChart));
        }

        if (imageView != null) {
            imageView.setCursor(Cursor.CROSSHAIR);
            imageView.setOnMouseClicked(event -> handleImageViewClick(event, imageView));
        }
    }

    private void disablePeakSelectionMode(LineChart<Number, Number> lineChart, ImageView imageView) {
        if (lineChart != null) {
            lineChart.setCursor(Cursor.DEFAULT);
            lineChart.setOnMouseClicked(null);
        }

        if (imageView != null) {
            imageView.setCursor(Cursor.DEFAULT);
            imageView.setOnMouseClicked(null);
        }
    }

    private void handleLineChartClick(MouseEvent event, LineChart<Number, Number> lineChart) {
        if (event.getButton() == MouseButton.PRIMARY) {
            double mouseXValue = lineChart.getXAxis().sceneToLocal(event.getSceneX(), 0).getX();
            if (!Double.isNaN(mouseXValue)) {
                Number xValue = lineChart.getXAxis().getValueForDisplay(mouseXValue);
                NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                double minY = yAxis.getLowerBound();
                double maxY = yAxis.getUpperBound();

                // Создаем серию данных для вертикальной линии
                XYChart.Series<Number, Number> verticalLineSeries = new XYChart.Series<>();
                verticalLineSeries.setName("Vertical Line");

                // Создаем точки данных для начала и конца линии
                verticalLineSeries.getData().add(new XYChart.Data<>(xValue, minY));
                verticalLineSeries.getData().add(new XYChart.Data<>(xValue, maxY));

                // Добавляем серию данных к LineChart перед стилизацией
                lineChart.getData().add(verticalLineSeries);
            }
        }
    }

    private void handleImageViewClick(javafx.scene.input.MouseEvent event, ImageView imageView) {
        if (event.getButton() == MouseButton.PRIMARY) {
            addPeakLine(event.getX(), imageView);
        }
    }

    // Добавляет линию для отметки пика на изображении
    public void addPeakLine(double xClick, ImageView imageView) {
        Line peakLine = new Line();
        peakLine.setStroke(Color.BLUE);
        peakLine.setStrokeWidth(2);
        final double finalXPosition = Math.max(0, Math.min(xClick, imageView.getImage().getWidth()));

        // Привязка startX линии к координате X щелчка с учетом масштабирования
        peakLine.startXProperty().bind(Bindings.createDoubleBinding(() -> {
            double minX = imageView.getBoundsInParent().getMinX();
            return minX + finalXPosition * imageView.getScaleX();
        }, imageView.boundsInParentProperty(), imageView.scaleXProperty()));

        // endX совпадает с startX, чтобы линия была вертикальной
        peakLine.endXProperty().bind(peakLine.startXProperty());

        // Привязка startY и endY линии к верхней и нижней границам изображения
        peakLine.startYProperty().bind(Bindings.createDoubleBinding(() ->
                        imageView.getBoundsInParent().getMinY(),
                imageView.boundsInParentProperty()));
        peakLine.endYProperty().bind(Bindings.createDoubleBinding(() ->
                        imageView.getBoundsInParent().getMaxY(),
                imageView.boundsInParentProperty()));

        // Добавление линии на изображение
        Pane imageContainer = (Pane) imageView.getParent();
        if (!imageContainer.getChildren().contains(peakLine)) {
            imageContainer.getChildren().add(peakLine);
        }
        imageLines.computeIfAbsent(imageProcessor.getSelectedImage(), k -> new ArrayList<>())
                .add(new LineInfo(peakLine, finalXPosition, lineSelectionComboBox.getValue(), elementInput.getText()));
    }

    // Получение LineChart из активной вкладки
    private LineChart<Number, Number> getActiveLineChart() {
        if (innerTabPane != null) {
            Tab selectedTab = innerTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null && selectedTab.getContent() instanceof LineChart) {
                return (LineChart<Number, Number>) selectedTab.getContent();
            }
        }
        return null;
    }

    // Обновляет стиль кнопки "Отметить пики" в зависимости от режима
    private void updateButtonStyle() {
        if (peakSelectionMode) {
            pickPeaksButton.setText("Режим пиков: ВКЛ");
            pickPeaksButton.setStyle("-fx-background-color: lightgreen;");
        } else {
            pickPeaksButton.setText("Отметить Пики");
            pickPeaksButton.setStyle("");
        }
    }
}