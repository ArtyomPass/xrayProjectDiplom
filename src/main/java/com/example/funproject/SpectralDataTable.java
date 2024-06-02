package com.example.funproject;

import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

    private TableColumn<SpectralData, Number> createColumn(String title, String propertyName) {
        TableColumn<SpectralData, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }

    /**
     * Импортирует данные из файла и обновляет TableView.
     */
    public void importTableData(File selectedFile) {
        if (selectedFile == null) {
            // TODO: Обработка случая, когда файл не выбран
            return;
        }

        List<SpectralData> tableData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        double xValue = Double.parseDouble(parts[0]);
                        double yValue = Double.parseDouble(parts[1]);
                        tableData.add(new SpectralData(xValue, yValue));
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка при разборе данных: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(tableData));
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
