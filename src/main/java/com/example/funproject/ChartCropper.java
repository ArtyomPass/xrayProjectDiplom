package com.example.funproject;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Iterator;

public class ChartCropper {
    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> verticalLineSeries;
    private XYChart.Series<Number, Number> peakSeries;
    private Runnable onCropComplete;

    /**
     * Конструктор класса ChartCropper
     */
    public ChartCropper(LineChart<Number, Number> lineChart, Runnable onCropComplete) {
        this.lineChart = lineChart;
        this.onCropComplete = onCropComplete;
        this.verticalLineSeries = new XYChart.Series<>();
        verticalLineSeries.setName("Линия обрезки");

        this.peakSeries = new XYChart.Series<>();
        peakSeries.setName("Локальные пики");

        lineChart.setCreateSymbols(false); // Отключить символы для основной серии
        lineChart.getData().add(verticalLineSeries);
        lineChart.getData().add(peakSeries);
        lineChart.setAnimated(false); // Отключить анимацию
        lineChart.setLegendVisible(false); // Отключить отображение легенды

        // Убедитесь, что оси не изменяются автоматически
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        xAxis.setAutoRanging(false);

        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        yAxis.setAutoRanging(false);
    }

    /**
     * Метод для выхода из режима обрезки
     */
    public void exitCroppingMode() {
        lineChart.setOnMouseMoved(null);
        lineChart.setOnMouseClicked(null);
        lineChart.getData().remove(verticalLineSeries);
        if (onCropComplete != null) {
            onCropComplete.run();
        }
    }

    /**
     * Метод для включения режима обрезки
     */
    public void enableChartCropping() {
        lineChart.setOnMouseEntered(event -> verticalLineSeries.getNode().setVisible(true));
        lineChart.setOnMouseExited(event -> verticalLineSeries.getNode().setVisible(false));
        lineChart.setOnMouseMoved(this::handleMouseEvent);
        lineChart.setOnMouseClicked(this::handleMouseEvent);

        // Изначально скрыть вертикальную линию
        verticalLineSeries.getNode().setVisible(false);

        // Установить стиль для вертикальной линии
        verticalLineSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: red; -fx-stroke-width: 1px;");
    }

    /**
     * Метод для обработки событий мыши
     */
    private void handleMouseEvent(MouseEvent event) {
        double mouseX = event.getSceneX();
        double localX = lineChart.getXAxis().sceneToLocal(mouseX, 0).getX();
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        Number xValue = xAxis.getValueForDisplay(localX);

        if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
            // Очистка предыдущих данных линии
            verticalLineSeries.getData().clear();

            // Добавление новых точек для вертикальной линии
            verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getLowerBound()));
            verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getUpperBound()));
        } else if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton() == MouseButton.PRIMARY) {
            // Обрезка данных серий
            for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                if (!series.equals(verticalLineSeries) && !series.equals(peakSeries)) {
                    Iterator<XYChart.Data<Number, Number>> iterator = series.getData().iterator();
                    while (iterator.hasNext()) {
                        XYChart.Data<Number, Number> data = iterator.next();
                        if (data.getXValue().doubleValue() < xValue.doubleValue()) {
                            iterator.remove();
                        }
                    }
                }
            }

            // Вызов окна для ввода параметров threshold и distance
            ParametersWindow parametersWindow = new ParametersWindow(this);
            parametersWindow.showAndWait();

            // Установка нового нижнего предела для оси X
            xAxis.setLowerBound(xValue.doubleValue());
            xAxis.setAutoRanging(false);

            // Фиксация диапазона оси Y для предотвращения изменений
            yAxis.setLowerBound(yAxis.getLowerBound());
            yAxis.setUpperBound(yAxis.getUpperBound());
            yAxis.setAutoRanging(false);

            // Удаление вертикальной линии как серии данных и выход из режима обрезки
            lineChart.getData().remove(verticalLineSeries);
            exitCroppingMode();
        }
    }

    /**
     * Метод для выделения локальных пиков
     */
    public void highlightLocalPeaks(double threshold, int distance) {
        peakSeries.getData().clear();
        for (XYChart.Series<Number, Number> series : lineChart.getData()) {
            if (!series.equals(verticalLineSeries) && !series.equals(peakSeries)) {
                for (int i = distance; i < series.getData().size() - distance; i++) {
                    XYChart.Data<Number, Number> currentData = series.getData().get(i);

                    boolean isPeak = true;
                    for (int j = 1; j <= distance; j++) {
                        XYChart.Data<Number, Number> prevData = series.getData().get(i - j);
                        XYChart.Data<Number, Number> nextData = series.getData().get(i + j);

                        if (currentData.getYValue().doubleValue() <= prevData.getYValue().doubleValue() ||
                                currentData.getYValue().doubleValue() <= nextData.getYValue().doubleValue() ||
                                currentData.getYValue().doubleValue() < threshold) {
                            isPeak = false;
                            break;
                        }
                    }

                    if (isPeak) {
                        System.out.println("Локальный пик найден на X: " + currentData.getXValue() + ", Y: " + currentData.getYValue());
                        XYChart.Data<Number, Number> peakData = new XYChart.Data<>(currentData.getXValue(), currentData.getYValue());
                        Circle circle = new Circle(5, Color.BLUE);
                        peakData.setNode(circle);
                        peakSeries.getData().add(peakData);
                    }
                }
            }
        }

        peakSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: transparent;");
    }

    /**
     * Внутренний класс для окна ввода параметров
     */
    public class ParametersWindow extends Stage {
        public ParametersWindow(ChartCropper cropper) {
            setTitle("Ввод параметров пиков");
            initModality(Modality.APPLICATION_MODAL);

            Label thresholdLabel = new Label("Порог:");
            TextField thresholdField = new TextField("11.0");

            Label distanceLabel = new Label("Дистанция:");
            TextField distanceField = new TextField("10");

            Button okButton = new Button("ОК");
            okButton.setOnAction(event -> {
                try {
                    double threshold = Double.parseDouble(thresholdField.getText().trim());
                    int distance = Integer.parseInt(distanceField.getText().trim());
                    cropper.highlightLocalPeaks(threshold, distance);
                    close();
                } catch (NumberFormatException e) {
                    System.err.println("Неверный ввод: " + e.getMessage());
                }
            });

            Button cancelButton = new Button("Отмена");
            cancelButton.setOnAction(event -> close());

            GridPane gridPane = new GridPane();
            gridPane.setVgap(10);
            gridPane.setHgap(10);
            gridPane.add(thresholdLabel, 0, 0);
            gridPane.add(thresholdField, 1, 0);
            gridPane.add(distanceLabel, 0, 1);
            gridPane.add(distanceField, 1, 1);
            gridPane.add(okButton, 0, 2);
            gridPane.add(cancelButton, 1, 2);

            Scene scene = new Scene(gridPane, 250, 100);
            setScene(scene);
        }

        public void showAndWait() {
            super.showAndWait();
        }
    }
}
