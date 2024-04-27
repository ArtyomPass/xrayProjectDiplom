package com.example.funproject;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

public class GraphData {
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> series;
    private String name; // Опционально: имя для графика

    public GraphData(LineChart<Number, Number> chart, XYChart.Series<Number, Number> series, String name) {
        this.chart = chart;
        this.series = series;
        this.name = name;
    }
}