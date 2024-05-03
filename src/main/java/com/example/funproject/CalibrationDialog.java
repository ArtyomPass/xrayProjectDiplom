package com.example.funproject;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalibrationDialog extends Stage {

    private ComboBox<String> calibrationMethodComboBox;
    private TextField standard1Field;
    private TextField standard2Field;
    private RadioButton linesFromPhotoRadioButton;
    private RadioButton linesFromSpectrumRadioButton;
    private ToggleGroup linesSourceToggleGroup;
    private Button calibrateButton;
    private Button cancelButton;

    private List<LineInfo> lineImageInfos;
    private List<LineInfo> lineChartInfos;
    private LineChart<Number, Number> currentChart;
    private TableView<SpectralDataTable.SpectralData> tableView;

    public CalibrationDialog(List<LineInfo> lineImageInfos,
                             List<LineInfo> lineChartInfos,
                             LineChart<Number, Number> currentChart,
                             TableView<SpectralDataTable.SpectralData> tableView) {
        this.lineImageInfos = lineImageInfos;
        this.lineChartInfos = lineChartInfos;
        this.currentChart = currentChart;
        this.tableView = tableView;

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // ComboBox для выбора метода калибровки
        calibrationMethodComboBox = new ComboBox<>();
        calibrationMethodComboBox.getItems().addAll("Два стандарта", "Линейная регрессия");
        calibrationMethodComboBox.setValue("Два стандарта"); // Значение по умолчанию

        // Поля для ввода значений стандартов
        standard1Field = new TextField();
        standard1Field.setPromptText("Стандарт 1");
        standard2Field = new TextField();
        standard2Field.setPromptText("Стандарт 2");

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

        // Добавление элементов в VBox
        root.getChildren().addAll(
                new Label("Метод калибровки:"),
                calibrationMethodComboBox,
                standard1Field,
                standard2Field,
                linesFromPhotoRadioButton,
                linesFromSpectrumRadioButton,
                calibrateButton,
                cancelButton
        );

        // Установка начальной видимости полей ввода
        if ("Два стандарта".equals(calibrationMethodComboBox.getValue())) {
            standard1Field.setVisible(true);
            standard2Field.setVisible(true);
        } else {
            standard1Field.setVisible(false);
            standard2Field.setVisible(false);
        }


        calibrationMethodComboBox.setOnAction(this::handleComboBoxAction);
        calibrateButton.setOnAction(this::performCalibration);

        // Создание сцены и отображение окна
        Scene scene = new Scene(root);
        setScene(scene);
        this.setAlwaysOnTop(true);
        show();
    }

    private void performCalibration(ActionEvent actionEvent) {
        String selectedMethod = calibrationMethodComboBox.getValue();

        if ("Два стандарта".equals(selectedMethod)) {
            // Логика для калибровки с двумя стандартами
            double standard1Value = Double.parseDouble(standard1Field.getText());
            double standard2Value = Double.parseDouble(standard2Field.getText());

        } else if ("Линейная регрессия".equals(selectedMethod)) {
            Map<String, Map<String, Double>> elementLinesEnergies = readElementLinesEnergiesFromFile("src/main/java/com/example/funproject/xray_lines_3d_metals.txt");
            //System.out.println(elementLinesEnergies);

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
                    // Обработка случая, когда энергия линии не найдена
                    System.err.println("Энергия линии не найдена для: " + elementName + " - " + peakType);
                    return;
                }
            }

            // 2. Вычисление калибровочной кривой (пример - линейная регрессия)
            double[] xValues = knownPositions.stream().mapToDouble(Double::doubleValue).toArray();
            double[] yValues = knownEnergies.stream().mapToDouble(Double::doubleValue).toArray();
            double[] calibrationParams = linearRegression(xValues, yValues);

            // 3. Применение калибровки к спектру
            for (XYChart.Series<Number, Number> series : currentChart.getData()) {
                if (!series.getName().equals("Vertical Line")) {
                    // Извлечение данных из серии
                    double[] xValuesSeries = series.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .toArray();

                    // Применение калибровочной кривой
                    double[] correctedEnergies = applyCalibrationCurve(xValuesSeries, calibrationParams);

                    // Обновление данных в серии
                    for (int i = 0; i < series.getData().size(); i++) {
                        series.getData().get(i).setXValue(correctedEnergies[i]);
                    }
                }
            }
            // Удаление вертикальных линий
            removeVerticalLineSeries(currentChart);
        }
    }

    // Удаляет все вертикальные линии из серии данных на графике
    public static void removeVerticalLineSeries(XYChart<Number, Number> chart) {
        // 1. Создаем список для хранения серий, которые нужно удалить.
        List<XYChart.Series<Number, Number>> seriesToRemove = new ArrayList<>();

        // 2. Перебираем все серии в графике.
        for (XYChart.Series<Number, Number> series : chart.getData()) {
            // 3. Проверяем, называется ли серия "Vertical Lines".
            if (series.getName().equals("Vertical Line")) {
                // 4. Если да, добавляем серию в список для удаления.
                seriesToRemove.add(series);
            }
        }

        // 5. Удаляем все серии из списка из графика.
        chart.getData().removeAll(seriesToRemove);
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

    // Влияет на отображение нодов в зависимости от комбобокса
    private void handleComboBoxAction(ActionEvent event) {
        String selectedMethod = calibrationMethodComboBox.getValue();
        if ("Два стандарта".equals(selectedMethod)) {
            standard1Field.setVisible(true);
            standard2Field.setVisible(true);
            linesFromPhotoRadioButton.setVisible(false);
            linesFromSpectrumRadioButton.setVisible(false);
        } else if ("Линейная регрессия".equals(selectedMethod)) {
            standard1Field.setVisible(false);
            standard2Field.setVisible(false);
            linesFromPhotoRadioButton.setVisible(true);
            linesFromSpectrumRadioButton.setVisible(true);
        }
    }
}