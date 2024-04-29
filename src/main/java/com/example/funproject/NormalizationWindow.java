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

public class NormalizationWindow extends Stage {

    private TextField valueField;
    private Button applyButton;

    // Конструктор для передачи данных и контроллера
    public NormalizationWindow(HelloController controller,
                               LineChart<Number, Number> chart,
                               TableView<SpectralDataTable.SpectralData> tableView) {
        // Настройка окна (размер, заголовок и т.д.)
        this.setTitle("Нормировка");
        this.initModality(Modality.APPLICATION_MODAL);

        // Создание элементов управления
        valueField = new TextField();
        applyButton = new Button("Применить");

        // Создаем TextFormatter с фильтром для чисел
        TextFormatter<Double> formatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*(\\.\\d*)?")) {
                return change;
            } else {
                return null;
            }
        });

        // Устанавливаем TextFormatter для поля
        valueField.setTextFormatter(formatter);

        // Размещение элементов в layout (VBox)
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(new Label("Значение нормировки:"), valueField, applyButton);
        this.setScene(new Scene(layout));
        this.setAlwaysOnTop(true);

        applyButton.setOnAction(event -> {
            try {

                if(chart.getData().isEmpty()){
                    // Вывод сообщения об ошибке
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("График пустой");
                    alert.setContentText("График должен иметь какие-нибудь значения)");
                    alert.showAndWait();
                    return; // Прерываем обработку
                }

                double normalizationValue = Double.parseDouble(valueField.getText());
                // Найти максимальное значение Y в данных графика
                double maxY = chart.getData().stream()
                        .flatMap(series -> series.getData().stream())
                        .mapToDouble(data -> data.getYValue().doubleValue())
                        .max().orElse(1.0); // 1.0 - значение по умолчанию, если данных нет
                // Коэффициент нормировки
                double normalizationFactor = normalizationValue / maxY;

                // Получить список всех серий
                ObservableList<XYChart.Series<Number, Number>> allSeries = chart.getData();
                XYChart.Series<Number, Number> lastSeries = allSeries.get(allSeries.size() - 1);

                // Создать новую серию для нормированных данных
                XYChart.Series<Number, Number> normalizedSeries = new XYChart.Series<>();

                // Скопировать данные из исходной серии и применить коэффициент нормировки
                for (XYChart.Data<Number, Number> data : lastSeries.getData()) {
                    Number xValue = data.getXValue();
                    Number yValue = data.getYValue().doubleValue() * normalizationFactor;
                    normalizedSeries.getData().add(new XYChart.Data<>(xValue, yValue));
                }
                normalizedSeries.setName("Нормированный график");
                // Добавить новую серию на график (и опционально удалить старую)
                chart.getData().clear();
                chart.getData().add(normalizedSeries);

                // Обновление данных в таблице
                for (SpectralDataTable.SpectralData data : tableView.getItems()) {
                    double oldValue = data.getYValue().doubleValue();
                    double newValue = oldValue * normalizationFactor;
                    data.setYValue(newValue);
                }
                tableView.refresh();

                this.close();
            } catch (NumberFormatException e) {
                // Обработка ошибки ввода (например, показать сообщение об ошибке)
                // ...
            }
        });
    }
}