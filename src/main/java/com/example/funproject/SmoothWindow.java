package com.example.funproject;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class SmoothWindow extends Stage {

    // Элементы управления
    private TextField iterationsField;
    private Button smoothButton;

    public SmoothWindow(HelloController controller, LineChart<Number, Number> chart) {
        // Инициализация элементов управления
        iterationsField = new TextField("1");  // Значение по умолчанию: 1 итерация
        smoothButton = new Button("Сгладить");

        // Создание GridPane для размещения элементов
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        // Добавление элементов на GridPane
        gridPane.add(new Label("Число итераций:"), 0, 0);
        gridPane.add(iterationsField, 1, 0);
        gridPane.add(smoothButton, 1, 1);

        // Обработка нажатия на кнопку сглаживания
        smoothButton.setOnAction(event -> {
            smoothLinear(chart, Integer.parseInt(iterationsField.getText()));
        });

        // Настройка окна
        this.setTitle("Сглаживание данных");
        this.setScene(new Scene(gridPane));
        this.setAlwaysOnTop(true);
    }

    private void smoothLinear(LineChart<Number, Number> chart, int iterations) {
        // Получение данных из графика
        XYChart.Series<Number, Number> series = chart.getData().get(chart.getData().size() - 1);
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        // Выполнение сглаживания заданное количество итераций
        for (int i = 0; i < iterations; i++) {
            for (int j = 1; j < data.size() - 1; j++) {
                // Расчет среднего значения между соседними точками
                double average = (data.get(j - 1).getYValue().doubleValue() + data.get(j + 1).getYValue().doubleValue()) / 2;
                // Обновление значения текущей точки
                data.get(j).setYValue(average);
            }
        }
        // График автоматически обновится с новыми данными
    }
}
