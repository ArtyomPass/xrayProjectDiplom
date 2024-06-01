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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
    private final HelloController controller;
    private final Tab currentChartTab;
    /**
     * Конструктор класса.
     *
     * @param lineChart         график для управления
     * @param tableViewToUpdate таблица для обновления данных
     * @param currentTab        текущая вкладка
     */
    public SeriesManagementWindow(LineChart<Number, Number> lineChart,
                                  TableView<SpectralDataTable.SpectralData> tableViewToUpdate,
                                  Tab currentTab,
                                  Tab currentChartTab,
                                  HelloController controller) {
        this.lineChart = lineChart;
        this.tableViewToUpdate = tableViewToUpdate;
        this.currentTab = currentTab;
        this.controller = controller;
        this.currentChartTab = currentChartTab;
        initialize();
    }

    private void initialize() {
        initializeUI();
        setOnCloseRequest(event -> thickenLinesOnClose());
    }

    /**
     * Инициализация пользовательского интерфейса.
     */
    private void initializeUI() {
        VBox layout = new VBox(10);
        updateSeriesList();
        seriesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedIndex = seriesList.getSelectionModel().getSelectedIndex();
                XYChart.Series<Number, Number> selectedSeries = lineChart.getData().get(selectedIndex);

                // Сброс стилей всех серий
                lineChart.getData().forEach(series -> {
                    if (!"Локальные пики".equals(series.getName())) {
                        Node seriesLine = series.getNode();
                        if (seriesLine != null) {
                            Color color = colorPickersMap.get(series).getValue();
                            String style = "-fx-stroke: " + toRgbString(color) + "; -fx-stroke-width: 3;";
                            seriesLine.setStyle(style);
                        }
                    }
                });

                // Установка стиля для выбранной серии на жирный
                if (!"Локальные пики".equals(selectedSeries.getName())) {
                    Node seriesLine = selectedSeries.getNode();
                    if (seriesLine != null) {
                        Color color = colorPickersMap.get(selectedSeries).getValue();
                        String style = "-fx-stroke: " + toRgbString(color) + "; -fx-stroke-width: 5;";
                        seriesLine.setStyle(style);
                    }
                }

                // Обновление таблицы
                SpectralDataTable.updateTableViewInTab(currentTab, selectedSeries.getData(), tableViewToUpdate);
            }
        });
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
            Label nameLabel = new Label(series.getName());
            ColorPicker colorPicker = colorPickersMap.computeIfAbsent(series, this::createColorPicker);
            Button removeButton = new Button("X");
            removeButton.getStyleClass().add("remove-button");
            removeButton.setOnAction(e -> {
                lineChart.getData().remove(series);
                colorPickersMap.remove(series);
                mirroredStateMap.remove(series);
                if ("Вертикальная линия".equals(series.getName())) {
                    removeVerticalLineFromController(series);
                }
                updateSeriesList();
            });

            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(radioGroup);
            radioButton.setOnAction(e -> {
                if (radioButton.isSelected()) {
                    Map<XYChart.Series<Number, Number>, Color> seriesColors = new HashMap<>();
                    lineChart.getData().forEach(s -> seriesColors.put(s, getSeriesColor(s)));

                    ObservableList<XYChart.Series<Number, Number>> newData = FXCollections.observableArrayList(lineChart.getData());
                    newData.remove(series);
                    newData.add(series);

                    lineChart.setData(newData);
                    seriesColors.forEach((s, color) -> {
                        ColorPicker cp = colorPickersMap.get(s);
                        cp.setValue(color);
                        if (!"Локальные пики".equals(s.getName())) {
                            Node seriesNode = s.getNode();
                            if (seriesNode != null) {
                                seriesNode.setStyle("-fx-stroke: " + toRgbString(color) + ";");
                            }
                        }
                    });
                    updateSeriesList();
                }
            });

            Button autoRangeButton = new Button("Авто");
            autoRangeButton.getStyleClass().add("custom-button");
            autoRangeButton.setOnAction(e -> {
                if (lineChart.getXAxis().isAutoRanging()) {
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
                        Node seriesLine = series.getNode();
                        if (seriesLine != null) {
                            Color color = colorPickersMap.get(series).getValue();
                            String style = "-fx-stroke: " + toRgbString(color) + "; -fx-stroke-width: 5;";
                            seriesLine.setStyle(style);
                        }
                        System.out.println("Режим: Установлены ручные границы (Set Bounds)");
                    } else {
                        System.out.println("Недостаточно данных для установки ручного диапазона");
                    }
                } else {
                    lineChart.getXAxis().setAutoRanging(true);
                    lineChart.getYAxis().setAutoRanging(true);
                    lineChart.getData().forEach(s -> {
                        if (!"Локальные пики".equals(s.getName())) {
                            Node seriesLine = s.getNode();
                            if (seriesLine != null) {
                                Color color = colorPickersMap.get(s).getValue();
                                String style = "-fx-stroke: " + toRgbString(color) + "; -fx-stroke-width: 3;";
                                seriesLine.setStyle(style);
                            }
                        }
                    });
                    System.out.println("Режим: Автоматическое определение границ (Auto Resize)");
                }
            });

            Button mirrorButton = new Button("Зеркало");
            mirrorButton.getStyleClass().add("custom-button");
            mirrorButton.setOnAction(e -> {
                ObservableList<XYChart.Data<Number, Number>> mirroredData = FXCollections.observableArrayList();

                double minX = series.getData().stream().mapToDouble(data -> data.getXValue().doubleValue()).min().orElse(0);
                double maxX = series.getData().stream().mapToDouble(data -> data.getXValue().doubleValue()).max().orElse(0);
                double centerX = (minX + maxX) / 2;

                for (XYChart.Data<Number, Number> data : series.getData()) {
                    double mirroredX = 2 * centerX - data.getXValue().doubleValue();
                    mirroredData.add(new XYChart.Data<>(mirroredX, data.getYValue()));
                }

                // Сортировка данных по возрастанию X
                mirroredData.sort(Comparator.comparingDouble(data -> data.getXValue().doubleValue()));

                // Перезаписываем данные в самой серии
                series.getData().setAll(mirroredData);
                mirroredStateMap.put(series, !mirroredStateMap.getOrDefault(series, false));

                SpectralDataTable.updateTableViewInTab(currentTab, series.getData(), tableViewToUpdate);

                if (!lineChart.getXAxis().isAutoRanging()) {
                    double minXManual = lineChart.getData().stream()
                            .flatMap(s -> s.getData().stream())
                            .mapToDouble(d -> d.getXValue().doubleValue())
                            .min().orElse(0);
                    double maxXManual = lineChart.getData().stream()
                            .flatMap(s -> s.getData().stream())
                            .mapToDouble(d -> d.getXValue().doubleValue())
                            .max().orElse(0);

                    ((NumberAxis) lineChart.getXAxis()).setLowerBound(minXManual);
                    ((NumberAxis) lineChart.getXAxis()).setUpperBound(maxXManual);
                } else {
                    if (series.getData().size() > 1) {
                        double firstX = series.getData().get(0).getXValue().doubleValue();
                        double lastX = series.getData().get(series.getData().size() - 1).getXValue().doubleValue();
                        ((NumberAxis) lineChart.getXAxis()).setLowerBound(firstX);
                        ((NumberAxis) lineChart.getXAxis()).setUpperBound(lastX);
                    }
                }
            });





            GridPane seriesInfo = new GridPane();
            seriesInfo.setHgap(10);
            seriesInfo.getColumnConstraints().addAll(
                    new ColumnConstraints(100, 100, Double.MAX_VALUE, Priority.ALWAYS, null, true),
                    new ColumnConstraints(50));
            seriesInfo.addRow(0, nameLabel, colorPicker, autoRangeButton, mirrorButton, radioButton, removeButton);

            seriesList.getItems().add(seriesInfo);
        });

        if (!seriesList.getItems().isEmpty()) {
            GridPane lastSeriesInfo = seriesList.getItems().get(seriesList.getItems().size() - 1);
            lastSeriesInfo.setStyle("-fx-background-color: lightgreen;");
        }
    }

    private void removeVerticalLineFromController(XYChart.Series<Number, Number> series) {
        List<LineInfo> linesInTab = controller.chartLines.get(currentChartTab);
        if (linesInTab != null) {
            linesInTab.removeIf(lineInfo -> lineInfo.getSeries() == series);
        }
    }


    /**
     * Создание выборщика цвета для серии.
     *
     * @param series серия данных
     * @return ColorPicker для выбора цвета
     */
    private ColorPicker createColorPicker(XYChart.Series<Number, Number> series) {
        ColorPicker picker = new ColorPicker(getSeriesColor(series));
        picker.setOnAction(e -> {
            Color newColor = picker.getValue();
            colorPickersMap.get(series).setValue(newColor);
            if (!"Локальные пики".equals(series.getName())) {
                Node seriesNode = series.getNode();
                if (seriesNode != null) {
                    seriesNode.setStyle("-fx-stroke: " + toRgbString(newColor) + ";");
                }
            }
        });
        return picker;
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
     * Преобразование цвета в строковый формат RGB.
     *
     * @param color объект Color
     * @return строка RGB
     */
    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
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
