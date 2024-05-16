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
    private ComboBox<String> calibrationMethodComboBox;
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

        // --- Комбобокс для выбора метода калибровки ---
        calibrationMethodComboBox = new ComboBox<>();
        calibrationMethodComboBox.getItems().addAll("Два стандарта", "Линейная регрессия");
        calibrationMethodComboBox.setValue("Два стандарта");

        // --- Радиокнопки для выбора источника линий ---
        linesSourceToggleGroup = new ToggleGroup();
        linesFromPhotoRadioButton = new RadioButton("Линии с фотографии");
        linesFromPhotoRadioButton.setToggleGroup(linesSourceToggleGroup);
        linesFromSpectrumRadioButton = new RadioButton("Линии со спектра");
        linesFromSpectrumRadioButton.setToggleGroup(linesSourceToggleGroup);

        // --- Поля для ввода порядка отражения и межплоскостного расстояния ---
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

        // --- Кнопки ---
        calibrateButton = new Button("Калибровать");
        dispersionButton = new Button("Дисперсия");
        dispersionField = new TextField();
        dispersionField.setPromptText("Значение дисперсии");

        // --- Инициализация меток порядка отражения ---
        orderStandardLabel = new Label("Порядок отражения (стандарт):");
        orderSampleLabel = new Label("Порядок отражения (образец):");

        // --- Label для межплоскостного расстояния ---
        dSpacingLabel = new Label("Значение межплоскостного расстояния:");

        // --- Добавление элементов в VBox ---
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

        // --- Установка начальной видимости полей ввода ---
        updateInputFieldsVisibility();

        // --- Обработчики событий ---
        calibrationMethodComboBox.setOnAction(this::handleComboBoxAction);
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

    private void updateInputFieldsVisibility() {
        boolean isTwoStandardsMethod = "Два стандарта".equals(calibrationMethodComboBox.getValue());
        orderStandardField.setVisible(isTwoStandardsMethod);
        orderSampleField.setVisible(isTwoStandardsMethod);
        dSpacingField.setVisible(isTwoStandardsMethod);
        dSpacingLabel.setVisible(isTwoStandardsMethod);
        orderStandardLabel.setVisible(isTwoStandardsMethod);
        orderSampleLabel.setVisible(isTwoStandardsMethod);
        dispersionButton.setVisible(isTwoStandardsMethod);
        dispersionField.setVisible(false);
        dispersionLabel.setVisible(false);
    }

    private void performCalibration(ActionEvent actionEvent) {
        String selectedMethod = calibrationMethodComboBox.getValue();
        double[] calibratedEnergies = null;

        if ("Два стандарта".equals(selectedMethod)) {
            calibratedEnergies = performTwoStandardsCalibration();
        } else if ("Линейная регрессия".equals(selectedMethod)) {
            calibratedEnergies = performLinearRegressionCalibration();
        }

        if (calibratedEnergies != null) {
            updateChartAndTable(calibratedEnergies);
        }

        this.close();
    }

    private double[] performTwoStandardsCalibration() {
        Map<String, Map<String, Double>> elementLinesEnergies = readElementLinesEnergiesFromFile(
                "src/main/java/com/example/funproject/xray_lines_3d_metals.txt");

        int orderStandard, orderSample;
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
            return null;
        }

        List<LineInfo> lines = linesFromPhotoRadioButton.isSelected() ? lineImageInfos : lineChartInfos;
        if (lines == null) {
            showErrorDialog("Ошибка калибровки", "Список линий не инициализирован.");
            return null;
        }

        if (dispersionButton.isVisible() && dispersionField.isVisible()) {
            try {
                double dispersion = Double.parseDouble(dispersionField.getText());
                if (dispersion <= 0) {
                    throw new NumberFormatException();
                }

                LineInfo standard1Line = lines.stream()
                        .filter(line -> line.getPeakType().equals("Ka1"))
                        .findFirst()
                        .orElse(null);

                if (standard1Line == null) {
                    showErrorDialog("Ошибка калибровки", "Не найдена Kα1 линия.");
                    return null;
                }

                double energyStandard1 = elementLinesEnergies.get(standard1Line.getElementName()).get("Ka1");
                int pixelStandard1 = (int) standard1Line.getXPosition();

                Optional<XYChart.Series<Number, Number>> intensitiesSeriesOptional = currentChart.getData().stream()
                        .filter(series -> series.getName().equals("Intensities"))
                        .findFirst();
                if (intensitiesSeriesOptional.isPresent()) {
                    XYChart.Series<Number, Number> intensitiesSeries = intensitiesSeriesOptional.get();
                    double[] positions = intensitiesSeries.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .toArray();
                    return calibrateWithDispersion(energyStandard1, dispersion, pixelStandard1, positions, orderStandard);
                } else {
                    showErrorDialog("Ошибка калибровки", "Серия данных 'Intensities' не найдена!");
                    return null;
                }
            } catch (NumberFormatException e) {
                showErrorDialog("Ошибка ввода", "Некорректное значение дисперсии.");
                return null;
            }
        } else {
            LineInfo standard1Line = null;
            LineInfo standard2Line = null;
            for (LineInfo line : lines) {
                if (line.getPeakType().equals("Ka1") && standard1Line == null) {
                    standard1Line = line;
                } else if (line.getPeakType().equals("Kb1") && standard2Line == null) {
                    standard2Line = line;
                }
                if (standard1Line != null && standard2Line != null) {
                    break;
                }
            }

            if (standard1Line == null || standard2Line == null) {
                showErrorDialog("Ошибка калибровки", "Не найдены Kα1 и Kβ1 линии для двух элементов.");
                return null;
            }

            double energyStandard1 = elementLinesEnergies.get(standard1Line.getElementName()).get("Ka1");
            double energyStandard2 = elementLinesEnergies.get(standard2Line.getElementName()).get("Kb1");

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

                return calibrateWithTwoPoints(energyStandard1, pixelStandard1, energyStandard2, pixelSampleKBeta1, positions, orderStandard, orderSample, dSpacing);
            } else {
                showErrorDialog("Ошибка калибровки", "Серия данных 'Intensities' не найдена!");
                return null;
            }
        }
    }

    private double[] calibrateWithTwoPoints(double energyKa1, int pixelKa1,
                                            double energyKb1, int pixelKb1,
                                            double[] spectrum, int orderStandard, int orderSample, double dSpacing) {
        double h = 6.62607015E-34;
        double c = 299792458;
        double energyToJoules = 1.602E-19;

        double lambdaKa1 = h * c / (energyKa1 * energyToJoules);
        double lambdaKb1 = h * c / (energyKb1 * energyToJoules);

        double thetaKa1 = Math.asin(orderStandard * lambdaKa1 / (2 * dSpacing));
        double thetaKb1 = Math.asin(orderSample * lambdaKb1 / (2 * dSpacing));

        double k = (thetaKb1 - thetaKa1) / (pixelKb1 - pixelKa1);
        double b = thetaKa1 - k * pixelKa1;

        double[] calibratedSpectrum = new double[spectrum.length];
        for (int i = 0; i < spectrum.length; i++) {
            double theta = k * i + b;
            double lambda = 2 * dSpacing * Math.sin(theta) / orderSample;
            calibratedSpectrum[i] = h * c / (lambda * energyToJoules);
        }
        return calibratedSpectrum;
    }

    private double[] calibrateWithDispersion(double energyStandard, double measuredDispersion,
                                             int pixelStandard, double[] spectrum, int order) {
        double trueDispersion = measuredDispersion / order;
        double[] calibratedSpectrum = new double[spectrum.length];
        for (int i = 0; i < spectrum.length; i++) {
            calibratedSpectrum[i] = energyStandard + (spectrum[i] - pixelStandard) * trueDispersion;
        }
        return calibratedSpectrum;
    }

    private double[] performLinearRegressionCalibration() {
        Map<String, Map<String, Double>> elementLinesEnergies = readElementLinesEnergiesFromFile(
                "src/main/java/com/example/funproject/xray_lines_3d_metals.txt");
        List<LineInfo> lines = linesFromPhotoRadioButton.isSelected() ? lineImageInfos : lineChartInfos;
        if (lines == null) {
            showErrorDialog("Ошибка калибровки", "Список линий не инициализирован.");
            return null;
        }

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
                return null;
            }
        }

        double[] xValues = knownPositions.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yValues = knownEnergies.stream().mapToDouble(Double::doubleValue).toArray();
        double[] calibrationParams = linearRegression(xValues, yValues);

        for (XYChart.Series<Number, Number> series : currentChart.getData()) {
            if (!series.getName().equals("Vertical Line")) {
                double[] xValuesSeries = series.getData().stream()
                        .mapToDouble(data -> data.getXValue().doubleValue())
                        .toArray();
                return applyCalibrationCurve(xValuesSeries, calibrationParams);
            }
        }
        return null;
    }

    private double[] linearRegression(double[] x, double[] y) {
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

    private double[] applyCalibrationCurve(double[] positions, double[] params) {
        double[] correctedEnergies = new double[positions.length];
        for (int i = 0; i < positions.length; i++) {
            correctedEnergies[i] = params[0] * positions[i] + params[1];
        }
        return correctedEnergies;
    }

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

    private void handleComboBoxAction(ActionEvent event) {
        updateInputFieldsVisibility();
    }

    private void updateChartAndTable(double[] calibratedEnergies) {
        Optional<XYChart.Series<Number, Number>> intensitiesSeriesOptional = currentChart.getData().stream()
                .filter(series -> series.getName().equals("Intensities"))
                .findFirst();

        if (intensitiesSeriesOptional.isPresent()) {
            XYChart.Series<Number, Number> intensitiesSeries = intensitiesSeriesOptional.get();
            XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
            calibratedSeries.setName("Intensities");
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
            removeSeriesByName(currentChart, "Vertical Line");
        } else {
            showErrorDialog("Ошибка калибровки", "Серия данных 'Intensities' не найдена!");
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
