package com.example.funproject;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class SpectraAnalysis {
    public SpectraAnalysis() {
        // Empty
    }

    public LineChart<Number, Number> getLineChartFromTab(Tab tab) {
        if (tab.getContent() instanceof SplitPane) {
            SplitPane mainSplitPane = (SplitPane) ((SplitPane) tab.getContent()).getItems().get(0);
            for (Node node : mainSplitPane.getItems()) {
                if (node instanceof LineChart) {
                    return (LineChart<Number, Number>) node;
                }
            }
        }
        return null;
    }

    public XYChart.Series<Number, Number> processImageForPeaks(Image image) {
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


}
