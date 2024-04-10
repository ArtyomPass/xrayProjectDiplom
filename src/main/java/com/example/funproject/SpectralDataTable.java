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

    public static void updateTableViewInTab(Tab tab, List<XYChart.Data<Number, Number>> seriesData) {
        if (tab == null || !(tab.getContent() instanceof SplitPane)) return;
        SplitPane mainSplitPane = (SplitPane) tab.getContent();
        SplitPane dataTableChartSplitPane = findSplitPaneWithTableView(mainSplitPane);
        if (dataTableChartSplitPane == null) return;

        for (Node item : dataTableChartSplitPane.getItems()) {
            if (item instanceof TableView) {
                System.out.println("\nTable view is " + item);
                TableView<SpectralData> tableView = (TableView<SpectralData>) item;
                List<SpectralData> tableData = seriesData.stream()
                        .map(data -> new SpectralData(data.getXValue(), data.getYValue()))
                        .collect(Collectors.toList());

                tableView.setItems(FXCollections.observableArrayList(tableData));
                break;
            }
        }
    }


    private static SplitPane findSplitPaneWithTableView(SplitPane mainSplitPane) {
        for (Node item : mainSplitPane.getItems()) {
            if (item instanceof SplitPane) {
                SplitPane splitPane = (SplitPane) item;
                for (Node innerItem : splitPane.getItems()) {
                    if (innerItem instanceof TableView) {
                        return splitPane;
                    }
                }
            }
        }
        return null;
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
