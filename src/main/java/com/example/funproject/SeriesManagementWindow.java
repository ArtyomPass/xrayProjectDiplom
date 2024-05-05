package com.example.funproject;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class SeriesManagementWindow extends Stage {

    private LineChart<Number, Number> lineChart; // Ссылка на график LineChart

    public SeriesManagementWindow(LineChart<Number, Number> lineChart) {
        this.lineChart = lineChart;
        initializeUI(); // Инициализируем пользовательский интерфейс
    }

    private void initializeUI() {
        VBox layout = new VBox(10); // Основной контейнер (вертикальный)
        ListView<HBox> seriesList = new ListView<>(); // Список для отображения серий

        // Срабатывает после отрисовки окна, чтобы получить доступ к узлам
        this.setOnShown(event -> {
            for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                // 1. Перебираем все серии в графике
                Label nameLabel = new Label(series.getName()); // Метка с именем серии
                ColorPicker colorPicker = new ColorPicker(); // Выбор цвета для серии

                // Получаем узел линии серии для начального цвета
                Shape seriesLine = (Shape) series.getNode().lookup(".chart-series-line");
                if (seriesLine != null) {
                    colorPicker.setValue((Color) seriesLine.getStroke());
                }

                // 2. Обработчик изменения цвета
                colorPicker.setOnAction(e -> {
                    Color newColor = colorPicker.getValue(); // Новый выбранный цвет
                    updateSeriesColor(series, newColor); // Обновляем цвет серии
                });

                HBox seriesInfo = new HBox(10, nameLabel, colorPicker); // Контейнер для имени и выбора цвета
                seriesList.getItems().add(seriesInfo); // Добавляем информацию о серии в список
            }
        });

        // Кнопки управления (добавление, редактирование, удаление)
        Button addButton = new Button("Add");
        Button editButton = new Button("Edit");
        Button removeButton = new Button("Remove");
        HBox buttonBar = new HBox(10, addButton, editButton, removeButton);

        layout.getChildren().addAll(seriesList, buttonBar); // Добавляем элементы в основной контейнер
        Scene scene = new Scene(layout, 400, 300); // Создаем сцену
        this.setTitle("Manage Data Series"); // Заголовок окна
        this.setScene(scene); // Устанавливаем сцену для окна
    }

    // 3. Метод для обновления цвета серии
    private void updateSeriesColor(XYChart.Series<Number, Number> series, Color newColor) {
        // Обновляем цвет линии серии
        Shape seriesLine = (Shape) series.getNode().lookup(".chart-series-line");
        if (seriesLine != null) {
            seriesLine.setStroke(newColor);
        }

        // Обновляем цвет маркеров данных
        ObservableList<XYChart.Data<Number, Number>> dataPoints = series.getData();
        for (XYChart.Data<Number, Number> data : dataPoints) {
            Node dataNode = data.getNode();
            if (dataNode != null) {
                // 4. Изменяем стиль маркера данных, используя CSS
                dataNode.lookup(".chart-line-symbol").setStyle("-fx-background-color: " + newColor.toString().replace("0x", "#") + ";");
            }
        }

        // 5. Обновляем цвет маркера в легенде (предполагается использование стандартной легенды)
        Node legendItem = lineChart.lookup(".chart-legend-item-symbol");
        if (legendItem != null) {
            legendItem.setStyle("-fx-background-color: " + newColor.toString().replace("0x", "#") + ";");
        }
    }
}