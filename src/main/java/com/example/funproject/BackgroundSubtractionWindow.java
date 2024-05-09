package com.example.funproject;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class BackgroundSubtractionWindow extends Stage {

    // Элементы управления для выбора типа фона
    private ComboBox<String> backgroundTypeComboBox;
    private Button subtractButton;

    // Элементы управления для линейного фона
    private Label x1Label;
    private TextField x1TextField;
    private Button x1Button;
    private Label x2Label;
    private TextField x2TextField;
    private Button x2Button;

    // Элементы управления для экспоненциального фона
    private Label beforeLineLabel;
    private Label afterLineLabel;

    // "От" и "До" для точек до линии
    private Label beforeFromLabel;
    private TextField beforeFromTextField;
    private Button beforeFromButton;
    private Label beforeToLabel;
    private TextField beforeToTextField;
    private Button beforeToButton;

    // "От" и "До" для точек после линии
    private Label afterFromLabel;
    private TextField afterFromTextField;
    private Button afterFromButton;
    private Label afterToLabel;
    private TextField afterToTextField;
    private Button afterToButton;

    // Конструктор окна вычитания фона
    public BackgroundSubtractionWindow(HelloController controller, LineChart<Number, Number> chart) {

        // Инициализация элементов управления
        initializeControls();

        // Создание GridPane для размещения элементов управления
        GridPane gridPane = createGridPane();

        // Добавление элементов управления на GridPane
        addControlsToGridPane(gridPane);

        // Настройка окна
        configureWindow(gridPane);

        // Обработка выбора типа фона
        handleBackgroundTypeSelection();

        // Обработка нажатия кнопки "Вычесть фон"
        handleSubtractButtonClick(chart);

        // Обработка нажатия кнопок выбора точек на графике
        handlePointSelectionButtonsClick(chart);
    }

    // Инициализация элементов управления
    private void initializeControls() {
        backgroundTypeComboBox = new ComboBox<>();
        backgroundTypeComboBox.getItems().addAll("Линейный", "Экспоненциальный");
        subtractButton = new Button("Вычесть фон");

        // Элементы для линейного фона
        x1Label = new Label("X1:");
        x1TextField = new TextField();
        x1Button = new Button("...");
        x2Label = new Label("X2:");
        x2TextField = new TextField();
        x2Button = new Button("...");

        // Элементы для экспоненциального фона
        beforeLineLabel = new Label("Точки до линии:");
        afterLineLabel = new Label("Точки после линии:");
        beforeFromLabel = new Label("От:");
        beforeFromTextField = new TextField();
        beforeFromButton = new Button("...");
        beforeToLabel = new Label("До:");
        beforeToTextField = new TextField();
        beforeToButton = new Button("...");
        afterFromLabel = new Label("От:");
        afterFromTextField = new TextField();
        afterFromButton = new Button("...");
        afterToLabel = new Label("До:");
        afterToTextField = new TextField();
        afterToButton = new Button("...");
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
        gridPane.add(new Label("Тип фона:"), 0, 0);
        gridPane.add(backgroundTypeComboBox, 1, 0);
        gridPane.add(subtractButton, 1, 8);

        // Добавление элементов для линейного фона (скрытые по умолчанию)
        gridPane.add(x1Label, 0, 2);
        gridPane.add(x1TextField, 1, 2);
        gridPane.add(x1Button, 2, 2);
        gridPane.add(x2Label, 0, 3);
        gridPane.add(x2TextField, 1, 3);
        gridPane.add(x2Button, 2, 3);
        hideLinearBackground();

        // Добавление элементов для экспоненциального фона (скрытые по умолчанию)
        gridPane.add(beforeLineLabel, 0, 2);
        gridPane.add(beforeFromLabel, 0, 3);
        gridPane.add(beforeFromTextField, 1, 3);
        gridPane.add(beforeFromButton, 2, 3);
        gridPane.add(beforeToLabel, 0, 4);
        gridPane.add(beforeToTextField, 1, 4);
        gridPane.add(beforeToButton, 2, 4);
        gridPane.add(afterLineLabel, 0, 5);
        gridPane.add(afterFromLabel, 0, 6);
        gridPane.add(afterFromTextField, 1, 6);
        gridPane.add(afterFromButton, 2, 6);
        gridPane.add(afterToLabel, 0, 7);
        gridPane.add(afterToTextField, 1, 7);
        gridPane.add(afterToButton, 2, 7);
        hideExpBackground();
    }

    // Настройка окна
    private void configureWindow(GridPane gridPane) {
        this.setTitle("Вычитание фона");
        this.setScene(new Scene(gridPane));
        this.setAlwaysOnTop(true);
    }

    // Обработка выбора типа фона
    private void handleBackgroundTypeSelection() {
        backgroundTypeComboBox.setOnAction(event -> {
            String selectedType = backgroundTypeComboBox.getValue();
            if (selectedType.equals("Линейный")) {
                showLinearBackgroud();
                hideExpBackground();
            } else if (selectedType.equals("Экспоненциальный")) {
                showExpBackground();
                hideLinearBackground();
            }
        });
    }

    // Обработка нажатия кнопки "Вычесть фон"
    private void handleSubtractButtonClick(LineChart<Number, Number> chart) {
        subtractButton.setOnAction(event -> {
            if (backgroundTypeComboBox.getValue().equals("Линейный")) {
                subtractLinearBackground(chart);
            } else if (backgroundTypeComboBox.getValue().equals("Экспоненциальный")) {
                subtractExponentialBackground(chart);
            }
        });
    }

    // Обработка нажатия кнопок выбора точек на графике
    private void handlePointSelectionButtonsClick(LineChart<Number, Number> chart) {
        x1Button.setOnAction(event -> lineChartMouseClicked(chart, x1TextField));
        x2Button.setOnAction(event -> lineChartMouseClicked(chart, x2TextField));
        beforeFromButton.setOnAction(event -> lineChartMouseClicked(chart, beforeFromTextField));
        beforeToButton.setOnAction(event -> lineChartMouseClicked(chart, beforeToTextField));
        afterFromButton.setOnAction(event -> lineChartMouseClicked(chart, afterFromTextField));
        afterToButton.setOnAction(event -> lineChartMouseClicked(chart, afterToTextField));
    }

    // Функция для выбора точки на графике и заполнения текстового поля
    private void lineChartMouseClicked(LineChart<Number, Number> chart, TextField textField) {
        chart.setOnMouseClicked(mouseEvent -> {
            double sceneX = mouseEvent.getSceneX();
            double sceneY = mouseEvent.getSceneY();
            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            double mouseXValue = xAxis.sceneToLocal(sceneX, sceneY).getX();
            Number xValue = xAxis.getValueForDisplay(mouseXValue);
            textField.setText(String.valueOf(xValue));
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
        LineChart.Series<Number, Number> subtractedSeries = new XYChart.Series<>();
        subtractedSeries.setName("Intensities");
        LineChart.Series<Number, Number> backgroundLineSeries = new XYChart.Series<>();
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

    // Вычитание экспоненциального фона
    private void subtractExponentialBackground(LineChart<Number, Number> chart) {
        // Получение значений из текстовых полей
        double beforeFrom = Double.parseDouble(beforeFromTextField.getText());
        double beforeTo = Double.parseDouble(beforeToTextField.getText());
        double afterFrom = Double.parseDouble(afterFromTextField.getText());
        double afterTo = Double.parseDouble(afterToTextField.getText());

        // Получение последней серии данных
        XYChart.Series<Number, Number> lastSeries = chart.getData().get(chart.getData().size() - 1);
        ObservableList<XYChart.Data<Number, Number>> data = lastSeries.getData();

        // ... (Реализация МНК - опущенная часть кода)

        // Разделение данных на две части: до и после линии
        List<XYChart.Data<Number, Number>> beforeData = data.stream()
                .filter(point -> point.getXValue().doubleValue() >= beforeFrom && point.getXValue().doubleValue() <= beforeTo)
                .collect(Collectors.toList());
        List<XYChart.Data<Number, Number>> afterData = data.stream()
                .filter(point -> point.getXValue().doubleValue() >= afterFrom && point.getXValue().doubleValue() <= afterTo)
                .collect(Collectors.toList());

        // Создать новую серию для вычтенных данных
        LineChart.Series<Number, Number> subtractedSeries = new XYChart.Series<>();
        subtractedSeries.setName("Вычтенные данные");

        // Вычитание фона и добавление точек в новую серию
        for (XYChart.Data<Number, Number> point : data) {
            double x = point.getXValue().doubleValue();
            double yValue = point.getYValue().doubleValue();
            //double backgroundValue = c + b * Math.pow(x, n) / (a + Math.pow(Math.E, n * x)); // Вычисляем фон для x
            //double subtractedY = yValue - backgroundValue;
            //subtractedSeries.getData().add(new XYChart.Data<>(x, subtractedY));
        }

        // Добавить новую серию на график
        chart.getData().clear();
        chart.getData().add(subtractedSeries);
    }

    // Показать элементы управления для линейного фона
    private void showLinearBackgroud() {
        x1Label.setVisible(true);
        x1TextField.setVisible(true);
        x1Button.setVisible(true);
        x2Label.setVisible(true);
        x2TextField.setVisible(true);
        x2Button.setVisible(true);
    }

    // Скрыть элементы управления для линейного фона
    private void hideLinearBackground() {
        x1Label.setVisible(false);
        x1TextField.setVisible(false);
        x1Button.setVisible(false);
        x2Label.setVisible(false);
        x2TextField.setVisible(false);
        x2Button.setVisible(false);
    }

    // Показать элементы управления для экспоненциального фона
    private void showExpBackground() {
        beforeLineLabel.setVisible(true);
        afterLineLabel.setVisible(true);
        beforeFromLabel.setVisible(true);
        beforeFromTextField.setVisible(true);
        beforeFromButton.setVisible(true);
        beforeToLabel.setVisible(true);
        beforeToTextField.setVisible(true);
        beforeToButton.setVisible(true);
        afterFromLabel.setVisible(true);
        afterFromTextField.setVisible(true);
        afterFromButton.setVisible(true);
        afterToLabel.setVisible(true);
        afterToTextField.setVisible(true);
        afterToButton.setVisible(true);
    }

    // Скрыть элементы управления для экспоненциального фона
    private void hideExpBackground() {
        beforeLineLabel.setVisible(false);
        afterLineLabel.setVisible(false);
        beforeFromLabel.setVisible(false);
        beforeFromTextField.setVisible(false);
        beforeFromButton.setVisible(false);
        beforeToLabel.setVisible(false);
        beforeToTextField.setVisible(false);
        beforeToButton.setVisible(false);
        afterFromLabel.setVisible(false);
        afterFromTextField.setVisible(false);
        afterFromButton.setVisible(false);
        afterToLabel.setVisible(false);
        afterToTextField.setVisible(false);
        afterToButton.setVisible(false);
    }
}