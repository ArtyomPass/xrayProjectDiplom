package com.example.funproject;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class SpectraAnalysis {

    public SpectraAnalysis() {
        // Empty
    }

    public List<XYChart.Data<Number, Number>> visualizePeaks(Tab tab,
                                                             TabPane innerTabPane,
                                                             XYChart.Series<Number, Number> series,
                                                             double threshold,
                                                             int windowSize,
                                                             double minPeakDistance) {
        LineChart<Number, Number> chart = getLineChartFromTab(tab, innerTabPane); // Используем innerTabPane для поиска
        List<XYChart.Data<Number, Number>> detectedPeaks = new ArrayList<>();
        if (chart != null && series != null) {
            detectedPeaks = detectPeaks(series, threshold, windowSize, minPeakDistance);
            for (XYChart.Data<Number, Number> peak : detectedPeaks) {
                XYChart.Series<Number, Number> peakSeries = new XYChart.Series<>();
                peakSeries.getData().add(new XYChart.Data<>(peak.getXValue(), peak.getYValue()));
                Node peakNode = new Circle(4);
                peakNode.setStyle("-fx-fill: red;");
                peakSeries.getData().get(0).setNode(peakNode);
                chart.getData().add(peakSeries);
                chart.setLegendVisible(false);
                chart.setCreateSymbols(false);
            }
        }
        return detectedPeaks;
    }

    public XYChart.Series<Number, Number> updateChartWithSplineData(Tab tab,
                                                                    Image image,
                                                                    TabPane innerTabPane) {
        LineChart<Number, Number> chart = getLineChartFromTab(tab, innerTabPane);
        XYChart.Series<Number, Number> series = null;
        if (chart != null && image != null) {
            series = processImageForSplineData(image);
            // chart.getData().clear();
            chart.getData().add(series);
            // series.getNode().setStyle("-fx-stroke: gray;");
            chart.setCreateSymbols(false); // Отключаем создание символов
        }
        return series;
    }

    public LineChart<Number, Number> getLineChartFromTab(Tab tab,
                                                         TabPane innerTabPane) {
        Tab currentGraphicTab = innerTabPane.getSelectionModel().getSelectedItem();
        System.out.println("here" + currentGraphicTab);
        if (innerTabPane != null) {
            if (currentGraphicTab.getContent() instanceof LineChart) {
                return (LineChart<Number, Number>) currentGraphicTab.getContent();
            }
        }
        return null; // Возвращаем null, если график не найден
    }

    private List<XYChart.Data<Number, Number>> detectPeaks(XYChart.Series<Number, Number> series,
                                                           double threshold,
                                                           int windowSize,
                                                           double minPeakDistance) {
        List<XYChart.Data<Number, Number>> peaks = new ArrayList<>();
        double lastPeakX = Double.MIN_VALUE;
        for (int i = windowSize; i < series.getData().size() - windowSize; i++) {
            XYChart.Data<Number, Number> current = series.getData().get(i);
            boolean isPeak = true;
            for (int j = -windowSize; j <= windowSize; j++) {
                if (j == 0) continue;
                XYChart.Data<Number, Number> neighbor = series.getData().get(i + j);
                if (current.getYValue().doubleValue() <= neighbor.getYValue().doubleValue()) {
                    isPeak = false;
                    break;
                }
            }
            if (isPeak && current.getYValue().doubleValue() > threshold && (current.getXValue().doubleValue() - lastPeakX) >= minPeakDistance) {
                peaks.add(current);
                lastPeakX = current.getXValue().doubleValue();
            }
        }
        return peaks;
    }

    private XYChart.Series<Number, Number> processImageForSplineData(Image image) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Intensities");
        if (image != null && image.getPixelReader() != null) {
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            for (int x = 0; x < width; x++) {
                double totalIntensity = 0;
                for (int y = 0; y < height; y++) {
                    Color color = pixelReader.getColor(x, y);
                    double intensity = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                    totalIntensity += intensity;
                }
                double averageIntensity = totalIntensity / height;
                series.getData().add(new XYChart.Data<>(x, averageIntensity * 100));
            }
        }
        return series;
    }
}