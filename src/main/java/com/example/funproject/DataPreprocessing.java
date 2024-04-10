package com.example.funproject;

import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.List;

public class DataPreprocessing {

    public Image imageSmoothing(Image originalImage, int kernelSize) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();
        return applyBoxBlur(originalImage, width, height, kernelSize);
    }

    private WritableImage applyBoxBlur(Image image, int width, int height, int kernelSize) {
        WritableImage blurredImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color avgColor = calculateAverateColor(pixelReader, x, y, width, height, kernelSize);
                blurredImage.getPixelWriter().setColor(x, y, avgColor);
            }
        }
        return blurredImage;
    }

    private Color calculateAverateColor(PixelReader pixelReader, int x, int y, int width, int height, int kernelSize) {
        double red = 0.0;
        double green = 0.0;
        double blue = 0.0;
        double opacity = 0.0;
        int count = 0;
        int halfKernel = kernelSize / 2;

        for (int ny = -halfKernel; ny <= halfKernel; ny++) {
            for (int nx = -halfKernel; nx <= halfKernel; nx++) {
                if (x + nx >= 0 && x + nx < width && y + ny >= 0 && y + ny < height) {
                    Color color = pixelReader.getColor(x + nx, y + ny);
                    red += color.getRed();
                    green += color.getGreen();
                    blue += color.getBlue();
                    opacity += color.getOpacity();
                    count++;
                }
            }
        }
        return new Color(red / count, green / count, blue / count, opacity / count);
    }

    public XYChart.Series<Number, Number> calibrateEnergy(XYChart.Series<Number, Number> spectrum,
                                                          List<XYChart.Series<Number, Number>> referenceSpectra) {
        XYChart.Series<Number, Number> calibratedSpectrum = new XYChart.Series<>();
        calibratedSpectrum.setName("Calibrated Spectrum");
        for (XYChart.Data<Number, Number> dataPoint : spectrum.getData()) {
            double averageEnergy = 0;
            int count = 0;
            for (XYChart.Series<Number, Number> refSpectrum : referenceSpectra) {
                double closestEnergy = findClosestByIntensity(refSpectrum, dataPoint.getYValue().doubleValue());
                averageEnergy += closestEnergy;
                count++;
            }
            double calibratedEnergy = averageEnergy / count;
            calibratedSpectrum.getData().add(new XYChart.Data<>(calibratedEnergy, dataPoint.getYValue()));
        }
        return calibratedSpectrum;
    }

    private double findClosestByIntensity(XYChart.Series<Number, Number> refSpectrum, double intensity) {
        double closestEnergy = 0;
        double minDiff = Double.MAX_VALUE;
        for (XYChart.Data<Number, Number> refPoint : refSpectrum.getData()) {
            double currentDiff = Math.abs(refPoint.getYValue().doubleValue() - intensity);
            if (currentDiff < minDiff) {
                minDiff = currentDiff;
                closestEnergy = refPoint.getXValue().doubleValue();
            }
        }
        return closestEnergy;
    }

}
