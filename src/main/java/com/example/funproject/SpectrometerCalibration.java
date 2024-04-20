package com.example.funproject;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class SpectrometerCalibration {
    public static double[] linearRegression(double[] x, double[] y) {
        int n = x.length;
        System.out.println(n);
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }
        double denominator = n * sumXX - sumX * sumX;
        System.out.println("Denominator: " + denominator + " n = " + n + " sumXX = " + sumXX + " sumX = " + sumX);


        double a = (n * sumXY - sumX * sumY) / denominator;
        double b = (sumY - a * sumX) / n;
        System.out.println("a = " + a + ", b = " + b);
        return new double[]{a, b};
    }



    public static double[] applyCalibrationCurve(double[] positions, double[] params) {
        double[] correctedEnergies = new double[positions.length];
        for (int i = 0; i < positions.length; i++) {
            correctedEnergies[i] = params[0] * positions[i] + params[1];
        }
        return correctedEnergies;
    }

}
