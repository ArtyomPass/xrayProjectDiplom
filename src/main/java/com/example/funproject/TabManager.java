package com.example.funproject;

import javafx.geometry.Orientation;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class TabManager {

    private final TabPane tabPane;

    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public void createNewTab(String title) {
        Tab newTab = new Tab(title, createTabContent());
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    private SplitPane createTabContent() {
        // Create and initialize SpectralDataTable
        SpectralDataTable spectralDataTable = new SpectralDataTable();
        TableView tableView = spectralDataTable.getTableView(); // Get the TableView

        // Set column resize policy to constrained resize
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Channel");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Intensity");

        // Create LineChart with spline interpolation
        LineChart<Number, Number> spectrumChart = new LineChart<>(xAxis, yAxis);
        spectrumChart.setCreateSymbols(false); // Disable data markers (dots)
        spectrumChart.setAlternativeRowFillVisible(false); // Optional: remove alternating row fills

        SplitPane dataChartSplitPane = new SplitPane();
        dataChartSplitPane.setOrientation(Orientation.HORIZONTAL);
        dataChartSplitPane.getItems().addAll(tableView, spectrumChart);
        dataChartSplitPane.setDividerPositions(0.3);

        ImageView xrayImageView = new ImageView();
        xrayImageView.setPreserveRatio(true);
        xrayImageView.setSmooth(true);

        ScrollPane imageViewScrollPane = new ScrollPane(xrayImageView);
        imageViewScrollPane.setFitToWidth(true);
        imageViewScrollPane.setFitToHeight(true);

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(dataChartSplitPane, imageViewScrollPane);
        mainSplitPane.setDividerPositions(0.5);

        return mainSplitPane;
    }
}