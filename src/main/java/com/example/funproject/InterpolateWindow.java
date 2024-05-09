package com.example.funproject;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class InterpolateWindow extends Stage {

    public InterpolateWindow(HelloController controller, LineChart<Number, Number> chart) {
        // Создаем новое окно
        Stage interpolateStage = new Stage();
        interpolateStage.setTitle("Интерполяция");
        interpolateStage.initModality(Modality.APPLICATION_MODAL);

        // Создаем элементы управления
        Label energyStepLabel = new Label("Шаг энергии:");
        TextField energyStepField = new TextField();
        Button interpolateButton = new Button("Интерполировать");

        // Создаем TextFormatter с фильтром для чисел
        TextFormatter<Double> formatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d*)?")) {
                return change;
            } else {
                return null;
            }
        });

        // Устанавливаем TextFormatter для поля
        energyStepField.setTextFormatter(formatter);

        // Размещаем элементы в layout (VBox)
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(energyStepLabel, energyStepField, interpolateButton);

        // Создаем сцену и устанавливаем ее в окно
        this.setScene(new Scene(layout));
        this.setAlwaysOnTop(true);

        // Обработчик нажатия на кнопку "Интерполировать"
        interpolateButton.setOnAction(event -> {
            try {
                // Получаем значение шага энергии
                double energyStep = Double.parseDouble(energyStepField.getText());

                // Проверка на ноль
                if (energyStep == 0) {
                    // Вывод сообщения об ошибке
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Неверный шаг энергии");
                    alert.setContentText("Шаг энергии не может быть равен нулю.");
                    alert.showAndWait();
                    return;
                }

                if (chart.getData().isEmpty()) {
                    // Вывод сообщения об ошибке
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("График пустой");
                    alert.setContentText("График должен иметь какие-нибудь значения)");
                    alert.showAndWait();
                    return;
                }

                // Получить список всех серий
                ObservableList<XYChart.Series<Number, Number>> allSeries = chart.getData();
                XYChart.Series<Number, Number> lastSeries = allSeries.get(allSeries.size() - 1);

                // Создать новую серию для интерполированных данных
                XYChart.Series<Number, Number> interpolatedSeries = new XYChart.Series<>();

                // Получить первое и последнее значения X из последней серии
                double firstX = lastSeries.getData().get(0).getXValue().doubleValue();
                double lastX = lastSeries.getData().get(lastSeries.getData().size() - 1).getXValue().doubleValue();

                // Цикл интерполяции
                for (double x = firstX; x <= lastX; x += energyStep) {
                    // Найти три ближайшие точки из последней серии
                    double x1 = findClosestX(lastSeries.getData(), x);
                    double x2 = findClosestX(lastSeries.getData(), x + energyStep);
                    double x3 = findClosestX(lastSeries.getData(), x + 2 * energyStep);

                    // Проверка границ
                    if (x3 > lastX) {
                        break; // Недостаточно точек для интерполяции
                    }

                    // Получить значения Y для трёх точек
                    double y1 = findYValue(lastSeries.getData(), x1);
                    double y2 = findYValue(lastSeries.getData(), x2);
                    double y3 = findYValue(lastSeries.getData(), x3);

                    // Интерполяция по трём точкам (например, кубический сплайн)
                    double y = interpolate(x, x1, y1, x2, y2, x3, y3);

                    // Добавить интерполированную точку в новую серию
                    interpolatedSeries.getData().add(new XYChart.Data<>(x, y));
                }

                chart.getData().clear();
                chart.getData().add(interpolatedSeries);
            } catch (NumberFormatException e) {
                // Обработка ошибки ввода
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Неверный формат ввода");
                alert.setContentText("Пожалуйста, введите числовое значение для шага энергии.");
                alert.showAndWait();
            }
        });
    }

    private static double findClosestX(ObservableList<XYChart.Data<Number, Number>> data, double targetX) {
        double closestDistance = Double.MAX_VALUE;
        double closestX = 0;
        for (XYChart.Data<Number, Number> point : data) {
            double currentX = point.getXValue().doubleValue();
            double distance = Math.abs(currentX - targetX);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestX = currentX;
            }
        }
        return closestX;
    }

    // Найти значение Y для заданного значения X
    private static double findYValue(ObservableList<XYChart.Data<Number, Number>> data, double targetX) {
        for (XYChart.Data<Number, Number> point : data) {
            if (point.getXValue().doubleValue() == targetX) {
                return point.getYValue().doubleValue();
            }
        }
        return 0; // Значение не найдено, вернуть 0
    }

    private static double interpolate(double x, double x1, double y1, double x2, double y2, double x3, double y3) {
        // Проверка на совпадение с x1 или x3
        if (x == x1) return y1;
        if (x == x3) return y3;

        // Интерполяция по трём точкам с использованием формулы Лагранжа
        double L1 = ((x - x2) * (x - x3)) / ((x1 - x2) * (x1 - x3));
        double L2 = ((x - x1) * (x - x3)) / ((x2 - x1) * (x2 - x3));
        double L3 = ((x - x1) * (x - x2)) / ((x3 - x1) * (x3 - x2));
        return L1 * y1 + L2 * y2 + L3 * y3;
    }
}