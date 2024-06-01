package com.example.funproject;

import javafx.scene.chart.XYChart;
import javafx.scene.shape.Line;

public class LineInfo {

    private XYChart.Series<Number, Number> series; // Добавленное поле
    private Line line;
    private double xPosition; // Координата X линии
    private String standardType; // Используем стандартType вместо peakType
    private String elementName;
    private double angle; // Угол

    public LineInfo(XYChart.Series<Number, Number> series,
                    Line line,
                    double xPosition,
                    String standardType,
                    String elementName,
                    double angle) {
        this.series = series; // Инициализация series
        this.line = line;
        this.xPosition = xPosition;
        this.standardType = standardType; // Инициализация стандартType
        this.elementName = elementName;
        this.angle = angle; // Инициализация угла
    }

    // Геттеры и сеттеры

    public XYChart.Series<Number, Number> getSeries() {
        return series;
    }

    public void setSeries(XYChart.Series<Number, Number> series) {
        this.series = series;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public double getXPosition() {
        return xPosition;
    }

    public void setXPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public String getStandardType() {
        return standardType;
    }

    public void setStandardType(String standardType) {
        this.standardType = standardType;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "LineInfo{" +
                "series=" + series +
                ", line=" + line +
                ", xPosition=" + xPosition +
                ", standardType='" + standardType + '\'' +
                ", elementName='" + elementName + '\'' +
                ", angle=" + angle +
                '}';
    }
}
