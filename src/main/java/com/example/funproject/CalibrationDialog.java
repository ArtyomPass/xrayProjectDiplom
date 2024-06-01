package com.example.funproject;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Диалоговое окно для калибровки спектра.
 */
public class CalibrationDialog extends Stage {

    // --- UI элементы ---
    private RadioButton linesFromPhotoRadioButton;
    private RadioButton linesFromSpectrumRadioButton;
    private ToggleGroup linesSourceToggleGroup;
    private Button calibrateButton;
    private Button dispersionButton;
    private Label dispersionLabel;
    private TextField dispersionField;
    private TextField orderStandardField;
    private TextField orderSampleField;
    private TextField dSpacingField;
    private Label orderStandardLabel;
    private Label orderSampleLabel;
    private Label dSpacingLabel;

    // --- Данные для калибровки ---
    private List<LineInfo> lineImageInfos;
    private List<LineInfo> lineChartInfos;
    private LineChart<Number, Number> currentChart;
    private TableView<SpectralDataTable.SpectralData> tableView;
    private Tab selectedTab;

    public CalibrationDialog(Tab selectedTab,
                             List<LineInfo> lineImageInfos,
                             List<LineInfo> lineChartInfos,
                             LineChart<Number, Number> currentChart,
                             TableView<SpectralDataTable.SpectralData> tableView) {
        this.lineImageInfos = lineImageInfos;
        this.lineChartInfos = lineChartInfos;
        this.currentChart = currentChart;
        this.tableView = tableView;
        this.selectedTab = selectedTab;

        // --- Создание пользовательского интерфейса ---
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // --- Заголовок ---
        Label titleLabel = new Label("Калибровка методом двух стандартов");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // --- Радиокнопки для выбора источника линий ---
        linesSourceToggleGroup = new ToggleGroup();
        linesFromPhotoRadioButton = new RadioButton("Линии с фотографии");
        linesFromPhotoRadioButton.setToggleGroup(linesSourceToggleGroup);
        linesFromSpectrumRadioButton = new RadioButton("Линии со спектра");
        linesFromSpectrumRadioButton.setToggleGroup(linesSourceToggleGroup);

        // --- Поля для ввода порядка отражения и межплоскостного расстояния ---
        orderStandardField = new TextField();
        orderStandardField.setPromptText("Порядок отражения");
        orderStandardField.setText("1");
        dSpacingField = new TextField();
        dSpacingField.setPromptText("Значение межплоскостного расстояния");
        dSpacingField.setText("2.38");
        dispersionLabel = new Label("Значение дисперсии:");

        // --- Кнопки ---
        calibrateButton = new Button("Калибровать");
        dispersionButton = new Button("Дисперсия");
        dispersionField = new TextField();
        dispersionField.setPromptText("Значение дисперсии");

        // --- Инициализация меток порядка отражения ---
        orderStandardLabel = new Label("Порядок отражения:");

        // --- Label для межплоскостного расстояния ---
        dSpacingLabel = new Label("Значение межплоскостного расстояния:");

        // --- Добавление элементов в VBox ---
        root.getChildren().addAll(
                titleLabel, // Добавление заголовка
                linesFromPhotoRadioButton,
                linesFromSpectrumRadioButton,
                orderStandardLabel,
                orderStandardField,
                dSpacingLabel,
                dSpacingField,
                dispersionButton,
                dispersionLabel,
                dispersionField,
                calibrateButton
        );

        // --- Установка начальной видимости полей ввода ---
        updateInputFieldsVisibility();

        // --- Обработчики событий ---
        calibrateButton.setOnAction(this::performCalibration);
        dispersionButton.setOnAction(this::handleDispersionButtonAction);

        // --- Создание сцены и отображение окна ---
        Scene scene = new Scene(root);
        setScene(scene);
        this.setAlwaysOnTop(true);
        show();
    }


    private void handleDispersionButtonAction(ActionEvent event) {
        boolean isVisible = dispersionField.isVisible();
        if (!isVisible) {
            dSpacingField.setStyle("-fx-background-color: lightgrey;");
        } else {
            dSpacingField.setStyle("-fx-background-color: white;");
        }
        dSpacingField.setEditable(isVisible);
        dispersionField.setVisible(!isVisible);
        dispersionLabel.setVisible(!isVisible);
    }


    private void updateInputFieldsVisibility() {
        orderStandardField.setVisible(true);
        dSpacingField.setVisible(true);
        dSpacingLabel.setVisible(true);
        orderStandardLabel.setVisible(true);
        dispersionButton.setVisible(true);
        dispersionField.setVisible(false);
        dispersionLabel.setVisible(false);
    }

    private void performCalibration(ActionEvent actionEvent) {
        // Основной метод, вызывающий второстепенные методы для выполнения калибровки
        double[] calibratedEnergies = performTwoStandardsCalibration();

        if (calibratedEnergies != null) {
            updateChartAndTable(calibratedEnergies);
        }

        this.close();
    }

    private double[] performTwoStandardsCalibration() {
        // Получение координат линий и углов для стандарта
        LineInfo line1 = lineChartInfos.stream()
                .filter(line -> line.getStandardType().equals("X1"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Line with standardType 'X1' not found"));

        LineInfo line2 = lineChartInfos.stream()
                .filter(line -> line.getStandardType().equals("X2"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Line with standardType 'X2' not found"));

        // Отладочная информация
        System.out.println("\n\n\nLine1 (X1): " + line1);
        System.out.println("Line2 (X2): " + line2);

        double x1 = line1.getXPosition();
        double x2 = line2.getXPosition();
        double angle1 = line1.getAngle(); // Угол в градусах для line1
        double angle2 = line2.getAngle(); // Угол в градусах для line2

        // Выводим координаты и углы в консоль
        System.out.println("x1: " + x1 + ", angle1: " + angle1);
        System.out.println("x2: " + x2 + ", angle2: " + angle2);

        // Рассчет угловой дисперсии
        double angularDispersion = (angle1 - angle2) / (x2 - x1);

        // Выводим угловую дисперсию для проверки
        System.out.println("Угловая дисперсия: " + angularDispersion);

        // Получаем значения порядка отражения и межплоскостного расстояния
        int n = Integer.parseInt(orderStandardField.getText());
        double d = Double.parseDouble(dSpacingField.getText());

        // Находим последнюю серию данных с именем "Спектр"
        Optional<XYChart.Series<Number, Number>> intensitiesSeriesOptional = currentChart.getData().stream()
                .filter(series -> series.getName().equals("Спектр"))
                .reduce((first, second) -> second);

        if (!intensitiesSeriesOptional.isPresent()) {
            showErrorDialog("Ошибка калибровки", "Серия данных 'Спектр' не найдена!");
            return null;
        }

        XYChart.Series<Number, Number> intensitiesSeries = intensitiesSeriesOptional.get();

        // Получаем координаты X всех точек спектра из текущего графика
        List<Double> xPositionsList = intensitiesSeries.getData().stream()
                .map(data -> ((Number) data.getXValue()).doubleValue())
                .collect(Collectors.toList());

        double[] xPositions = xPositionsList.stream().mapToDouble(Double::doubleValue).toArray();

        // Массив для хранения рассчитанных энергетических единиц для всех точек спектра
        double[] energies = new double[xPositions.length];

        // Проверяем длину массивов xPositions и данных интенсивности
        if (xPositions.length != intensitiesSeries.getData().size()) {
            System.out.println("Несоответствие длины данных! xPositions.length = " + xPositions.length + ", intensitiesSeries.getData().size() = " + intensitiesSeries.getData().size());
            showErrorDialog("Ошибка калибровки", "Несоответствие длины данных!");
            return null;
        }

        // Рассчитываем угол, длину волны и энергетические единицы для каждой точки спектра
        for (int i = 0; i < xPositions.length; i++) {
            double angle = angle1 + (x1 - xPositions[i]) * angularDispersion;
            double wavelength = (d * Math.sin(Math.toRadians(angle))) / n;
            double energy = 12398.1 / wavelength;
            energies[i] = energy;
           }

        // Возвращаем массив энергетических единиц
        return energies;
    }



    private void updateChartAndTable(double[] calibratedEnergies) {
        Optional<XYChart.Series<Number, Number>> intensitiesSeriesOptional = currentChart.getData().stream()
                .filter(series -> series.getName().equals("Спектр"))
                .findFirst();

        if (intensitiesSeriesOptional.isPresent()) {
            XYChart.Series<Number, Number> intensitiesSeries = intensitiesSeriesOptional.get();
            XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
            calibratedSeries.setName("Калиброванный спектр");

            // Выводим длину данных интенсивности
            System.out.println("Длина данных интенсивности = " + intensitiesSeries.getData().size());

            // Проверяем соответствие длины данных
            if (calibratedEnergies.length != intensitiesSeries.getData().size()) {
                showErrorDialog("Ошибка калибровки", "Несоответствие длины данных!");
                return;
            }

            for (int i = 0; i < calibratedEnergies.length; i++) {
                calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], intensitiesSeries.getData().get(i).getYValue()));
            }

            currentChart.getData().remove(intensitiesSeries);
            currentChart.getData().add(calibratedSeries);

            NumberAxis xAxis = (NumberAxis) currentChart.getXAxis();
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(calibratedEnergies[0]);
            xAxis.setUpperBound(calibratedEnergies[calibratedEnergies.length - 1]);

            // Настройка количества меток на оси X
            xAxis.setTickUnit((calibratedEnergies[calibratedEnergies.length - 1] - calibratedEnergies[0]) / 10);
            xAxis.setMinorTickVisible(true);
            xAxis.setMinorTickCount(5);

            SpectralDataTable.updateTableViewInTab(selectedTab, calibratedSeries.getData(), tableView);
            removeSeriesByName(currentChart, "Вертикальная линия");
        } else {
            showErrorDialog("Ошибка калибровки", "Серия данных 'Спектр' не найдена!");
        }
    }




    public static void removeSeriesByName(XYChart<Number, Number> chart, String seriesName) {
        List<XYChart.Series<Number, Number>> seriesToRemove = chart.getData().stream()
                .filter(series -> series.getName().equals(seriesName))
                .collect(Collectors.toList());
        chart.getData().removeAll(seriesToRemove);
    }


    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
