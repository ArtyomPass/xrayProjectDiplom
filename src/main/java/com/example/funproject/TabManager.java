package com.example.funproject;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class TabManager {

    private final TabPane tabPane;
    private TabPane innerTabPane;
    // Объявление TableView как глобальной переменной
    private TableView<SpectralDataTable.SpectralData> spectralDataTableView;

    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;
        // Инициализация TableView в конструкторе
        SpectralDataTable spectralDataTable = new SpectralDataTable();
        spectralDataTableView = spectralDataTable.getTableView();
        spectralDataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void createNewTab(String title, HelloController controller) {
        Tab newTab = new Tab(title, createTabContent(controller));
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        controller.setInnerTabPane(innerTabPane);
        controller.spectralDataTableViews.put(newTab, spectralDataTableView);
    }

    private SplitPane createTabContent(HelloController controller) {
        // Создание нового TabPane для графиков
        innerTabPane = new TabPane();
        handleAddButtonClick();

        // Создание кнопок
        Button addChartButton = new Button("Добавить");
        addChartButton.setOnAction(event -> handleAddButtonClick());

        // Создание кнопки под таблицей
        Button newButton = new Button("Новая кнопка");
        newButton.setOnAction(event -> {
            // Действия при нажатии на кнопку
            System.out.println("Кнопка нажата!");
        });

        VBox buttonsVBox = new VBox(addChartButton, newButton);
        buttonsVBox.setSpacing(5);

        // VBox для таблицы и кнопки
        VBox tableVBox = new VBox();
        tableVBox.getChildren().addAll(spectralDataTableView, newButton);

        // SplitPane для TableView и области с графиками (innerTabPane)
        SplitPane tableAndChartsSplitPane = new SplitPane();
        tableAndChartsSplitPane.setOrientation(Orientation.HORIZONTAL);
        tableAndChartsSplitPane.getItems().addAll(tableVBox, innerTabPane);
        tableAndChartsSplitPane.setDividerPositions(0.2);

        // SplitPane для объединения tableAndChartsSplitPane и кнопок
        SplitPane tabsAndButtonsSplitPane = new SplitPane();
        tabsAndButtonsSplitPane.setOrientation(Orientation.HORIZONTAL);
        tabsAndButtonsSplitPane.getItems().addAll(tableAndChartsSplitPane, buttonsVBox);
        tabsAndButtonsSplitPane.setDividerPositions(0.9);

        // Инициализация ImageView для основного изображения
        ImageView mainImageView = new ImageView();
        mainImageView.setPreserveRatio(true);
        ScrollPane mainImageScrollPane = new ScrollPane(mainImageView);
        mainImageScrollPane.setFitToWidth(true);
        mainImageScrollPane.setFitToHeight(true);
        mainImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Инициализация TilePane для миниатюр изображений
        TilePane thumbnailsTilePane = new TilePane();
        thumbnailsTilePane.setPrefColumns(1);
        thumbnailsTilePane.setPadding(new Insets(5));
        thumbnailsTilePane.setVgap(5);
        ScrollPane thumbnailsScrollPane = new ScrollPane(thumbnailsTilePane);
        thumbnailsScrollPane.setFitToWidth(true);
        thumbnailsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        thumbnailsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // SplitPane для основного изображения и миниатюр
        SplitPane imageAndThumbnailsSplitPane = new SplitPane();
        imageAndThumbnailsSplitPane.setOrientation(Orientation.HORIZONTAL);
        imageAndThumbnailsSplitPane.getItems().addAll(mainImageScrollPane, thumbnailsScrollPane);
        imageAndThumbnailsSplitPane.setDividerPositions(0.8);

        // Общий вертикальный SplitPane для всего содержимого вкладки
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(tabsAndButtonsSplitPane, imageAndThumbnailsSplitPane);
        mainSplitPane.setDividerPositions(0.6);

        return mainSplitPane;
    }

    private void handleAddButtonClick() {
        // Создаём оси для графика
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Создаём сам график
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Новый график");

        // Создаём новую вкладку с графиком в качестве содержимого
        Tab newTab = new Tab("График", lineChart);

        // Добавляем вкладку в InnerTabPane
        innerTabPane.getTabs().add(newTab);

        // Выбираем новую вкладку
        innerTabPane.getSelectionModel().select(newTab);
    }
}