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
    private final Map<XYChart.Series<Number, Number>, Boolean> mirroredStateMap = new HashMap<>();
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
        layout.getChildren().add(seriesList);
        Scene scene = new Scene(layout, 350, 300);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
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
            lastSeriesInfo.setStyle("-fx-background-color: lightgreen;");
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
        ColorPicker colorPicker = colorPickersMap.computeIfAbsent(series, this::createColorPicker);
        Button removeButton = createRemoveButton(series);
        RadioButton radioButton = createRadioButton(series, radioGroup);
        Button autoRangeButton = createAutoRangeButton(series);
        Button mirrorButton = createMirrorButton(series);

        GridPane seriesInfo = new GridPane();
        seriesInfo.setHgap(10);
        seriesInfo.getColumnConstraints().addAll(
                new ColumnConstraints(100, 100, Double.MAX_VALUE, Priority.ALWAYS, null, true),
                new ColumnConstraints(50));
        seriesInfo.addRow(0, nameLabel, colorPicker, autoRangeButton, mirrorButton, radioButton, removeButton);

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
        button.getStyleClass().add("custom-button");
        button.setOnAction(e -> toggleAutoRange(series));
        return button;
    }

    /**
     * Создание кнопки для зеркалирования серии.
     *
     * @param series серия данных
     * @return кнопка для зеркалирования
     */
    private Button createMirrorButton(XYChart.Series<Number, Number> series) {
        Button button = new Button("Зеркало");
        button.getStyleClass().add("custom-button");
        button.setOnAction(e -> mirrorSeries(series));
        return button;
    }

    /**
     * Зеркалирование серии данных внутри своего диапазона.
     *
     * @param series серия данных
     */
    private void mirrorSeries(XYChart.Series<Number, Number> series) {
        ObservableList<XYChart.Data<Number, Number>> mirroredData = FXCollections.observableArrayList();

        double minX = series.getData().stream().mapToDouble(data -> data.getXValue().doubleValue()).min().orElse(0);
        double maxX = series.getData().stream().mapToDouble(data -> data.getXValue().doubleValue()).max().orElse(0);
        double centerX = (minX + maxX) / 2;

        for (XYChart.Data<Number, Number> data : series.getData()) {
            double mirroredX = 2 * centerX - data.getXValue().doubleValue();
            mirroredData.add(new XYChart.Data<>(mirroredX, data.getYValue()));
        }

        series.setData(mirroredData);
        mirroredStateMap.put(series, !mirroredStateMap.getOrDefault(series, false));

        SpectralDataTable.updateTableViewInTab(currentTab, series.getData(), tableViewToUpdate);

        if (!lineChart.getXAxis().isAutoRanging()) {
            adjustManualBounds();
        } else {
            adjustBounds(series);
        }
    }

    /**
     * Корректировка границ оси X в ручном режиме.
     */
    private void adjustManualBounds() {
        double minX = lineChart.getData().stream()
                .flatMap(series -> series.getData().stream())
                .mapToDouble(data -> data.getXValue().doubleValue())
                .min().orElse(0);
        double maxX = lineChart.getData().stream()
                .flatMap(series -> series.getData().stream())
                .mapToDouble(data -> data.getXValue().doubleValue())
                .max().orElse(0);

        ((NumberAxis) lineChart.getXAxis()).setLowerBound(minX);
        ((NumberAxis) lineChart.getXAxis()).setUpperBound(maxX);
    }

    /**
     * Корректировка границ оси после зеркалирования.
     *
     * @param series серия данных
     */
    private void adjustBounds(XYChart.Series<Number, Number> series) {
        if (series.getData().size() > 1) {
            double firstX = series.getData().get(0).getXValue().doubleValue();
            double lastX = series.getData().get(series.getData().size() - 1).getXValue().doubleValue();
            ((NumberAxis) lineChart.getXAxis()).setLowerBound(firstX);
            ((NumberAxis) lineChart.getXAxis()).setUpperBound(lastX);
        }
    }

    /**
     * Переключение автонастройки диапазона оси.
     *
     * @param series серия данных
     */
    private void toggleAutoRange(XYChart.Series<Number, Number> series) {
        if (lineChart.getXAxis().isAutoRanging()) {
            switchToManualRange(series);
        } else {
            switchToAutoRange();
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
            if (mirroredStateMap.getOrDefault(series, false)) {
                double temp = firstX;
                firstX = lastX;
                lastX = temp;
            }
            lineChart.getXAxis().setAutoRanging(false);
            ((NumberAxis) lineChart.getXAxis()).setLowerBound(firstX);
            ((NumberAxis) lineChart.getXAxis()).setUpperBound(lastX);
            setSeriesStyle(series, true);
            System.out.println("Режим: Установлены ручные границы (Set Bounds)");
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
        System.out.println("Режим: Автоматическое определение границ (Auto Resize)");
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
        button.getStyleClass().add("remove-button");
        button.setOnAction(e -> {
            lineChart.getData().remove(series);
            colorPickersMap.remove(series);
            mirroredStateMap.remove(series);
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

            // Сброс стилей всех серий
            resetAllSeriesStyles();

            // Установка стиля для выбранной серии на жирный
            if (!"Локальные пики".equals(selectedSeries.getName())) {
                setSeriesStyle(selectedSeries, true);
            }

            // Обновление таблицы
            SpectralDataTable.updateTableViewInTab(currentTab, selectedSeries.getData(), tableViewToUpdate);
        }
    }




    /**
     * Сброс стилей всех серий на тонкий.
     */
    private void resetAllSeriesStyles() {
        lineChart.getData().forEach(series -> {
            if (!"Локальные пики".equals(series.getName())) {
                setSeriesStyle(series, false);
                updateSeriesColor(series, colorPickersMap.get(series).getValue());
            }
        });
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
        colorPickersMap.get(series).setValue(newColor);
        if (!"Локальные пики".equals(series.getName())) {
            Node seriesNode = series.getNode();
            if (seriesNode != null) {
                seriesNode.setStyle("-fx-stroke: " + toRgbString(newColor) + ";");
            }
        }
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
        if ("Локальные пики".equals(series.getName())) {
            return; // Не изменять стиль для серии "Локальные пики"
        }
        Node seriesLine = series.getNode();
        if (seriesLine != null) {
            Color color = colorPickersMap.get(series).getValue();
            String style = "-fx-stroke: " + toRgbString(color) + "; -fx-stroke-width: " + (bold ? "5" : "3") + ";";
            seriesLine.setStyle(style);
        }
    }



    /**
     * Утолщение линий при закрытии окна в зависимости от типа серии.
     */
    private void thickenLinesOnClose() {
        lineChart.getData().forEach(series -> {
            if (!"Локальные пики".equals(series.getName())) {
                Node seriesLine = series.getNode().lookup(".chart-series-line");
                if (seriesLine instanceof Shape) {
                    Shape line = (Shape) seriesLine;
                    Color color = colorPickersMap.get(series).getValue();
                    line.setStyle("-fx-stroke: " + toRgbString(color) + "; -fx-stroke-width: 3;");
                }
            }
        });
    }

}
