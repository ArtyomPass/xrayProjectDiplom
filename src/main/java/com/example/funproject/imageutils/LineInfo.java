package com.example.funproject.imageutils;

import javafx.scene.image.Image;
import javafx.scene.shape.Line;

public class LineInfo {
    private Image image;
    private Line line;
    private double xPosition; // Координата X линии
    private String peakType; // Тип пика, например, "K-Alpha 1" or "K-Beta 2";

    public LineInfo(Image image, Line line, double xPosition, String peakType) {
        this.image = image;
        this.line = line;
        this.xPosition = xPosition;
        this.peakType = peakType;
    }

    // Геттеры и сеттеры
    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
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

    public String getPeakType() {
        return peakType;
    }

    public void setPeakType(String peakType) {
        this.peakType = peakType;
    }
}
