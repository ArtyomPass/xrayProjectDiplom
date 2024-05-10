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

                // 1. Определение диапазона X и направления
                double minX, maxX, step;
                if (lastSeries.getData().get(0).getXValue().doubleValue() <
                        lastSeries.getData().get(lastSeries.getData().size() - 1).getXValue().doubleValue()) {

                    minX = lastSeries.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .min()
                            .orElse(0);
                    maxX = lastSeries.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .max()
                            .orElse(0);
                    step = energyStep;
                } else {
                    minX = lastSeries.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .max()
                            .orElse(0);
                    maxX = lastSeries.getData().stream()
                            .mapToDouble(data -> data.getXValue().doubleValue())
                            .min()
                            .orElse(0);
                    step = -energyStep;
                }

                // 2. Генерация новых X-значений в диапазоне minX - maxX с учетом направления
                List<Double> newXValues = new ArrayList<>();
                if (minX < maxX) {
                    for (double x = minX; x <= maxX; x += step) {
                        newXValues.add(x);
                    }
                } else {
                    for (double x = minX; x >= maxX; x -= step) { // Изменено условие и шаг
                        newXValues.add(x);
                    }
                }

                // Создать новую серию для интерполированных данных
                XYChart.Series<Number, Number> interpolatedSeries = new XYChart.Series<>();
                for (int i = 0; i < newXValues.size(); i++) {
                    double x = newXValues.get(i);
                    // Найти три ближайшие точки из последней серии
                    int index1 = findClosestIndex(lastSeries.getData(), x);
                    int index2 = index1 + 1;
                    int index3 = index2 + 1;

                    // Проверка границ
                    if (index3 >= lastSeries.getData().size()) {
                        break; // Недостаточно точек для интерполяции
                    }

                    // Получить значения X и Y для трёх точек
                    double x1 = lastSeries.getData().get(index1).getXValue().doubleValue();
                    double y1 = lastSeries.getData().get(index1).getYValue().doubleValue();
                    double x2 = lastSeries.getData().get(index2).getXValue().doubleValue();
                    double y2 = lastSeries.getData().get(index2).getYValue().doubleValue();
                    double x3 = lastSeries.getData().get(index3).getXValue().doubleValue();
                    double y3 = lastSeries.getData().get(index3).getYValue().doubleValue();

                    // Интерполяция по трём точкам (например, кубический сплайн)
                    double y = interpolate(x, x1, y1, x2, y2, x3, y3);

                    // Добавить интерполированную точку в новую серию
                    interpolatedSeries.getData().add(new XYChart.Data<>(x, y));
                }

                chart.getData().clear();
                chart.getData().add(interpolatedSeries);
                System.out.println(interpolatedSeries.getData());
                interpolatedSeries.setName("Intensities");
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

    private static int findClosestIndex(ObservableList<XYChart.Data<Number, Number>> data, double targetX) {
        int closestIndex = -1;
        double closestDistance = Double.MAX_VALUE;
        for (int i = 0; i < data.size(); i++) {
            double currentX = data.get(i).getXValue().doubleValue();
            double distance = Math.abs(currentX - targetX);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
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