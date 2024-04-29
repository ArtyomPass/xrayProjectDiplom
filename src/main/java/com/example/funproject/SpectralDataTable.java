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

    public TableView<SpectralData> getTableView() {
        return tableView;
    }

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

    public static void updateTableViewInTab(Tab tab,
                                            List<XYChart.Data<Number, Number>> seriesData,
                                            TableView<SpectralDataTable.SpectralData> spectralDataTableViews) {
        if (tab == null) return; // Проверка на null
        // Получаем TableView из spectralDataTableViews
        TableView<SpectralData> tableView = spectralDataTableViews;
        if (tableView == null) {
            System.err.println("TableView не найдена для вкладки " + tab.getText());
            return;
        }
        // Преобразуем данные seriesData в SpectralData
        List<SpectralData> tableData = seriesData.stream()
                .map(data -> new SpectralData(data.getXValue(), data.getYValue()))
                .collect(Collectors.toList());
        // Обновляем данные таблицы
        tableView.setItems(FXCollections.observableArrayList(tableData));
    }



    public static class SpectralData {
        private Number xValue;
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
