package com.example.funproject;

import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class SpectralDataTable {

    private final TableView<SpectralData> tableView = new TableView<>();

    public SpectralDataTable() {
        setupColumns();
    }

    private void setupColumns() {
        tableView.getColumns().addAll(
                createColumn("X", "xValue"),
                createColumn("Y", "yValue")
        );
    }

    // Убрали параметр типа T
    private TableColumn<SpectralData, Number> createColumn(String title, String propertyName) {
        TableColumn<SpectralData, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }

    public static void updateTableViewInTab(Tab tab,
                                            List<XYChart.Data<Number, Number>> seriesData,
                                            TableView<SpectralData> tableView) {
        tableView.setItems(FXCollections.observableArrayList(
                seriesData.stream()
                        .map(data -> new SpectralData(data.getXValue(), data.getYValue()))
                        .toList()
        ));
    }

    public TableView<SpectralData> getTableView() {
        return tableView;
    }

    public static class SpectralData {
        private final Number xValue;
        private Number yValue;

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

        public void setYValue(Number yValue) {
            this.yValue = yValue;
        }
    }
}