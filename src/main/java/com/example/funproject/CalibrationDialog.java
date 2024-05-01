package com.example.funproject;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalibrationDialog extends Stage {

    private ComboBox<String> calibrationMethodComboBox;
    private Button calibrateButton;

    public CalibrationDialog() {
        // Создаем ComboBox для выбора метода калибровки
        ComboBox<String> calibrationMethodComboBox = new ComboBox<>();
        calibrationMethodComboBox.getItems().addAll("Линейная регрессия", "Метод двух стандартов");

        // Создаем кнопку "ОК"
        Button okButton = new Button("OK");
        okButton.setOnAction(event -> {
            // Здесь будет обработка выбранного метода калибровки
            String selectedMethod = calibrationMethodComboBox.getValue();
            calibrateSpectrum();

            // Закрываем диалоговое окно
            this.close();
        });

        // Создаем layout (например, VBox)
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(calibrationMethodComboBox, okButton);

        // Создаем сцену и устанавливаем ее в Stage
        Scene scene = new Scene(layout);
        this.setScene(scene);

        // Настраиваем Stage (заголовок, модальный режим и т.д.)
        this.setTitle("Калибровка спектрометра");
        this.setAlwaysOnTop(true);
    }

    private void calibrateSpectrum() {


    }

    public static void calibrateSpectrum(Tab currentTab,
                                         TabPane innerTabPane,
                                         Map<Image, List<LineInfo>> imageLines,
                                         Map<Tab, XYChart.Series<Number, Number>> spectralDataSeries,
                                         TableView<SpectralDataTable.SpectralData> spectralDataTableViews,
                                         SpectraAnalysis spectraAnalysis,
                                         TabManager tabManager,
                                         TabPane tabPane) {

        // 1. Подготовка данных для калибровки
        Map<String, Double> knownEnergiesMn = Map.of(
                "K-Alpha 1", 5898.0,
                "Mn K-Alpha 2", 5887.0,
                "Mn K-Beta 1", 6490.0
        ); // TODO: Здесь просто подключим загрузку из файла

        List<Double> knownPositions = new ArrayList<>();
        List<Double> knownEnergies = new ArrayList<>();
        for (Image image : imageLines.keySet()) {   // TODO: Взять из selectedImage
            for (LineInfo line : imageLines.get(image)) {
                if (line.getElementName().equals("Mn") && knownEnergiesMn.containsKey("Mn " + line.getPeakType())) {
                    knownPositions.add(line.getXPosition());
                    knownEnergies.add(knownEnergiesMn.get(line.getPeakType()));
                }
            }
        }



        if (knownPositions.size() < 2) {
            System.out.println("Недостаточно данных для калибровки. Нужно как минимум два пика.");
            return;
        }

        // 2. Вычисление калибровочной кривой (пример - линейная регрессия)
        double[] xValues = knownPositions.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yValues = knownEnergies.stream().mapToDouble(Double::doubleValue).toArray();
        double[] calibrationParams = linearRegression(xValues, yValues);

        // 3. Применение калибровки к спектру
        XYChart.Series<Number, Number> series = spectralDataSeries.get(tabManager.innerTabPanes.get(tabPane
                        .getSelectionModel()
                        .getSelectedItem())
                .getSelectionModel()
                .getSelectedItem());
        ;
        if (series != null) {
            double[] positions = series.getData().stream().mapToDouble(d -> d.getXValue().doubleValue()).toArray();
            double[] calibratedEnergies = applyCalibrationCurve(positions, calibrationParams);
            XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
            calibratedSeries.setName("Calibrated Spectrum");
            for (int i = 0; i < positions.length; i++) {
                calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], series.getData().get(i).getYValue()));
            }

            // 4. Обновление данных и визуализация
            spectralDataSeries.put(tabManager.innerTabPanes.get(tabPane
                            .getSelectionModel()
                            .getSelectedItem())
                    .getSelectionModel()
                    .getSelectedItem(), calibratedSeries);

            SpectralDataTable.updateTableViewInTab(currentTab, calibratedSeries.getData(), spectralDataTableViews);
            LineChart<Number, Number> chart = spectraAnalysis.getLineChartFromTab(currentTab, innerTabPane);
            if (chart != null) {
                NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                xAxis.setAutoRanging(false);
                xAxis.setLowerBound(calibratedEnergies[0]);
                xAxis.setUpperBound(calibratedEnergies[calibratedEnergies.length - 1]);
                chart.getData().clear();
                chart.getData().add(calibratedSeries);
            } else {
                System.out.println("Ошибка: не найден график для вкладки.");
            }
        } else {
            System.out.println("Ошибка: не найдены данные спектра для вкладки.");
        }
    }

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

    private static double[] applyCalibrationCurve(double[] positions, double[] params) {
        double[] correctedEnergies = new double[positions.length];
        for (int i = 0; i < positions.length; i++) {
            correctedEnergies[i] = params[0] * positions[i] + params[1];
        }
        return correctedEnergies;
    }

}
