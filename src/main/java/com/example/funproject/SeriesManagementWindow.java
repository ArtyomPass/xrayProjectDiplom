package com.example.funproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SeriesManagementWindow extends Stage {

    // Ссылка на LineChart, которым управляет окно
    private final LineChart<Number, Number> lineChart;

    // Список серий, отображаемый в ListView
    private final ListView<GridPane> seriesList = new ListView<>();

    // Соответствие между сериями и их ColorPicker'ами
    private final Map<XYChart.Series<Number, Number>, ColorPicker> colorPickersMap = new HashMap<>();

    // Ссылка на текущую вкладку
    private final Tab currentTab;

    // Сохранение предыдущей выделенной серии (для сброса стиля)
    private XYChart.Series<Number, Number> previousSelectedSeries = null;

    // Ссылка на TableView для обновления
    private final TableView<SpectralDataTable.SpectralData> tableViewToUpdate;


    /**
     * Конструктор окна управления сериями
     *
     * @param lineChart       LineChart, которым управляет окно
     * @param tableViewToUpdate TableView для обновления данными из выбранной серии
     * @param currentTab     Текущая вкладка, содержащая LineChart и TableView
     */
    public SeriesManagementWindow(LineChart<Number, Number> lineChart,
                                  TableView<SpectralDataTable.SpectralData> tableViewToUpdate,
                                  Tab currentTab) {
        this.lineChart = lineChart;
        this.tableViewToUpdate = tableViewToUpdate; // Сохраняем ссылку на TableView
        this.currentTab = currentTab;
        initializeUI(); // Инициализация пользовательского интерфейса

        // Добавление обработчика события закрытия окна
        // При закрытии окна, тонкие линии делаются жирнее для лучшей видимости
        this.setOnCloseRequest(event -> {
            for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                Node seriesLine = series.getNode().lookup(".chart-series-line");
                if (seriesLine instanceof Shape) {
                    // Получаем текущий стиль линии
                    String currentStyle = seriesLine.getStyle();
                    // Проверяем, есть ли уже стиль пунктирной линии
                    boolean isDashed = currentStyle.contains("-fx-stroke-dash-array");
                    // Определяем толщину линии
                    double currentStrokeWidth = ((Shape) seriesLine).getStrokeWidth();
                    if (isDashed) {
                        // Пунктирная линия: устанавливаем только толщину, сохраняя стиль
                        seriesLine.setStyle(String.format("-fx-stroke-width: %.1f; %s",
                                (currentStrokeWidth < 2) ? 3.0 : currentStrokeWidth, currentStyle));
                    } else if (currentStrokeWidth < 2) {
                        // Тонкая сплошная линия: делаем её жирной сплошной
                        ((Shape) seriesLine).setStrokeWidth(3);
                    } // else - жирная линия остаётся без изменений
                }
            }
        });
    }

    /**
     * Инициализация пользовательского интерфейса окна управления сериями
     */
    private void initializeUI() {
        VBox layout = new VBox(10); // Вертикальный контейнер для элементов
        updateSeriesList(); // Заполнение списка серий

        // Обработчик выбора серии в ListView:
        // - устанавливает жирный стиль для выбранной серии
        // - обновляет TableView с данными из выбранной серии
        seriesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Получение серии из выбранного элемента списка
                XYChart.Series<Number, Number> selectedSeries = lineChart.getData().get(seriesList.getSelectionModel().getSelectedIndex());

                // Сброс стиля предыдущей выделенной серии
                if (previousSelectedSeries != null) {
                    setSeriesStyle(previousSelectedSeries, false);
                }

                // Установка жирного стиля для выбранной серии
                setSeriesStyle(selectedSeries, true);

                // Сохранение текущей выделенной серии
                previousSelectedSeries = selectedSeries;

                // Обновление TableView с данными из выбранной серии
                SpectralDataTable.updateTableViewInTab(currentTab, selectedSeries.getData(), tableViewToUpdate);
            }
        });

        layout.getChildren().addAll(seriesList); // Добавление списка в контейнер
        Scene scene = new Scene(layout, 240, 300); // Создание сцены
        // this.setTitle("Удаление серий"); // Заголовок окна (опционально)
        this.setScene(scene); // Установка сцены для окна
    }

    /**
     * Обновление списка серий в ListView
     * Создает элементы для каждой серии с именем, выбором цвета и кнопкой удаления
     */
    private void updateSeriesList() {
        seriesList.getItems().clear(); // Очистка списка перед обновлением
        ToggleGroup radioGroup = new ToggleGroup(); // Создаем группу для радиокнопок

        // Перебор всех серий в LineChart
        for (XYChart.Series<Number, Number> series : lineChart.getData()) {
            Label nameLabel = new Label(series.getName()); // Метка с именем серии

            // Получение или создание ColorPicker для серии
            ColorPicker colorPicker = colorPickersMap.computeIfAbsent(series, s -> {
                ColorPicker picker = new ColorPicker(getSeriesColor(s));
                // Обработчик изменения цвета серии
                picker.setOnAction(e -> updateSeriesColor(s, picker.getValue()));
                return picker;
            });

            Button removeButton = new Button("X"); // Кнопка для удаления серии
            removeButton.setOnAction(e -> {
                lineChart.getData().remove(series); // Удаление серии из LineChart
                colorPickersMap.remove(series); // Удаление из словаря
                updateSeriesList(); // Обновление списка серий
            });

            // Создаем радиокнопку для серии
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(radioGroup); // Добавляем кнопку в группу
            radioButton.setOnAction(e -> {
                if (radioButton.isSelected()) {
                    moveToEnd(series);
                }
            });

            // Организация элементов в GridPane
            GridPane seriesInfo = new GridPane();
            seriesInfo.setHgap(10);
            seriesInfo.getColumnConstraints().addAll(
                    new ColumnConstraints(100, 100, Double.MAX_VALUE, Priority.ALWAYS, null, true),
                    new ColumnConstraints(50));
            seriesInfo.addRow(0, nameLabel, colorPicker, removeButton, radioButton);
            seriesList.getItems().add(seriesInfo); // Добавление информации о серии в список

        }
    }

    /**
     * Перемещение серии в конец списка данных графика
     *
     * @param series Серия для перемещения
     */
    /**
     * Перемещение серии в конец списка данных графика, сохраняя текущие цвета.
     *
     * @param series Серия для перемещения
     */
    private void moveToEnd(XYChart.Series<Number, Number> series) {
        // Сохранение текущих цветов серий
        Map<XYChart.Series<Number, Number>, Color> seriesColors = new HashMap<>();
        for (XYChart.Series<Number, Number> s : lineChart.getData()) {
            seriesColors.put(s, getSeriesColor(s));
        }

        // Создание нового списка данных
        ObservableList<XYChart.Series<Number, Number>> newData = FXCollections.observableArrayList(lineChart.getData());
        newData.remove(series);
        newData.add(series); // Перемещение выбранной серии в конец

        // Обновление данных графика
        lineChart.setData(newData);

        // Применение сохранённых цветов к обновлённым сериям
        for (Map.Entry<XYChart.Series<Number, Number>, Color> entry : seriesColors.entrySet()) {
            updateSeriesColor(entry.getKey(), entry.getValue());
        }

        // Обновление TableView
        SpectralDataTable.updateTableViewInTab(currentTab, series.getData(), tableViewToUpdate);

        // Обновление списка серий
        updateSeriesList();
    }


    /**
     * Получение текущего цвета серии
     *
     * @param series Серия данных
     * @return Цвет серии или черный, если цвет не определен
     */
    private Color getSeriesColor(XYChart.Series<Number, Number> series) {
        // Извлекаем узел, представляющий линию серии
        Node seriesLine = series.getNode().lookup(".chart-series-line");

        // Проверяем, является ли узел фигурой (Shape)
        if (seriesLine instanceof Shape) {
            // Если да, то возвращаем цвет обводки фигуры
            return (Color) ((Shape) seriesLine).getStroke();
        } else {
            // Если нет, возвращаем черный цвет по умолчанию
            return Color.BLACK;
        }
    }

    /**
     * Обновление цвета серии и соответствующих элементов
     *
     * @param series   Серия данных
     * @param newColor Новый цвет
     */
    private void updateSeriesColor(XYChart.Series<Number, Number> series, Color newColor) {
        Node seriesNode = series.getNode();
        if (seriesNode != null) {
            seriesNode.setStyle("-fx-stroke: " + toRgbString(newColor) + ";");
        }
    }

    // Вспомогательный метод для преобразования цвета в строку RGB
    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Установка стиля серии (жирный или обычный)
     *
     * @param series Серия данных
     * @param bold   true для жирного стиля, false для обычного
     */
    private void setSeriesStyle(XYChart.Series<Number, Number> series, boolean bold) {
        Node seriesLine = series.getNode().lookup(".chart-series-line");
        if (seriesLine instanceof Shape) {
            // Получаем текущий стиль линии
            String currentStyle = seriesLine.getStyle();

            // Проверяем, есть ли уже стиль пунктирной линии
            boolean isDashed = currentStyle.contains("-fx-stroke-dash-array");

            if (bold) {
                // Если нужно сделать линию жирной, увеличиваем толщину
                ((Shape) seriesLine).setStrokeWidth(3);
            } else {
                // Если нужно сделать линию обычной, уменьшаем толщину
                ((Shape) seriesLine).setStrokeWidth(1);

                // Если линия была пунктирной, сохраняем этот стиль
                if (isDashed) {
                    seriesLine.setStyle("-fx-stroke-width: 1; " + currentStyle);
                }
            }
        }
    }
}