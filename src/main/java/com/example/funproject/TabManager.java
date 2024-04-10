package com.example.funproject;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

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
        // Инициализация TableView для отображения данных
        SpectralDataTable spectralDataTable = new SpectralDataTable();
        TableView<SpectralDataTable.SpectralData> tableView = spectralDataTable.getTableView();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Channel");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Intensity");
        LineChart<Number, Number> spectrumChart = new LineChart<>(xAxis, yAxis);
        spectrumChart.setCreateSymbols(false);
        spectrumChart.setLegendVisible(false);

        // Горизонтальный SplitPane для TableView и LineChart
        SplitPane dataTableChartSplitPane = new SplitPane();
        dataTableChartSplitPane.setOrientation(Orientation.HORIZONTAL);
        dataTableChartSplitPane.getItems().addAll(tableView, spectrumChart);
        dataTableChartSplitPane.setDividerPositions(0.5);

        // Инициализация ImageView для основного изображения
        ImageView mainImageView = new ImageView();
        mainImageView.setPreserveRatio(true);

        // Настройка ScrollPane для основного ImageView
        ScrollPane mainImageScrollPane = new ScrollPane(mainImageView);
        mainImageScrollPane.setFitToWidth(true);
        mainImageScrollPane.setFitToHeight(true);
        mainImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Инициализация TilePane для миниатюр изображений
        TilePane thumbnailsTilePane = new TilePane();
        thumbnailsTilePane.setPrefColumns(1); // Установка количества колонок для миниатюр
        thumbnailsTilePane.setPadding(new Insets(5)); // Небольшой отступ
        thumbnailsTilePane.setVgap(5); // Вертикальный отступ между миниатюрами

        // Настройка ScrollPane для миниатюр
        ScrollPane thumbnailsScrollPane = new ScrollPane(thumbnailsTilePane);
        thumbnailsScrollPane.setFitToWidth(true);
        thumbnailsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        thumbnailsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Горизонтальный SplitPane для основного изображения и миниатюр
        SplitPane imageAndThumbnailsSplitPane = new SplitPane();
        imageAndThumbnailsSplitPane.setOrientation(Orientation.HORIZONTAL);
        imageAndThumbnailsSplitPane.getItems().addAll(mainImageScrollPane, thumbnailsScrollPane);
        imageAndThumbnailsSplitPane.setDividerPositions(0.75);

        // Общий вертикальный SplitPane для всего содержимого вкладки
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(dataTableChartSplitPane, imageAndThumbnailsSplitPane);
        mainSplitPane.setDividerPositions(0.5);

        return mainSplitPane;
    }


}