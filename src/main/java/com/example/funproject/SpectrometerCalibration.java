package com.example.funproject;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpectrometerCalibration {

    // =========================================================================
    // Методы для калибровки с использованием известных энергий линий
    // =========================================================================

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
                "K-Alpha 2", 5887.0,
                "K-Beta 1", 6490.0
        );
        List<Double> knownPositions = new ArrayList<>();
        List<Double> knownEnergies = new ArrayList<>();
        for (Image image : imageLines.keySet()) {
            for (LineInfo line : imageLines.get(image)) {
                if (line.getElementName().equals("Mn") && knownEnergiesMn.containsKey(line.getPeakType())) {
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
        XYChart.Series<Number, Number> series = spectralDataSeries.get(tabManager.innerTableAndChartTabPanes.get(tabPane
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
            spectralDataSeries.put(tabManager.innerTableAndChartTabPanes.get(tabPane
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

    // --- Вспомогательные методы для калибровки с использованием известных энергий линий ---

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


    // =========================================================================
    // Методы для калибровки методом двух стандартов
    // =========================================================================

    public static void calibrateWithTwoStandards(Tab currentTab, TabPane innerTabPane,
                                                 Map<Tab, XYChart.Series<Number, Number>> spectralDataSeries,
                                                 TableView<SpectralDataTable.SpectralData> spectralDataTableViews,
                                                 SpectraAnalysis spectraAnalysis,
                                                 double elementPosition,
                                                 double longWavelengthStandard1,
                                                 double longWavelengthStandard2,
                                                 TabPane tabPane,
                                                 TabManager tabManager) {

        // 1. Получите данные из серии
        double[] positions = spectralDataSeries.get(tabManager.innerTableAndChartTabPanes.get(tabPane
                        .getSelectionModel()
                        .getSelectedItem())
                .getSelectionModel()
                .getSelectedItem()).getData().stream().mapToDouble(d -> d.getXValue().doubleValue()).toArray();

        // 2. Выбор метода калибровки и необходимые данные
        boolean usingTwoPointsWithKBeta1 = true;  // Замените на false, если хотите использовать дисперсию
        double energyStandard1 = 6930.32;  // Kα1 линия первого стандарта (пример)
        int pixelStandard1 = 713;      // Положение пикселя Kα1 линии первого стандарта (пример)
        double energySampleKBeta1 = 7057.98; // Энергия Kβ1 линии исследуемого соединения (пример)
        int pixelSampleKBeta1 = 2570;      // Положение Kβ1 линии исследуемого соединения (пример)
        double dispersion = 0.3;           // Значение дисперсии (если используется)

        // 3. Выполните калибровку
        double[] calibratedEnergies;
        if (usingTwoPointsWithKBeta1) {
            calibratedEnergies = calibrateWithTwoPoints(energyStandard1, pixelStandard1,
                    energySampleKBeta1, pixelSampleKBeta1, positions);
        } else {
            calibratedEnergies = calibrateWithDispersion(energyStandard1, dispersion, pixelStandard1, positions);
        }

        // 4. Создайте новую серию данных с откалиброванными энергиями
        XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
        calibratedSeries.setName("Calibrated Spectrum (Two Standards)");
        for (int i = 0; i < positions.length; i++) {
            calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], spectralDataSeries.get(tabManager.innerTableAndChartTabPanes.get(tabPane
                            .getSelectionModel()
                            .getSelectedItem())
                    .getSelectionModel()
                    .getSelectedItem()).getData().get(i).getYValue()));
        }

        // 5. Обновите данные и визуализацию
        spectralDataSeries.put(tabManager.innerTableAndChartTabPanes.get(tabPane
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
        }
        System.out.println("Спектр откалиброван методом двух стандартов или с использованием дисперсии");
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

}