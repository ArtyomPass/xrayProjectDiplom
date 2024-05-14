package com.example.funproject;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
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
    private final TabPane innerTabPane;

    private final Map<Image, List<LineInfo>> imageLines;
    private final Map<Tab, List<LineInfo>> chartLines;

    // Элементы управления
    private final ComboBox<String> lineSelectionComboBox;
    private final TextField elementInput;
    private final Button pickPeaksButton;
    private final Button resetButton;

    // Флаг режима выбора пиков
    private boolean peakSelectionMode = false;

    public ImageControlPanel(HelloController controller, ImageProcessor imageProcessor,
                             ImageView mainImageView, TabPane innerTabPane) {
        this.imageProcessor = imageProcessor;
        this.imageLines = controller.imageLines;
        this.chartLines = controller.chartLines;
        this.innerTabPane = innerTabPane;

        // Инициализация элементов управления
        lineSelectionComboBox = new ComboBox<>();
        elementInput = new TextField();
        pickPeaksButton = new Button("Отметить Пики");
        resetButton = new Button("Сброс");

        // Настройка ComboBox
        lineSelectionComboBox.getItems().addAll("Ka1", "Ka2", "Kb1", "Kb2");
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
            // Сброс трансформаций изображения
            imageView.setTranslateX(0);
            imageView.setTranslateY(0);
            imageView.setScaleX(1);
            imageView.setScaleY(1);
            imageProcessor.imageViewStates.put(selectedImage, new double[]{1, 1, 0, 0});
        }
    }

    // Переключение режима выбора пиков и обработка кликов
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

    // Настройка режима выбора пиков
    private void setupPeakSelectionMode(LineChart<Number, Number> lineChart, ImageView imageView) {
        if (lineChart != null) {
            // Установка курсора перекрестия
            lineChart.setCursor(Cursor.CROSSHAIR);

            // Обработка кликов на графике
            lineChart.setOnMouseClicked(event -> handleLineChartClick(event, lineChart));
        }

        if (imageView != null) {
            // Установка курсора перекрестия
            imageView.setCursor(Cursor.CROSSHAIR);

            // Обработка кликов на изображении
            imageView.setOnMouseClicked(event -> handleImageViewClick(event, imageView));
        }
    }

    // Отключение режима выбора пиков
    private void disablePeakSelectionMode(LineChart<Number, Number> lineChart, ImageView imageView) {
        if (lineChart != null) {
            // Сброс курсора
            lineChart.setCursor(Cursor.DEFAULT);
            lineChart.setOnMouseClicked(null);
        }

        if (imageView != null) {
            // Сброс курсора
            imageView.setCursor(Cursor.DEFAULT);
            imageView.setOnMouseClicked(null);
        }
    }

    // Обработка клика на графике
    private void handleLineChartClick(MouseEvent event, LineChart<Number, Number> lineChart) {
        if (event.getButton() == MouseButton.PRIMARY) {
            // Получение координаты X клика
            double mouseXValue = lineChart.getXAxis().sceneToLocal(event.getSceneX(), 0).getX();
            if (!Double.isNaN(mouseXValue)) {
                // Получение значения данных по координате X
                Number xValue = lineChart.getXAxis().getValueForDisplay(mouseXValue);

                // Фиксирование диапазонов осей
                NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
                xAxis.setAutoRanging(false);

                NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                yAxis.setAutoRanging(false);

                // Создание серии данных для вертикальной линии
                XYChart.Series<Number, Number> verticalLineSeries = new XYChart.Series<>();
                verticalLineSeries.setName("Vertical Line");

                // Добавление точек для линии
                verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getLowerBound()));
                verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getUpperBound()));

                // Добавление серии в начало списка
                lineChart.getData().add(0, verticalLineSeries);

                // *** Добавление информации о линии в chartLines ***
                Tab currentTab = innerTabPane.getSelectionModel().getSelectedItem(); // Получить текущую внутреннюю вкладку для графика
                chartLines.computeIfAbsent(currentTab, k -> new ArrayList<>())
                        .add(new LineInfo(verticalLineSeries,
                                null,
                                xValue.doubleValue(),
                                lineSelectionComboBox.getValue(),
                                elementInput.getText()));
            }
        }
    }

    // Обработка клика на изображении
    private void handleImageViewClick(MouseEvent event, ImageView imageView) {
        if (event.getButton() == MouseButton.PRIMARY) {
            addPeakLine(event.getX(), imageView);
        }
    }

    // Добавляет линию для отметки пика на изображении
    public void addPeakLine(double xClick, ImageView imageView) {
        // Создание линии
        Line peakLine = new Line();
        peakLine.setStroke(Color.BLUE);
        peakLine.setStrokeWidth(2);

        // Ограничение координаты X щелчка
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

        // Добавление информации о линии
        imageLines.computeIfAbsent(imageProcessor.getSelectedImage(), k -> new ArrayList<>())
                .add(new LineInfo(null, peakLine, finalXPosition, lineSelectionComboBox.getValue(), elementInput.getText()));
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