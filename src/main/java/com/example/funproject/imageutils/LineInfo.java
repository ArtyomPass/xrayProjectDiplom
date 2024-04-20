package com.example.funproject.imageutils;

import javafx.scene.image.Image;
import javafx.scene.shape.Line;

public class LineInfo {

    private Line line;
    private double xPosition; // Координата X линии
    private String peakType; // Тип пика, например, "K-Alpha 1" or "K-Beta 2";
    private String elementName;

    public LineInfo( Line line, double xPosition, String peakType, String elementName) {
        this.line = line;
        this.xPosition = xPosition;
        this.peakType = peakType;
        this.elementName = elementName;
    }

    // Геттеры и сеттеры


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

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
}
