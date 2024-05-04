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

    // Элементы пользовательского интерфейса
    private ComboBox<String> calibrationMethodComboBox;
    private RadioButton linesFromPhotoRadioButton;
    private RadioButton linesFromSpectrumRadioButton;
    private ToggleGroup linesSourceToggleGroup;
    private Button calibrateButton;
    private Button cancelButton;

    // Данные для калибровки
    private List<LineInfo> lineImageInfos;
    private List<LineInfo> lineChartInfos;
    private LineChart<Number, Number> currentChart;
    private TableView<SpectralDataTable.SpectralData> tableView;
    private Tab selectedTab;

    // Поля для ввода дисперсии
    private Button dispersionButton;
    private TextField dispersionField;

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
        calibrationMethodComboBox.setValue("Два стандарта"); // Значение по умолчанию

        // Радиокнопки для выбора источника линий (изначально скрыты)
        linesSourceToggleGroup = new ToggleGroup();
        linesFromPhotoRadioButton = new RadioButton("Линии с фотографии");
        linesFromPhotoRadioButton.setToggleGroup(linesSourceToggleGroup);
        linesFromPhotoRadioButton.setVisible(false);
        linesFromSpectrumRadioButton = new RadioButton("Линии со спектра");
        linesFromSpectrumRadioButton.setToggleGroup(linesSourceToggleGroup);
        linesFromSpectrumRadioButton.setVisible(false);

        // Кнопки
        calibrateButton = new Button("Калибровать");
        cancelButton = new Button("Отмена");

        // Кнопка и поле для ввода дисперсии
        dispersionButton = new Button("Дисперсия");
        dispersionField = new TextField();
        dispersionField.setPromptText("Значение дисперсии");
        dispersionField.setVisible(false);

        // Добавление элементов в VBox
        root.getChildren().addAll(
                new Label("Метод калибровки:"),
                calibrationMethodComboBox,
                linesFromPhotoRadioButton,
                linesFromSpectrumRadioButton,
                dispersionButton,
                dispersionField,
                calibrateButton,
                cancelButton
        );

        // Установка начальной видимости полей ввода
        updateInputFieldsVisibility();

        // Обработчики событий
        calibrationMethodComboBox.setOnAction(this::handleComboBoxAction);
        calibrateButton.setOnAction(this::performCalibration);
        // Обработчик событий для кнопки дисперсии
        dispersionButton.setOnAction(this::handleDispersionButtonAction);

        // Создание сцены и отображение окна
        Scene scene = new Scene(root);
        setScene(scene);
        this.setAlwaysOnTop(true);
        show();
    }

    // Обработчик события для кнопки "Дисперсия"
    private void handleDispersionButtonAction(ActionEvent event) {
        dispersionField.setVisible(!dispersionField.isVisible());
    }

    // Обновление видимости полей ввода
    private void updateInputFieldsVisibility() {
        String selectedMethod = calibrationMethodComboBox.getValue();
        linesFromPhotoRadioButton.setVisible("Два стандарта".equals(selectedMethod));
        linesFromSpectrumRadioButton.setVisible("Два стандарта".equals(selectedMethod));
        dispersionButton.setVisible("Два стандарта".equals(selectedMethod));
        dispersionField.setVisible(dispersionButton.isVisible() && dispersionField.isVisible()); // Только если кнопка видна
    }

    // Выполнение калибровки
    private void performCalibration(ActionEvent actionEvent) {
        String selectedMethod = calibrationMethodComboBox.getValue();
        if ("Два стандарта".equals(selectedMethod)) {
            performTwoStandardsCalibration();
        } else if ("Линейная регрессия".equals(selectedMethod)) {
            performLinearRegressionCalibration();
        }
    }

    // Калибровка методом 2х стандартов
    private void performTwoStandardsCalibration() {
        Map<String, Map<String, Double>> elementLinesEnergies = readElementLinesEnergiesFromFile("src/main/java/com/example/funproject/xray_lines_3d_metals.txt");

        // 1. Проверка выбранного метода калибровки
        if (dispersionButton.isVisible() && dispersionField.isVisible()) {
            // Калибровка с одним стандартом и дисперсией
            try {
                double dispersion = Double.parseDouble(dispersionField.getText());

                // 2. Поиск Kα1 линии для одного элемента
                LineInfo standard1Line = null;
                List<LineInfo> lines;
                if (linesFromPhotoRadioButton.isSelected()) {
                    lines = lineImageInfos;
                } else {
                    lines = lineChartInfos;
                }

                for (LineInfo line : lines) {
                    if (line.getPeakType().equals("Ka1") && standard1Line == null) {
                        standard1Line = line;
                        break;
                    }
                }

                // 3. Обработка ошибок и получение энергии линии
                if (standard1Line == null) {
                    System.err.println("Не найдена Ka1 линия. Введите Ka1 линию.");
                    return;
                }

                double energyStandard1 = elementLinesEnergies.get(standard1Line.getElementName()).get("Ka1");
                int pixelStandard1 = (int) standard1Line.getXPosition();

                // 4. Калибровка серии данных "Intensities"
                Optional<XYChart.Series<Number, Number>> intensitiesSeriesOptional = currentChart.getData().stream()
                        .filter(series -> series.getName().equals("Intensities"))
                        .findFirst();
                if (intensitiesSeriesOptional.isPresent()) {
                    XYChart.Series<Number, Number> intensitiesSeries = intensitiesSeriesOptional.get();
                    double[] positions = intensitiesSeries.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .toArray();

                    double[] calibratedEnergies = calibrateWithDispersion(energyStandard1, dispersion, pixelStandard1, positions);

                    // 5. Создание новой серии с откалиброванными энергиями
                    XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
                    calibratedSeries.setName("Calibrated Spectrum (Dispersion)");
                    for (int i = 0; i < positions.length; i++) {
                        calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], intensitiesSeries.getData().get(i).getYValue()));
                    }

                    // 6. Обновление графика и таблицы
                    currentChart.getData().remove(intensitiesSeries);
                    currentChart.getData().add(calibratedSeries);
                    NumberAxis xAxis = (NumberAxis) currentChart.getXAxis();
                    xAxis.setAutoRanging(false);
                    xAxis.setLowerBound(calibratedEnergies[0]);
                    xAxis.setUpperBound(calibratedEnergies[calibratedEnergies.length - 1]);
                    SpectralDataTable.updateTableViewInTab(selectedTab, calibratedSeries.getData(), tableView);
                    System.out.println("Серия данных 'Intensities' откалибрована с дисперсией.");
                } else {
                    System.err.println("Серия данных 'Intensities' не найдена!");
                }
            } catch (NumberFormatException e) {
                System.err.println("Некорректное значение дисперсии.");
            }
        } else {
            // 2. Калибровка с двумя стандартами
            List<LineInfo> lines;
            if (linesFromPhotoRadioButton.isSelected()) {
                lines = lineImageInfos;
            } else {
                lines = lineChartInfos;
            }

            // 3. Поиск Kα1 и Kβ1 линий для двух элементов
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

            // 4. Обработка ошибок и получение энергий линий
            if (standard1Line == null || standard2Line == null) {
                System.err.println("Не найдены Kα1 и Kβ1 линии для двух элементов, Введите сначала Ka1, Потом Kb1");
                return;
            }
            double energyStandard1 = elementLinesEnergies.get(standard1Line.getElementName()).get("Ka1");
            double energyStandard2 = elementLinesEnergies.get(standard2Line.getElementName()).get("Kb1");

            // 5. Калибровка серии данных "Intensities"
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
                double[] calibratedEnergies = calibrateWithTwoPoints(energyStandard1, pixelStandard1,
                        energyStandard2, pixelSampleKBeta1, positions);

                // 6. Создание новой серии с откалиброванными энергиями
                XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
                calibratedSeries.setName("Calibrated Spectrum (Two Standards)");
                for (int i = 0; i < positions.length; i++) {
                    calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], intensitiesSeries.getData().get(i).getYValue()));
                }

                // 7. Обновление графика и таблицы
                currentChart.getData().remove(intensitiesSeries);
                currentChart.getData().add(calibratedSeries);
                NumberAxis xAxis = (NumberAxis) currentChart.getXAxis();
                xAxis.setAutoRanging(false);
                xAxis.setLowerBound(calibratedEnergies[0]);
                xAxis.setUpperBound(calibratedEnergies[calibratedEnergies.length - 1]);
                SpectralDataTable.updateTableViewInTab(selectedTab, calibratedSeries.getData(), tableView);
                System.out.println("Серия данных 'Intensities' откалибрована методом двух стандартов.");
            } else {
                System.err.println("Серия данных 'Intensities' не найдена!");
            }
        }
    }

    // Метод калибровки с двумя стандартами (Kα1 и Kβ1)
    private static double[] calibrateWithTwoPoints(double energyStandard, int pixelStandard,
                                                   double energySampleKBeta1, int pixelSampleKBeta1,
                                                   double[] spectrum) {
        // Вычисление разницы в пикселях между Kα1 и Kβ1
        int pixelDifference = Math.abs(pixelSampleKBeta1 - pixelStandard);

        // Вычисление разницы энергий между Kα1 и Kβ1
        double energyDifference = Math.abs(energySampleKBeta1 - energyStandard);

        // Вычисление угловой дисперсии
        double angularDispersion = pixelDifference / energyDifference;

        // Вычисление энергетической дисперсии
        double energyDispersion = 1 / angularDispersion;

        // Калибровка спектра
        double[] calibratedSpectrum = new double[spectrum.length];
        for (int i = 0; i < spectrum.length; i++) {
            calibratedSpectrum[i] = energyStandard + (spectrum[i] - pixelStandard) * energyDispersion;
        }
        return calibratedSpectrum;
    }

    // Метод калибровки с одним стандартом и дисперсией
    private static double[] calibrateWithDispersion(double energyStandard, double dispersion, int pixelStandard, double[] spectrum) {
        // Калибровка спектра
        double[] calibratedSpectrum = new double[spectrum.length];
        for (int i = 0; i < spectrum.length; i++) {
            calibratedSpectrum[i] = energyStandard + (spectrum[i] - pixelStandard) * dispersion;
        }
        return calibratedSpectrum;
    }


    // Калибровка методом линейной регрессии
    private void performLinearRegressionCalibration() {
        Map<String, Map<String, Double>> elementLinesEnergies = readElementLinesEnergiesFromFile("src/main/java/com/example/funproject/xray_lines_3d_metals.txt");
        List<LineInfo> lines;
        List<Double> knownPositions = new ArrayList<>();
        List<Double> knownEnergies = new ArrayList<>();
        if (linesFromPhotoRadioButton.isSelected()) {
            lines = lineImageInfos;
        } else {
            lines = lineChartInfos;
        }
        for (LineInfo lineInfo : lines) {
            String elementName = lineInfo.getElementName();
            String peakType = lineInfo.getPeakType();
            if (elementLinesEnergies.containsKey(elementName) && elementLinesEnergies.get(elementName).containsKey(peakType)) {
                knownPositions.add(lineInfo.getXPosition());
                knownEnergies.add(elementLinesEnergies.get(elementName).get(peakType));
            } else {
                System.err.println("Энергия линии не найдена для: " + elementName + " - " + peakType);
                return;
            }
        }

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
        double tickUnit = (maxCalibratedX - minCalibratedX) / 10; // пример: 10 меток
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
                String[] parts = line.split("\t");  // Разделяем строку по табуляции
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
}