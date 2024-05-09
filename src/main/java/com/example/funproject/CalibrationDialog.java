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

public class CalibrationDialog extends Stage {

    // UI элементы
    private ComboBox<String> calibrationMethodComboBox;
    private RadioButton linesFromPhotoRadioButton;
    private RadioButton linesFromSpectrumRadioButton;
    private ToggleGroup linesSourceToggleGroup;
    private Button calibrateButton;
    // Данные для калибровки
    private List<LineInfo> lineImageInfos;
    private List<LineInfo> lineChartInfos;
    private LineChart<Number, Number> currentChart;
    private TableView<SpectralDataTable.SpectralData> tableView;
    private Tab selectedTab;
    // Поля для ввода дисперсии, порядка отражения и межплоскостного расстояния
    private Button dispersionButton;
    private Label dispersionLabel;
    private TextField dispersionField;
    private TextField orderStandardField;
    private TextField orderSampleField;
    private TextField dSpacingField;
    private Label dSpacingLabel;
    // Метки для полей ввода порядка отражения
    private Label orderStandardLabel;
    private Label orderSampleLabel;

    // Конструктор
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

        // Создание пользовательского интерфейса
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Комбобокс для выбора метода калибровки
        calibrationMethodComboBox = new ComboBox<>();
        calibrationMethodComboBox.getItems().addAll("Два стандарта", "Линейная регрессия");
        calibrationMethodComboBox.setValue("Два стандарта");

        // Радиокнопки для выбора источника линий
        linesSourceToggleGroup = new ToggleGroup();
        linesFromPhotoRadioButton = new RadioButton("Линии с фотографии");
        linesFromPhotoRadioButton.setToggleGroup(linesSourceToggleGroup);
        linesFromSpectrumRadioButton = new RadioButton("Линии со спектра");
        linesFromSpectrumRadioButton.setToggleGroup(linesSourceToggleGroup);

        // Поля для ввода порядка отражения и межплоскостного расстояния
        orderStandardField = new TextField();
        orderStandardField.setPromptText("Порядок отражения для стандарта");
        orderStandardField.setText("1");
        orderSampleField = new TextField();
        orderSampleField.setPromptText("Порядок отражения для образца");
        orderSampleField.setText("1");
        dSpacingField = new TextField();
        dSpacingField.setPromptText("Значение межплоскостного расстояния");
        dSpacingField.setText("2.38");
        dispersionLabel = new Label("Значение дисперсии:");

        // Кнопки
        calibrateButton = new Button("Калибровать");
        dispersionButton = new Button("Дисперсия");
        dispersionField = new TextField();
        dispersionField.setPromptText("Значение дисперсии");

        // Инициализация меток порядка отражения
        orderStandardLabel = new Label("Порядок отражения (стандарт):");
        orderSampleLabel = new Label("Порядок отражения (образец):");

        // Label для межплоскостного расстояния
        dSpacingLabel = new Label("Значение межплоскостного расстояния:");

        // Добавление элементов в VBox
        root.getChildren().addAll(
                new Label("Метод калибровки:"),
                calibrationMethodComboBox,
                linesFromPhotoRadioButton,
                linesFromSpectrumRadioButton,
                orderStandardLabel,
                orderStandardField,
                orderSampleLabel,
                orderSampleField,
                dSpacingLabel,
                dSpacingField,
                dispersionButton,
                dispersionLabel,
                dispersionField,
                calibrateButton
        );

        // Установка начальной видимости полей ввода
        updateInputFieldsVisibility();

        // Обработчики событий
        calibrationMethodComboBox.setOnAction(this::handleComboBoxAction);
        calibrateButton.setOnAction(this::performCalibration);
        dispersionButton.setOnAction(this::handleDispersionButtonAction);

        // Создание сцены и отображение окна
        Scene scene = new Scene(root);
        setScene(scene);
        this.setAlwaysOnTop(true);
        show();
    }

    private void handleDispersionButtonAction(ActionEvent event) {
        boolean isVisible = dispersionField.isVisible();
        // Сначала меняем цвет, потом делаем поля невводимыми
        if (!isVisible) {
            orderSampleField.setStyle("-fx-background-color: lightgrey;");
            dSpacingField.setStyle("-fx-background-color: lightgrey;");
        } else {
            orderSampleField.setStyle("-fx-background-color: white;");
            dSpacingField.setStyle("-fx-background-color: white;");
        }
        orderSampleField.setEditable(isVisible);
        dSpacingField.setEditable(isVisible);
        dispersionField.setVisible(!isVisible);
        dispersionLabel.setVisible(!isVisible);
    }

    // Обновление видимости полей ввода
    private void updateInputFieldsVisibility() {
        String selectedMethod = calibrationMethodComboBox.getValue();
        linesFromPhotoRadioButton.setVisible(true);
        linesFromSpectrumRadioButton.setVisible(true);
        // Отображаем поля и метки только для метода "Два стандарта"
        boolean isTwoStandardsMethod = "Два стандарта".equals(selectedMethod);
        orderStandardField.setVisible(isTwoStandardsMethod);
        orderSampleField.setVisible(isTwoStandardsMethod);
        dSpacingField.setVisible(isTwoStandardsMethod);
        dSpacingLabel.setVisible(isTwoStandardsMethod);
        // Управление видимостью меток
        orderStandardLabel.setVisible(isTwoStandardsMethod);
        orderSampleLabel.setVisible(isTwoStandardsMethod);
        dispersionButton.setVisible(isTwoStandardsMethod);
        // Скрываем поле ввода дисперсии и метку
        dispersionField.setVisible(false);
        dispersionLabel.setVisible(false);
    }

    // Выполнение калибровки
    private void performCalibration(ActionEvent actionEvent) {
        String selectedMethod = calibrationMethodComboBox.getValue();
        if ("Два стандарта".equals(selectedMethod)) {
            performTwoStandardsCalibration();
        } else if ("Линейная регрессия".equals(selectedMethod)) {
            performLinearRegressionCalibration();
        }
        this.close();
    }

    // Калибровка методом 2х стандартов
    private void performTwoStandardsCalibration() {
        Map<String, Map<String, Double>> elementLinesEnergies = readElementLinesEnergiesFromFile("src/main/java/com/example/funproject/xray_lines_3d_metals.txt");

        // Получение значений порядка отражения и межплоскостного расстояния
        int orderStandard;
        int orderSample;
        double dSpacing;
        try {
            orderStandard = Integer.parseInt(orderStandardField.getText());
            orderSample = Integer.parseInt(orderSampleField.getText());
            dSpacing = Double.parseDouble(dSpacingField.getText());
            if (orderStandard <= 0 || orderSample <= 0 || dSpacing <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Ошибка ввода", "Некорректные значения порядка отражения или межплоскостного расстояния.");
            return;
        }

        // Калибровка с одним стандартом и дисперсией
        if (dispersionButton.isVisible() && dispersionField.isVisible()) {
            try {
                double dispersion = Double.parseDouble(dispersionField.getText());
                if (dispersion <= 0) {
                    throw new NumberFormatException();
                }

                // Поиск Kα1 линии для одного элемента
                LineInfo standard1Line = null;
                List<LineInfo> lines = linesFromPhotoRadioButton.isSelected() ? lineImageInfos : lineChartInfos;
                if (lines == null) {
                    showErrorDialog("Ошибка калибровки", "Список линий не инициализирован.");
                    return;
                }
                for (LineInfo line : lines) {
                    if (line.getPeakType().equals("Ka1") && standard1Line == null) {
                        standard1Line = line;
                        break;
                    }
                }

                // Обработка ошибок и получение энергии линии
                if (standard1Line == null) {
                    showErrorDialog("Ошибка калибровки", "Не найдена Ka1 линия.");
                    return;
                }
                double energyStandard1 = elementLinesEnergies.get(standard1Line.getElementName()).get("Ka1");
                int pixelStandard1 = (int) standard1Line.getXPosition();

                // Калибровка серии данных "Intensities"
                Optional<XYChart.Series<Number, Number>> intensitiesSeriesOptional = currentChart.getData().stream()
                        .filter(series -> series.getName().equals("Intensities"))
                        .findFirst();
                if (intensitiesSeriesOptional.isPresent()) {
                    XYChart.Series<Number, Number> intensitiesSeries = intensitiesSeriesOptional.get();
                    double[] positions = intensitiesSeries.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .toArray();
                    double[] calibratedEnergies = calibrateWithDispersion(energyStandard1, dispersion, pixelStandard1, positions, orderStandard);

                    // Создание новой серии с откалиброванными энергиями
                    XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
                    calibratedSeries.setName("Intensities");
                    for (int i = 0; i < positions.length; i++) {
                        calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], intensitiesSeries.getData().get(i).getYValue()));
                    }

                    // Обновление графика и таблицы
                    updateChartAndTable(intensitiesSeries, calibratedSeries);
                    System.out.println("Серия данных 'Intensities' откалибрована с дисперсией.");
                } else {
                    showErrorDialog("Ошибка калибровки", "Серия данных 'Intensities' не найдена!");
                }
            } catch (NumberFormatException e) {
                showErrorDialog("Ошибка ввода", "Некорректное значение дисперсии.");
            }
        } else {
            // Калибровка с двумя стандартами
            List<LineInfo> lines = linesFromPhotoRadioButton.isSelected() ? lineImageInfos : lineChartInfos;
            if (lines == null) {
                showErrorDialog("Ошибка калибровки", "Список линий не инициализирован.");
                return;
            }

            // Поиск Kα1 и Kβ1 линий для двух элементов
            LineInfo standard1Line = null;
            LineInfo standard2Line = null;
            for (LineInfo line : lines) {
                if (line.getPeakType().equals("Ka1") && standard1Line == null) {
                    standard1Line = line;
                } else if (line.getPeakType().equals("Kb1") &&
                        standard2Line == null &&
                        !line.getElementName().equals(standard1Line.getElementName())) {
                    standard2Line = line;
                }
                if (standard1Line != null && standard2Line != null) {
                    break;
                }
            }

            // Обработка ошибок и получение энергий линий
            if (standard1Line == null || standard2Line == null) {
                showErrorDialog("Ошибка калибровки", "Не найдены Kα1 и Kβ1 линии для двух элементов.");
                return;
            }
            double energyStandard1 = elementLinesEnergies.get(standard1Line.getElementName()).get("Ka1");
            double energyStandard2 = elementLinesEnergies.get(standard2Line.getElementName()).get("Kb1");

            // Калибровка серии данных "Intensities"
            Optional<XYChart.Series<Number, Number>> intensitiesSeriesOptional = currentChart.getData().stream()
                    .filter(series -> series.getName().equals("Intensities"))
                    .findFirst();
            if (intensitiesSeriesOptional.isPresent()) {
                XYChart.Series<Number, Number> intensitiesSeries = intensitiesSeriesOptional.get();
                double[] positions = intensitiesSeries.getData().stream()
                        .mapToDouble(data -> data.getXValue().doubleValue())
                        .toArray();
                int pixelStandard1 = (int) standard1Line.getXPosition();
                int pixelSampleKBeta1 = (int) standard2Line.getXPosition();

                // Вызов метода калибровки
                double[] calibratedEnergies = calibrateWithTwoPoints(energyStandard1, pixelStandard1,
                        energyStandard2, pixelSampleKBeta1,
                        positions, orderStandard, orderSample, dSpacing);

                // Создание новой серии с откалиброванными энергиями
                XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
                calibratedSeries.setName("Intensities");
                for (int i = 0; i < positions.length; i++) {
                    calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], intensitiesSeries.getData().get(i).getYValue()));
                }

                // Обновление графика и таблицы
                updateChartAndTable(intensitiesSeries, calibratedSeries);
                System.out.println("Серия данных 'Intensities' откалибрована методом двух стандартов.");
            } else {
                showErrorDialog("Ошибка калибровки", "Серия данных 'Intensities' не найдена!");
            }
        }
    }

    // Метод калибровки с двумя стандартами (Kα1 и Kβ1)
    private static double[] calibrateWithTwoPoints(double energyStandard, int pixelStandard,
                                                   double energySampleKBeta1, int pixelSampleKBeta1,
                                                   double[] spectrum, int orderStandard, int orderSample, double dSpacing) {
        // Вычисление длин волн по энергиям (hc/E)
        double wavelengthStandard = 1239.84193 / energyStandard;
        double wavelengthSample = 1239.84193 / energySampleKBeta1;

        // Учёт порядка отражения в законе Брэгга-Вульфа: nλ = 2d sin θ
        double thetaStandard = Math.asin(orderStandard * wavelengthStandard / (2 * dSpacing));
        double thetaSample = Math.asin(orderSample * wavelengthSample / (2 * dSpacing));

        // Вычисление угловой дисперсии
        double angularDispersion = Math.abs(thetaSample - thetaStandard) / Math.abs(pixelSampleKBeta1 - pixelStandard);

        // Калибровка спектра
        double[] calibratedSpectrum = new double[spectrum.length];
        for (int i = 0; i < spectrum.length; i++) {
            double theta = thetaStandard + (spectrum[i] - pixelStandard) * angularDispersion;
            double wavelength = 2 * dSpacing * Math.sin(theta) / orderStandard; // Предполагаем одинаковый порядок отражения
            calibratedSpectrum[i] = 1239.84193 / wavelength;
        }
        return calibratedSpectrum;
    }

    // Метод калибровки с одним стандартом и дисперсией
    private static double[] calibrateWithDispersion(double energyStandard, double measuredDispersion,
                                                    int pixelStandard, double[] spectrum, int order) {
        // Учёт порядка отражения для получения истинной дисперсии
        double trueDispersion = measuredDispersion / order;

        // Калибровка спектра с использованием истинной дисперсии
        double[] calibratedSpectrum = new double[spectrum.length];
        for (int i = 0; i < spectrum.length; i++) {
            calibratedSpectrum[i] = energyStandard + (spectrum[i] - pixelStandard) * trueDispersion;
        }
        return calibratedSpectrum;
    }

    // Калибровка методом линейной регрессии
    private void performLinearRegressionCalibration() {
        Map<String, Map<String, Double>> elementLinesEnergies = readElementLinesEnergiesFromFile("src/main/java/com/example/funproject/xray_lines_3d_metals.txt");
        List<LineInfo> lines = linesFromPhotoRadioButton.isSelected() ? lineImageInfos : lineChartInfos;
        if (lines == null) {
            showErrorDialog("Ошибка калибровки", "Список линий не инициализирован.");
            return;
        }

        // Подготовка данных для линейной регрессии
        List<Double> knownPositions = new ArrayList<>();
        List<Double> knownEnergies = new ArrayList<>();
        for (LineInfo lineInfo : lines) {
            String elementName = lineInfo.getElementName();
            String peakType = lineInfo.getPeakType();
            if (elementLinesEnergies.containsKey(elementName) && elementLinesEnergies.get(elementName).containsKey(peakType)) {
                knownPositions.add(lineInfo.getXPosition());
                knownEnergies.add(elementLinesEnergies.get(elementName).get(peakType));
            } else {
                showErrorDialog("Ошибка калибровки", "Энергия линии не найдена для: " + elementName + " - " + peakType);
                return;
            }
        }

        // Вычисление калибровочных коэффициентов
        double[] xValues = knownPositions.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yValues = knownEnergies.stream().mapToDouble(Double::doubleValue).toArray();
        double[] calibrationParams = linearRegression(xValues, yValues);

        // Применение калибровки и определение новых границ
        double minCalibratedX = Double.POSITIVE_INFINITY;
        double maxCalibratedX = Double.NEGATIVE_INFINITY;
        for (XYChart.Series<Number, Number> series : currentChart.getData()) {
            if (!series.getName().equals("Vertical Line")) {
                double[] xValuesSeries = series.getData().stream()
                        .mapToDouble(data -> data.getXValue().doubleValue())
                        .toArray();
                double[] correctedEnergies = applyCalibrationCurve(xValuesSeries, calibrationParams);
                for (int i = 0; i < series.getData().size(); i++) {
                    series.getData().get(i).setXValue(correctedEnergies[i]);
                }
                SpectralDataTable.updateTableViewInTab(selectedTab, series.getData(), tableView);
                minCalibratedX = Math.min(minCalibratedX, Arrays.stream(correctedEnergies).min().getAsDouble());
                maxCalibratedX = Math.max(maxCalibratedX, Arrays.stream(correctedEnergies).max().getAsDouble());
            }
        }
        removeSeriesByName(currentChart, "Vertical Line");

        // Установка новых границ и меток оси X
        NumberAxis xAxis = (NumberAxis) currentChart.getXAxis();
        xAxis.setLowerBound(minCalibratedX);
        xAxis.setUpperBound(maxCalibratedX);
        xAxis.setAutoRanging(false);
        double tickUnit = (maxCalibratedX - minCalibratedX) / 10;
        xAxis.setTickUnit(tickUnit);
    }

    // Вычисление калибровочных коэффициентов для линейной регрессии
    private static double[] linearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }
        double denominator = n * sumXX - sumX * sumX;
        double a = (n * sumXY - sumX * sumY) / denominator;
        double b = (sumY - a * sumX) / n;
        return new double[]{a, b};
    }

    // Применить калибровочную кривую к спектру
    private static double[] applyCalibrationCurve(double[] positions, double[] params) {
        double[] correctedEnergies = new double[positions.length];
        for (int i = 0; i < positions.length; i++) {
            correctedEnergies[i] = params[0] * positions[i] + params[1];
        }
        return correctedEnergies;
    }

    // Чтение данных из файла
    private Map<String, Map<String, Double>> readElementLinesEnergiesFromFile(String fileName) {
        Map<String, Map<String, Double>> elementLinesEnergies = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String elementName = parts[0];
                String lineType = parts[1];
                double energy = Double.parseDouble(parts[2]);
                elementLinesEnergies.computeIfAbsent(elementName, k -> new HashMap<>()).put(lineType, energy);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return elementLinesEnergies;
    }

    // Удаляет все серии с указанным именем из графика
    public static void removeSeriesByName(XYChart<Number, Number> chart, String seriesName) {
        List<XYChart.Series<Number, Number>> seriesToRemove = chart.getData().stream()
                .filter(series -> series.getName().equals(seriesName))
                .collect(Collectors.toList());
        chart.getData().removeAll(seriesToRemove);
    }

    // Влияет на отображение нодов в зависимости от комбобокса
    private void handleComboBoxAction(ActionEvent event) {
        updateInputFieldsVisibility();
    }

    // Обновление графика и таблицы
    private void updateChartAndTable(XYChart.Series<Number, Number> oldSeries, XYChart.Series<Number, Number> newSeries) {
        currentChart.getData().remove(oldSeries);
        currentChart.getData().add(newSeries);
        NumberAxis xAxis = (NumberAxis) currentChart.getXAxis();
        xAxis.setAutoRanging(false);
        double[] calibratedEnergies = newSeries.getData().stream()
                .mapToDouble(data -> data.getXValue().doubleValue())
                .toArray();
        xAxis.setLowerBound(calibratedEnergies[0]);
        xAxis.setUpperBound(calibratedEnergies[calibratedEnergies.length - 1]);
        SpectralDataTable.updateTableViewInTab(selectedTab, newSeries.getData(), tableView);
        removeSeriesByName(currentChart, "Vertical Line");
    }

    // Выводит сообщения об ошибке
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}