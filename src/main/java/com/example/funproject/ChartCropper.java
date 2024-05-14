package com.example.funproject;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class ChartCropper {
    private LineChart<Number, Number> lineChart;
    private double percentage; // Процент от пика для обрезания

    public ChartCropper(LineChart<Number, Number> lineChart, double percentage) {
        this.lineChart = lineChart;
        this.percentage = percentage / 100; // Преобразуем проценты в десятичную дробь
    }

    public void cropChart() {
        if (lineChart.getData().isEmpty()) {
            return; // Ничего не делаем, если график пуст
        }

        // Находим последнюю серию данных
        XYChart.Series<Number, Number> series = lineChart.getData().get(lineChart.getData().size() - 1);

        // Находим X-значение пика
        double peakX = findPeakX(series);

        // Вычисляем X-значение для обрезания
        double cropX = peakX + (peakX * percentage);

        // Обрезаем данные
        series.getData().removeIf(data -> data.getXValue().doubleValue() < cropX);

        // Обновляем границы графика
        updateChartBounds(series);
    }

    private void updateChartBounds(XYChart.Series<Number, Number> series) {
        if (series.getData().isEmpty()) {
            return;
        }
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(series.getData().get(0).getXValue().doubleValue());
        xAxis.setUpperBound(series.getData().get(series.getData().size() - 1).getXValue().doubleValue());
    }

    private double findPeakX(XYChart.Series<Number, Number> series) {
        double peakX = 0;
        double maxValue = Double.MIN_VALUE;
        for (XYChart.Data<Number, Number> data : series.getData()) {
            if (data.getYValue().doubleValue() > maxValue) {
                maxValue = data.getYValue().doubleValue();
                peakX = data.getXValue().doubleValue();
            }
        }
        return peakX;
    }
}