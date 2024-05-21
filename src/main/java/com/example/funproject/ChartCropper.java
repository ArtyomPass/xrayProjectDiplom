package com.example.funproject;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Iterator;

public class ChartCropper {
    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> verticalLineSeries;
    private XYChart.Series<Number, Number> peakSeries;
    private Runnable onCropComplete;

    public ChartCropper(LineChart<Number, Number> lineChart) {
        this.lineChart = lineChart;
        this.verticalLineSeries = new XYChart.Series<>();
        verticalLineSeries.setName("Vertical Line");

        this.peakSeries = new XYChart.Series<>();
        peakSeries.setName("Local Peaks");

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

    public void setOnCropComplete(Runnable onCropComplete) {
        this.onCropComplete = onCropComplete;
    }

    public void enterCroppingMode() {
        enableChartCropping();
    }

    public void exitCroppingMode() {
        lineChart.setOnMouseMoved(null);
        lineChart.setOnMouseClicked(null);
        if (lineChart.getData().contains(verticalLineSeries)) {
            lineChart.getData().remove(verticalLineSeries);
        }
        if (onCropComplete != null) {
            onCropComplete.run();
        }
    }

    private void enableChartCropping() {
        lineChart.setOnMouseEntered(event -> verticalLineSeries.getNode().setVisible(true));
        lineChart.setOnMouseExited(event -> verticalLineSeries.getNode().setVisible(false));
        lineChart.setOnMouseMoved(this::handleMouseMoved);
        lineChart.setOnMouseClicked(this::handleMouseClicked);

        // Изначально скрыть вертикальную линию
        verticalLineSeries.getNode().setVisible(false);

        // Установить стиль для вертикальной линии
        verticalLineSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: red; -fx-stroke-width: 1px;");
    }

    private void handleMouseMoved(MouseEvent event) {
        // Преобразование координат мыши из сцены в локальные координаты графика
        double mouseX = event.getSceneX();
        double localX = lineChart.getXAxis().sceneToLocal(mouseX, 0).getX();

        // Преобразование localX в соответствующее значение по оси X
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        Number xValue = xAxis.getValueForDisplay(localX);

        // Очистка предыдущих данных линии
        verticalLineSeries.getData().clear();

        // Добавление новых точек для вертикальной линии
        verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getLowerBound()));
        verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getUpperBound()));
    }

    private void handleMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            double mouseX = event.getSceneX();
            double localX = lineChart.getXAxis().sceneToLocal(mouseX, 0).getX();

            // Преобразование localX в соответствующее значение по оси X
            NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
            Number xValue = xAxis.getValueForDisplay(localX);

            // Обрезка данных серий
            cropData(xValue.doubleValue());

            // Выделение локальных пиков точками
            highlightLocalPeaks();

            // Установка нового нижнего предела для оси X
            xAxis.setLowerBound(xValue.doubleValue());
            xAxis.setAutoRanging(false);

            // Фиксация диапазона оси Y для предотвращения изменений
            NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
            yAxis.setLowerBound(yAxis.getLowerBound());
            yAxis.setUpperBound(yAxis.getUpperBound());
            yAxis.setAutoRanging(false);

            // Удаление вертикальной линии как серии данных и выход из режима обрезки
            lineChart.getData().remove(verticalLineSeries);
            exitCroppingMode();  // Выход из режима обрезки
        }
    }

    private void cropData(double xValue) {
        for (XYChart.Series<Number, Number> series : lineChart.getData()) {
            if (!series.equals(verticalLineSeries) && !series.equals(peakSeries)) {
                Iterator<XYChart.Data<Number, Number>> iterator = series.getData().iterator();
                while (iterator.hasNext()) {
                    XYChart.Data<Number, Number> data = iterator.next();
                    if (data.getXValue().doubleValue() < xValue) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void highlightLocalPeaks() {
        double threshold = 10.0; // Пример порогового уровня
        int distance = 10; // Пример расстояния для проверки

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
                        // Это истинный локальный пик, превышающий порог
                        System.out.println("Local peak found at X: " + currentData.getXValue() + ", Y: " + currentData.getYValue());
                        XYChart.Data<Number, Number> peakData = new XYChart.Data<>(currentData.getXValue(), currentData.getYValue());
                        Circle circle = new Circle(5, Color.BLUE);
                        peakData.setNode(circle);
                        peakSeries.getData().add(peakData);
                    }
                }
            }
        }

        // Отключить линии для серии пиков
        peakSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: transparent;");
    }

}
