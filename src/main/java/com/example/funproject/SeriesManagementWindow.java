package com.example.funproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс окна управления сериями графиков.
 */
public class SeriesManagementWindow extends Stage {
    private final LineChart<Number, Number> lineChart;
    private final ListView<GridPane> seriesList = new ListView<>();
    private final Map<XYChart.Series<Number, Number>, ColorPicker> colorPickersMap = new HashMap<>();
    private final Tab currentTab;
    private final TableView<SpectralDataTable.SpectralData> tableViewToUpdate;

    /**
     * Конструктор класса.
     *
     * @param lineChart         график для управления
     * @param tableViewToUpdate таблица для обновления данных
     * @param currentTab        текущая вкладка
     */
    public SeriesManagementWindow(LineChart<Number, Number> lineChart,
                                  TableView<SpectralDataTable.SpectralData> tableViewToUpdate,
                                  Tab currentTab) {
        this.lineChart = lineChart;
        this.tableViewToUpdate = tableViewToUpdate;
        this.currentTab = currentTab;
        initializeUI();
        this.setOnCloseRequest(event -> thickenLinesOnClose());
    }

    /**
     * Инициализация пользовательского интерфейса.
     */
    private void initializeUI() {
        VBox layout = new VBox(10);
        updateSeriesList();
        seriesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                handleSeriesSelection(newValue));
        layout.getChildren().addAll(seriesList); // Добавление списка серий в layout
        Scene scene = new Scene(layout, 290, 300);
        this.setScene(scene);
        this.setAlwaysOnTop(true);
    }

    /**
     * Обновление списка серий в пользовательском интерфейсе.
     */
    private void updateSeriesList() {
        seriesList.getItems().clear();
        ToggleGroup radioGroup = new ToggleGroup();

        lineChart.getData().forEach(series -> {
            GridPane seriesInfo = createSeriesInfo(series, radioGroup);
            seriesList.getItems().add(seriesInfo);
        });

        if (!seriesList.getItems().isEmpty()) {
            GridPane lastSeriesInfo = seriesList.getItems().get(seriesList.getItems().size() - 1);
            lastSeriesInfo.setStyle("-fx-background-color: lightgreen;"); // Последний элемент зелёный
        }
    }

    /**
     * Создание интерфейса для каждой серии в списке.
     *
     * @param series     серия данных
     * @param radioGroup группа радио кнопок
     * @return GridPane с информацией о серии
     */
    private GridPane createSeriesInfo(XYChart.Series<Number, Number> series, ToggleGroup radioGroup) {
        Label nameLabel = new Label(series.getName());
        ColorPicker colorPicker = colorPickersMap.computeIfAbsent(series, s -> createColorPicker(s));
        Button removeButton = createRemoveButton(series);
        RadioButton radioButton = createRadioButton(series, radioGroup);
        Button autoRangeButton = createAutoRangeButton(series); // Создаем кнопку

        GridPane seriesInfo = new GridPane();
        seriesInfo.setHgap(10);
        seriesInfo.getColumnConstraints().addAll(
                new ColumnConstraints(100, 100, Double.MAX_VALUE, Priority.ALWAYS, null, true),
                new ColumnConstraints(50));
        seriesInfo.addRow(0, nameLabel, colorPicker, autoRangeButton, radioButton, removeButton);

        return seriesInfo;
    }

    /**
     * Создание кнопки для автонастройки диапазона оси.
     *
     * @param series серия данных
     * @return кнопка для автонастройки
     */
    private Button createAutoRangeButton(XYChart.Series<Number, Number> series) {
        Button button = new Button("Авто");
        button.setOnAction(e -> toggleAutoRange(series));
        return button;
    }

    /**
     * Переключение автонастройки диапазона оси.
     *
     * @param series серия данных
     */
    private void toggleAutoRange(XYChart.Series<Number, Number> series) {
        if (series.getNode() != null) {
            if (lineChart.getXAxis().isAutoRanging()) {
                switchToManualRange(series);
            } else {
                switchToAutoRange();
            }
        }
    }

    /**
     * Переключение в ручной режим диапазона оси.
     *
     * @param series серия данных
     */
    private void switchToManualRange(XYChart.Series<Number, Number> series) {
        if (series.getData().size() > 1) {
            double firstX = series.getData().get(0).getXValue().doubleValue();
            double lastX = series.getData().get(series.getData().size() - 1).getXValue().doubleValue();
            lineChart.getXAxis().setAutoRanging(false);
            ((NumberAxis) lineChart.getXAxis()).setLowerBound(firstX);
            ((NumberAxis) lineChart.getXAxis()).setUpperBound(lastX);
            setSeriesStyle(series, true);
        } else {
            System.out.println("Недостаточно данных для установки ручного диапазона");
        }
    }

    /**
     * Переключение в автоматический режим диапазона оси.
     */
    private void switchToAutoRange() {
        lineChart.getXAxis().setAutoRanging(true);
        lineChart.getYAxis().setAutoRanging(true);
        resetAllSeriesStyles();
    }

    /**
     * Создание выборщика цвета для серии.
     *
     * @param series серия данных
     * @return ColorPicker для выбора цвета
     */
    private ColorPicker createColorPicker(XYChart.Series<Number, Number> series) {
        ColorPicker picker = new ColorPicker(getSeriesColor(series));
        picker.setOnAction(e -> updateSeriesColor(series, picker.getValue()));
        return picker;
    }

    /**
     * Создание кнопки для удаления серии.
     *
     * @param series серия данных
     * @return кнопка для удаления
     */
    private Button createRemoveButton(XYChart.Series<Number, Number> series) {
        Button button = new Button("X");
        button.setOnAction(e -> {
            lineChart.getData().remove(series);
            colorPickersMap.remove(series);
            updateSeriesList();
        });
        return button;
    }

    /**
     * Создание радиокнопки для перемещения серии в конец списка.
     *
     * @param series серия данных
     * @param group  группа радио кнопок
     * @return радиокнопка
     */
    private RadioButton createRadioButton(XYChart.Series<Number, Number> series, ToggleGroup group) {
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(group);
        radioButton.setOnAction(e -> {
            if (radioButton.isSelected()) moveToEnd(series);
        });
        return radioButton;
    }

    /**
     * Обработка выбора серии в списке.
     *
     * @param newValue новое значение выбранной строки
     */
    private void handleSeriesSelection(GridPane newValue) {
        if (newValue != null) {
            int selectedIndex = seriesList.getSelectionModel().getSelectedIndex();
            XYChart.Series<Number, Number> selectedSeries = lineChart.getData().get(selectedIndex);

            // Сбросить стиль всех серий на тонкий
            resetAllSeriesStyles();

            // Установить стиль для выбранной серии на жирный
            setSeriesStyle(selectedSeries, true);

            // Обновить таблицу
            SpectralDataTable.updateTableViewInTab(currentTab, selectedSeries.getData(), tableViewToUpdate);
        }
    }

    /**
     * Сброс стилей всех серий на тонкий.
     */
    private void resetAllSeriesStyles() {
        lineChart.getData().forEach(series -> setSeriesStyle(series, false));
    }

    /**
     * Перемещение выбранной серии в конец списка данных.
     *
     * @param series серия для перемещения
     */
    private void moveToEnd(XYChart.Series<Number, Number> series) {
        Map<XYChart.Series<Number, Number>, Color> seriesColors = cacheSeriesColors();

        ObservableList<XYChart.Series<Number, Number>> newData = FXCollections.observableArrayList(lineChart.getData());
        newData.remove(series);
        newData.add(series);

        lineChart.setData(newData);
        applyCachedColors(seriesColors);
        updateSeriesList();
    }

    /**
     * Кэширование цветов серий перед изменением порядка.
     *
     * @return карта серий и их цветов
     */
    private Map<XYChart.Series<Number, Number>, Color> cacheSeriesColors() {
        Map<XYChart.Series<Number, Number>, Color> colors = new HashMap<>();
        lineChart.getData().forEach(s -> colors.put(s, getSeriesColor(s)));
        return colors;
    }

    /**
     * Применение кэшированных цветов к сериям.
     *
     * @param colors карта цветов серий
     */
    private void applyCachedColors(Map<XYChart.Series<Number, Number>, Color> colors) {
        colors.forEach(this::updateSeriesColor);
    }

    /**
     * Получение текущего цвета серии.
     *
     * @param series серия данных
     * @return цвет серии
     */
    private Color getSeriesColor(XYChart.Series<Number, Number> series) {
        Node seriesLine = series.getNode().lookup(".chart-series-line");
        return seriesLine instanceof Shape ? (Color) ((Shape) seriesLine).getStroke() : Color.BLACK;
    }

    /**
     * Обновление цвета серии.
     *
     * @param series   серия данных
     * @param newColor новый цвет
     */
    private void updateSeriesColor(XYChart.Series<Number, Number> series, Color newColor) {
        Node seriesNode = series.getNode();
        if (seriesNode != null) seriesNode.setStyle("-fx-stroke: " + toRgbString(newColor) + ";");
    }

    /**
     * Преобразование цвета в строковый формат RGB.
     *
     * @param color объект Color
     * @return строка RGB
     */
    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * Установка стиля серии (жирный или обычный).
     *
     * @param series серия данных
     * @param bold   true для жирного стиля, false для обычного
     */
    private void setSeriesStyle(XYChart.Series<Number, Number> series, boolean bold) {
        Node seriesLine = series.getNode();
        if (seriesLine != null) {
            seriesLine.setStyle(bold ? "-fx-stroke-width: 3;" : "-fx-stroke-width: 1;");
        }
    }

    /**
     * Утолщение линий при закрытии окна в зависимости от типа серии.
     */
    private void thickenLinesOnClose() {
        lineChart.getData().forEach(series -> {
            Node seriesLine = series.getNode().lookup(".chart-series-line");
            if (seriesLine instanceof Shape) {
                Shape line = (Shape) seriesLine;
                String currentStyle = line.getStyle();
                boolean isDashed = currentStyle != null && currentStyle.contains("-fx-stroke-dash-array");

                if ("Baseline".equals(series.getName())) {
                    if (isDashed) {
                        line.setStyle("-fx-stroke-width: 1; " + currentStyle);
                    } else {
                        line.setStyle("-fx-stroke-width: 1; -fx-stroke: " + toRgbString((Color) line.getStroke()) + ";");
                    }
                } else {
                    if (isDashed) {
                        line.setStyle("-fx-stroke-width: 3; " + currentStyle);
                    } else {
                        line.setStrokeWidth(3);
                    }
                }
            }
        });
    }
}
