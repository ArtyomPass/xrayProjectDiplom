package com.example.funproject;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class BackgroundSubtractionWindow extends Stage {

    // Элементы управления для линейного фона
    private Label x1Label;
    private TextField x1TextField;
    private Button x1Button;
    private Label x2Label;
    private TextField x2TextField;
    private Button x2Button;
    private Button subtractButton;

    private TextField currentWaitingTextField = null;

    // Конструктор окна вычитания фона
    public BackgroundSubtractionWindow(LineChart<Number, Number> chart) {

        // Инициализация элементов управления
        initializeControls();

        // Создание GridPane для размещения элементов управления
        GridPane gridPane = createGridPane();

        // Добавление элементов управления на GridPane
        addControlsToGridPane(gridPane);

        // Настройка окна
        configureWindow(gridPane);

        // Обработка нажатия кнопки "Вычесть фон"
        handleSubtractButtonClick(chart);

        // Обработка нажатия кнопок выбора точек на графике
        handlePointSelectionButtonsClick(chart);
    }

    // Инициализация элементов управления
    private void initializeControls() {
        subtractButton = new Button("Вычесть фон");

        // Элементы для линейного фона
        x1Label = new Label("X1:");
        x1TextField = new TextField();
        x1Button = new Button("...");
        x2Label = new Label("X2:");
        x2TextField = new TextField();
        x2Button = new Button("...");
    }

    // Создание GridPane для размещения элементов управления
    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        return gridPane;
    }

    // Добавление элементов управления на GridPane
    private void addControlsToGridPane(GridPane gridPane) {
        gridPane.add(new Label("Тип фона: Линейный"), 0, 0);
        gridPane.add(subtractButton, 1, 6);

        // Добавление элементов для линейного фона
        gridPane.add(x1Label, 0, 1);
        gridPane.add(x1TextField, 1, 1);
        gridPane.add(x1Button, 2, 1);
        gridPane.add(x2Label, 0, 2);
        gridPane.add(x2TextField, 1, 2);
        gridPane.add(x2Button, 2, 2);
    }

    // Настройка окна
    private void configureWindow(GridPane gridPane) {
        this.setTitle("Вычитание фона");
        this.setScene(new Scene(gridPane));
        this.setAlwaysOnTop(true);
    }

    // Обработка нажатия кнопки "Вычесть фон"
    private void handleSubtractButtonClick(LineChart<Number, Number> chart) {
        subtractButton.setOnAction(event -> subtractLinearBackground(chart));
    }

    // Обработка нажатия кнопок выбора точек на графике
    private void handlePointSelectionButtonsClick(LineChart<Number, Number> chart) {
        x1Button.setOnAction(event -> setTextFieldToWaitingMode(chart, x1TextField));
        x2Button.setOnAction(event -> setTextFieldToWaitingMode(chart, x2TextField));
    }

    // Установка текстового поля в режим ожидания
    private void setTextFieldToWaitingMode(LineChart<Number, Number> chart, TextField textField) {
        if (currentWaitingTextField != null) {
            currentWaitingTextField.setStyle(""); // Сбросить стиль предыдущего текстового поля
        }
        currentWaitingTextField = textField;
        textField.setStyle("-fx-background-color: yellow;");

        chart.setOnMouseClicked(mouseEvent -> {
            double sceneX = mouseEvent.getSceneX();
            double sceneY = mouseEvent.getSceneY();
            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            double mouseXValue = xAxis.sceneToLocal(sceneX, sceneY).getX();
            Number xValue = xAxis.getValueForDisplay(mouseXValue);
            textField.setText(String.valueOf(xValue));
            textField.setStyle(""); // Сбросить стиль

            currentWaitingTextField = null;

            // Сбросить обработчик событий
            chart.setOnMouseClicked(null);
        });
    }

    // Вычитание линейного фона
    private void subtractLinearBackground(LineChart<Number, Number> chart) {
        XYChart.Series<Number, Number> lastSeries = chart.getData().get(chart.getData().size() - 1);
        ObservableList<XYChart.Data<Number, Number>> data = lastSeries.getData();

        // Получение значений X1 и X2
        double x1 = Double.parseDouble(x1TextField.getText());
        double x2 = Double.parseDouble(x2TextField.getText());

        // Найти ближайшие точки к X1 и X2
        XYChart.Data<Number, Number> point1 = findClosestPoint(data, x1);
        XYChart.Data<Number, Number> point2 = findClosestPoint(data, x2);

        // Получить значения y1 и y2 из ближайших точек
        double y1 = point1.getYValue().doubleValue();
        double y2 = point2.getYValue().doubleValue();

        // Вычисление коэффициентов линейной функции (y = mx + b)
        double m = (y2 - y1) / (point2.getXValue().doubleValue() - point1.getXValue().doubleValue());
        double b = y1 - m * point1.getXValue().doubleValue();

        // Создать новую серию для вычтенных данных и линию фона
        XYChart.Series<Number, Number> subtractedSeries = new XYChart.Series<>();
        subtractedSeries.setName("Intensities");
        XYChart.Series<Number, Number> backgroundLineSeries = new XYChart.Series<>();
        backgroundLineSeries.setName("Baseline");

        // Вычитание фона и добавление точек в новую серию
        for (XYChart.Data<Number, Number> point : data) {
            double x = point.getXValue().doubleValue();
            double yValue = point.getYValue().doubleValue();
            double backgroundValue = m * x + b;
            double subtractedY = yValue - backgroundValue;
            subtractedSeries.getData().add(new XYChart.Data<>(x, subtractedY));
            backgroundLineSeries.getData().add(new XYChart.Data<>(x, backgroundValue));
        }

        // Добавить серии на график и скрыть легенду
        chart.getData().clear();
        chart.getData().add(backgroundLineSeries);
        backgroundLineSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke-width: 1; -fx-stroke-dash-array: 2 2;");
        chart.getData().add(subtractedSeries);
        chart.setLegendVisible(false);
    }

    // Поиск ближайшей точки к заданному значению x
    private XYChart.Data<Number, Number> findClosestPoint(ObservableList<XYChart.Data<Number, Number>> data, double x) {
        XYChart.Data<Number, Number> closestPoint = null;
        double minDistance = Double.MAX_VALUE;
        for (XYChart.Data<Number, Number> point : data) {
            double distance = Math.abs(point.getXValue().doubleValue() - x);
            if (distance < minDistance) {
                closestPoint = point;
                minDistance = distance;
            }
        }
        return closestPoint;
    }
}
