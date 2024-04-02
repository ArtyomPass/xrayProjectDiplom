package com.example.funproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpectralDataTable {

    private TableView<SpectralData> tableView;

    public SpectralDataTable() {
        tableView = new TableView<>();
        setupColumns();
    }

    private void setupColumns() {
        TableColumn<SpectralData, Number> xColumn = new TableColumn<>("X");
        xColumn.setCellValueFactory(new PropertyValueFactory<>("xValue"));

        TableColumn<SpectralData, Number> yColumn = new TableColumn<>("Y");
        yColumn.setCellValueFactory(new PropertyValueFactory<>("yValue"));

        tableView.getColumns().addAll(xColumn, yColumn);
    }

    public static void updateTableViewInTab(Tab tab, List<XYChart.Data<Number, Number>> seriesData) {
        if (!(tab != null && tab.getContent() instanceof SplitPane)) return;

        SplitPane mainSplitPane = (SplitPane) tab.getContent();
        if (mainSplitPane.getItems().isEmpty() || !(mainSplitPane.getItems().get(0) instanceof SplitPane)) return;

        SplitPane innerSplitPane = (SplitPane) mainSplitPane.getItems().get(0);
        if (innerSplitPane.getItems().isEmpty() || !(innerSplitPane.getItems().get(0) instanceof TableView)) return;

        TableView<SpectralData> tableView = (TableView<SpectralData>) innerSplitPane.getItems().get(0);
        List<SpectralData> tableData = seriesData.stream()
                .map(dataPoint -> new SpectralData(dataPoint.getXValue(), dataPoint.getYValue()))
                .collect(Collectors.toList());

        ObservableList<SpectralData> observableList = FXCollections.observableArrayList(tableData);
        tableView.setItems(observableList);
        System.out.println("TableView successfully updated.");
    }

    public TableView<SpectralData> getTableView() {
        return tableView;
    }

    public static class SpectralData {
        private final Number xValue;
        private final Number yValue;

        public SpectralData(Number xValue, Number yValue) {
            this.xValue = xValue;
            this.yValue = yValue;
        }

        public Number getXValue() {
            return xValue;
        }

        public Number getYValue() {
            return yValue;
        }
    }
}
