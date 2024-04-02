package com.example.funproject;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class SpectraAnalysis {

    public SpectraAnalysis() {
        // Empty
    }

    private LineChart<Number, Number> getLineChartFromTab(Tab tab) {
        if (tab.getContent() instanceof SplitPane) {
            SplitPane splitPane = (SplitPane) tab.getContent();
            for (Node item : splitPane.getItems()) {
                if (item instanceof SplitPane) {
                    for (Node innerItem : ((SplitPane) item).getItems()) {
                        if (innerItem instanceof LineChart) {
                            return (LineChart<Number, Number>) innerItem;
                        }
                    }
                }
            }
        }
        return null;
    }

    public XYChart.Series<Number, Number> updateChartWithSplineData(Tab tab, Image image) {
        LineChart<Number, Number> chart = getLineChartFromTab(tab);
        XYChart.Series<Number, Number> series = null;
        if (chart != null && image != null) {
            series = processImageForPeaks(image);
            chart.getData().clear();
            chart.getData().add(series);
        }
        return series;
    }

    private XYChart.Series<Number, Number> processImageForPeaks(Image image) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Peak Intensities");
        if (image != null && image.getPixelReader() != null) {
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            int rowToProcess = height / 2;
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, rowToProcess);
                double intensity = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                series.getData().add(new XYChart.Data<>(x, intensity * 100));
            }
        }
        return series;
    }

    public void visualizePeaks(Tab tab, XYChart.Series<Number, Number> series, double threshold) {
        LineChart<Number, Number> chart = getLineChartFromTab(tab);
        if (chart != null && series != null) {
            List<XYChart.Data<Number, Number>> peaks = detectPeaks(series, threshold);
            for (XYChart.Data<Number, Number> peak : peaks) {
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
    }

    private List<XYChart.Data<Number, Number>> detectPeaks(XYChart.Series<Number, Number> series, double threshold) {
        List<XYChart.Data<Number, Number>> peaks = new ArrayList<>();
        for (int i = 10; i < series.getData().size() - 10; i++) {
            XYChart.Data<Number, Number> current = series.getData().get(i);
            XYChart.Data<Number, Number> previous = series.getData().get(i - 10);
            XYChart.Data<Number, Number> next = series.getData().get(i + 10);
            if (current.getYValue().doubleValue() > threshold &&
                    current.getYValue().doubleValue() > previous.getYValue().doubleValue() &&
                    current.getYValue().doubleValue() > next.getYValue().doubleValue()) {
                peaks.add(current);
            }
        }
        return peaks;
    }

}