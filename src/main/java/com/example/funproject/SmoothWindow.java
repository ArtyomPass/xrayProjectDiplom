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
import javafx.scene.control.ComboBox;


public class SmoothWindow extends Stage {

    // Элементы управления
    private ComboBox<String> smoothingTypeComboBox;
    private TextField iterationsField;
    private Label splineAccuracyLabel;
    private TextField splineAccuracyField;
    private Button smoothButton;

    public SmoothWindow(HelloController controller, LineChart<Number, Number> chart) {
        // Инициализация элементов управления
        smoothingTypeComboBox = new ComboBox<>();
        smoothingTypeComboBox.getItems().addAll("Линейное", "Квадратичное", "Кубическое","Сплайн");
        iterationsField = new TextField("1");  // Значение по умолчанию: 1 итерация
        splineAccuracyLabel = new Label("Точность сплайна:");
        splineAccuracyField = new TextField();
        smoothButton = new Button("Сгладить");

        // Создание GridPane для размещения элементов
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        // Добавление элементов на GridPane
        gridPane.add(new Label("Тип сглаживания:"), 0, 0);
        gridPane.add(smoothingTypeComboBox, 1, 0);
        gridPane.add(new Label("Число итераций:"), 0, 1);
        gridPane.add(iterationsField, 1, 1);
        gridPane.add(splineAccuracyLabel, 0, 2);
        gridPane.add(splineAccuracyField, 1, 2);
        gridPane.add(smoothButton, 1, 3);

        // Скрыть элементы для точности сплайна
        hideSplineAccuracy();

        // Обработка выбора типа сглаживания
        smoothingTypeComboBox.setOnAction(event -> {
            String selectedType = smoothingTypeComboBox.getValue();
            switch (selectedType) {
                case "Линейное":
                    hideSplineAccuracy();
                    // Перенос вызова метода smoothLinear внутрь обработчика кнопки
                    smoothButton.setOnAction(smoothEvent -> {
                        smoothLinear(chart, Integer.parseInt(iterationsField.getText()));
                    });
                    break;
                case "Квадратичное":
                    hideSplineAccuracy();
                    smoothButton.setOnAction(smoothEvent -> {
                        smoothQuadratic(chart, Integer.parseInt(iterationsField.getText()));
                    });
                    break;
                case "Кубическое":
                    hideSplineAccuracy();
                    smoothButton.setOnAction(smoothEvent -> {
                        smoothCubic(chart, Integer.parseInt(iterationsField.getText()));
                    });
                    break;
                case "Сплайн":
                    showSplineAccuracy();

                    break;
                default:
                    System.out.println("Выберите тип сглаживания!");
            }


            if (selectedType.equals("Сплайн")) {
                showSplineAccuracy();
            } else {
                hideSplineAccuracy();
            }
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

    private void smoothQuadratic(LineChart<Number, Number> chart, int iterations) {
        // Получение данных из графика
        XYChart.Series<Number, Number> series = chart.getData().get(chart.getData().size() - 1);
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        // Выполнение сглаживания заданное количество итераций
        for (int i = 0; i < iterations; i++) {
            for (int j = 2; j < data.size() - 2; j++) {
                // Расчет квадратичного сглаживания
                double newValue = (-3 * data.get(j - 2).getYValue().doubleValue() +
                                12 * data.get(j - 1).getYValue().doubleValue() +
                                17 * data.get(j).getYValue().doubleValue() +
                                12 * data.get(j + 1).getYValue().doubleValue() -
                                3 * data.get(j + 2).getYValue().doubleValue()) / 35;

                // Обновление значения текущей точки
                data.get(j).setYValue(newValue);
            }
        }
    }

    private void smoothCubic(LineChart<Number, Number> chart, int iterations) {
        // Последняя серия данных
        XYChart.Series<Number, Number> series = chart.getData().get(chart.getData().size() - 1);
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        for (int i = 0; i < iterations; i++) {
            for (int j = 2; j < data.size() - 2; j++) { // Обратите внимание на изменение границ цикла
                double average = (1.0 / 21) * (
                        data.get(j - 2).getYValue().doubleValue() -
                                8 * data.get(j - 1).getYValue().doubleValue() +
                                13 * data.get(j).getYValue().doubleValue() +
                                13 * data.get(j + 1).getYValue().doubleValue() -
                                8 * data.get(j + 2).getYValue().doubleValue() +
                                data.get(j + 3).getYValue().doubleValue()
                );
                data.get(j).setYValue(average);
            }
        }

    }

    private void showSplineAccuracy() {
        splineAccuracyLabel.setVisible(true);
        splineAccuracyField.setVisible(true);
    }

    private void hideSplineAccuracy() {
        splineAccuracyLabel.setVisible(false);
        splineAccuracyField.setVisible(false);
    }


}

