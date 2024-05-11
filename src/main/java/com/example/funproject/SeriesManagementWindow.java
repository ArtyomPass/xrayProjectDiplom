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

import java.util.HashMap;
import java.util.Map;

public class SeriesManagementWindow extends Stage {
    private final LineChart<Number, Number> lineChart;
    private final ListView<GridPane> seriesList = new ListView<>();
    private final Map<XYChart.Series<Number, Number>, ColorPicker> colorPickersMap = new HashMap<>();
    private final Tab currentTab;
    private final TableView<SpectralDataTable.SpectralData> tableViewToUpdate;
    private XYChart.Series<Number, Number> previousSelectedSeries = null;

    public SeriesManagementWindow(LineChart<Number, Number> lineChart,
                                  TableView<SpectralDataTable.SpectralData> tableViewToUpdate,
                                  Tab currentTab) {
        this.lineChart = lineChart;
        this.tableViewToUpdate = tableViewToUpdate;
        this.currentTab = currentTab;
        initializeUI();

        this.setOnCloseRequest(event -> thickenLinesOnClose());
    }

    private void initializeUI() {
        VBox layout = new VBox(10);
        updateSeriesList();

        seriesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                handleSeriesSelection(newValue));

        layout.getChildren().addAll(seriesList); // Добавление списка серий в layout
        Scene scene = new Scene(layout, 240, 300);
        this.setScene(scene);
    }

    private void updateSeriesList() {
        seriesList.getItems().clear();
        ToggleGroup radioGroup = new ToggleGroup();

        ObservableList<GridPane> seriesInfoList = FXCollections.observableArrayList();

        lineChart.getData().forEach(series -> {
            GridPane seriesInfo = createSeriesInfo(series, radioGroup);
            seriesInfoList.add(seriesInfo);
        });

        // Обновление списка элементов в ListView
        seriesList.getItems().addAll(seriesInfoList);

        // Установка светло-зелёного фона для последнего элемента, если он существует
        if (!seriesInfoList.isEmpty()) {
            GridPane lastSeriesInfo = seriesInfoList.get(seriesInfoList.size() - 1);
            lastSeriesInfo.setStyle("-fx-background-color: lightgreen;");
        }
    }

    private GridPane createSeriesInfo(XYChart.Series<Number, Number> series, ToggleGroup radioGroup) {
        Label nameLabel = new Label(series.getName());
        ColorPicker colorPicker = colorPickersMap.computeIfAbsent(series, s -> createColorPicker(s));
        Button removeButton = createRemoveButton(series);
        RadioButton radioButton = createRadioButton(series, radioGroup);

        GridPane seriesInfo = new GridPane();
        seriesInfo.setHgap(10);
        seriesInfo.getColumnConstraints().addAll(
                new ColumnConstraints(100, 100, Double.MAX_VALUE, Priority.ALWAYS, null, true),
                new ColumnConstraints(50));
        seriesInfo.addRow(0, nameLabel, colorPicker, removeButton, radioButton);

        return seriesInfo;
    }

    private ColorPicker createColorPicker(XYChart.Series<Number, Number> series) {
        ColorPicker picker = new ColorPicker(getSeriesColor(series));
        picker.setOnAction(e -> updateSeriesColor(series, picker.getValue()));
        return picker;
    }

    private Button createRemoveButton(XYChart.Series<Number, Number> series) {
        Button button = new Button("X");
        button.setOnAction(e -> {
            lineChart.getData().remove(series);
            colorPickersMap.remove(series);
            updateSeriesList();
        });
        return button;
    }

    private RadioButton createRadioButton(XYChart.Series<Number, Number> series, ToggleGroup group) {
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(group);
        radioButton.setOnAction(e -> {
            if (radioButton.isSelected()) moveToEnd(series);
        });
        return radioButton;
    }

    private void handleSeriesSelection(GridPane newValue) {
        if (newValue != null) {
            int selectedIndex = seriesList.getSelectionModel().getSelectedIndex();
            XYChart.Series<Number, Number> selectedSeries = lineChart.getData().get(selectedIndex);

            if (previousSelectedSeries != null) setSeriesStyle(previousSelectedSeries, false);
            setSeriesStyle(selectedSeries, true);

            previousSelectedSeries = selectedSeries;
            SpectralDataTable.updateTableViewInTab(currentTab, selectedSeries.getData(), tableViewToUpdate);
        }
    }

    private void moveToEnd(XYChart.Series<Number, Number> series) {
        Map<XYChart.Series<Number, Number>, Color> seriesColors = cacheSeriesColors();

        ObservableList<XYChart.Series<Number, Number>> newData = FXCollections.observableArrayList(lineChart.getData());
        newData.remove(series);
        newData.add(series);

        lineChart.setData(newData);
        applyCachedColors(seriesColors);
        updateSeriesList();
    }

    private Map<XYChart.Series<Number, Number>, Color> cacheSeriesColors() {
        Map<XYChart.Series<Number, Number>, Color> colors = new HashMap<>();
        lineChart.getData().forEach(s -> colors.put(s, getSeriesColor(s)));
        return colors;
    }

    private void applyCachedColors(Map<XYChart.Series<Number, Number>, Color> colors) {
        colors.forEach(this::updateSeriesColor);
    }

    private Color getSeriesColor(XYChart.Series<Number, Number> series) {
        Node seriesLine = series.getNode().lookup(".chart-series-line");
        return seriesLine instanceof Shape ? (Color) ((Shape) seriesLine).getStroke() : Color.BLACK;
    }

    private void updateSeriesColor(XYChart.Series<Number, Number> series, Color newColor) {
        Node seriesNode = series.getNode();
        if (seriesNode != null) seriesNode.setStyle("-fx-stroke: " + toRgbString(newColor) + ";");
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    private void setSeriesStyle(XYChart.Series<Number, Number> series, boolean bold) {
        Node seriesLine = series.getNode().lookup(".chart-series-line");
        if (seriesLine instanceof Shape) {
            ((Shape) seriesLine).setStrokeWidth(bold ? 3 : 1);
        }
    }


    private void thickenLinesOnClose() {
        lineChart.getData().forEach(series -> {
            Node seriesLine = series.getNode().lookup(".chart-series-line");
            if (seriesLine instanceof Shape) {
                Shape line = (Shape) seriesLine;
                String currentStyle = line.getStyle();
                boolean isDashed = currentStyle != null && currentStyle.contains("-fx-stroke-dash-array");

                // Устанавливаем стиль в зависимости от названия серии
                if ("Baseline".equals(series.getName())) {
                    // Для серии "Baseline" сохраняем пунктирность и делаем линию тонкой
                    if (isDashed) {
                        line.setStyle("-fx-stroke-width: 1; " + currentStyle);
                    } else {
                        line.setStyle("-fx-stroke-width: 1; -fx-stroke: " + toRgbString((Color)line.getStroke()) + ";");
                    }
                } else {
                    // Для всех других серий делаем линии жирными
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
