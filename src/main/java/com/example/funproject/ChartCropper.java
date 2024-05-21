package com.example.funproject;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Iterator;

public class ChartCropper {
    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> verticalLineSeries;
    private Runnable onCropComplete;

    public ChartCropper(LineChart<Number, Number> lineChart) {
        this.lineChart = lineChart;
        this.verticalLineSeries = new XYChart.Series<>();
        verticalLineSeries.setName("Vertical Line");

        lineChart.setCreateSymbols(false); // Отключить символы
        lineChart.getData().add(verticalLineSeries);
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
            if (!series.equals(verticalLineSeries)) {
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
}
