package com.example.funproject;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DataPreprocessing {

    // Метод для сглаживания изображения (Box Blur)
    public Image imageSmoothing(Image originalImage, int kernelSize) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        // Создание WritableImage для хранения результата
        WritableImage blurredImage = new WritableImage(width, height);

        // Получение PixelReader и PixelWriter
        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = blurredImage.getPixelWriter();

        // Применение Box Blur
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color averageColor = calculateAverageColor(pixelReader, x, y, width, height, kernelSize);
                pixelWriter.setColor(x, y, averageColor);
            }
        }

        return blurredImage;
    }

    // Метод для вычисления среднего цвета в окрестности пикселя
    private Color calculateAverageColor(PixelReader pixelReader, int x, int y, int width, int height, int kernelSize) {
        double red = 0.0;
        double green = 0.0;
        double blue = 0.0;
        double opacity = 0.0;
        int count = 0;
        int halfKernel = kernelSize / 2;

        for (int ny = -halfKernel; ny <= halfKernel; ny++) {
            for (int nx = -halfKernel; nx <= halfKernel; nx++) {
                int currentX = x + nx;
                int currentY = y + ny;

                if (currentX >= 0 && currentX < width && currentY >= 0 && currentY < height) {
                    Color color = pixelReader.getColor(currentX, currentY);
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

    public WritableImage applyDensity(Image originalImage) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();
        WritableImage densityImage = new WritableImage(width, height);
        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = densityImage.getPixelWriter();

        double D = 2000;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color originalColor = pixelReader.getColor(x, y);

                // Вычисление среднего значения по каналам RGB для оригинального цвета
                double originalAverage = (originalColor.getRed() + originalColor.getGreen() + originalColor.getBlue()) / 3;
                int originalGrayValue = (int) (originalAverage * 255);

                // Применение формулы денситометрии
                double I = -D * Math.log10(1 - (originalAverage / D));

                // Нормализация значения интенсивности для нового изображения
                int processedGrayValue = (int) (I * 255 / (D * Math.log10(D / (D - 1))));
                processedGrayValue = Math.min(Math.max(processedGrayValue, 0), 255);

                Color newColor = Color.rgb(processedGrayValue, processedGrayValue, processedGrayValue);
                pixelWriter.setColor(x, y, newColor);

                // Вывод в консоль оригинального и обработанного серых значений
                System.out.println("Original Gray: " + originalGrayValue + ", Processed Gray: " + processedGrayValue);
            }
        }
        return densityImage;
    }

}