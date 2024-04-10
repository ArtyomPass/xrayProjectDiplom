package com.example.funproject;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class SpectrometerCalibration {
    public static double[] linearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }
        double a = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double b = (sumY - a * sumX) / n;
        return new double[]{a, b};
    }


    public static double[] applyCalibrationCurve(double[] positions, double[] params) {
        double[] correctedEnergies = new double[positions.length];
        for (int i = 0; i < positions.length; i++) {
            correctedEnergies[i] = params[0] * positions[i] + params[1];
        }
        return correctedEnergies;
    }

    public static ContextMenu createInstrumentCalibrationMenu(){
        ContextMenu contextMenu = new ContextMenu();

        MenuItem importPhoto = new MenuItem("Импорт изображение");
        MenuItem importText = new MenuItem("Импорт текстового файла");

        //logic for import

        contextMenu.getItems().addAll(importPhoto, importText);
        return contextMenu;
    }
}
