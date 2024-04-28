package com.example.funproject;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class TabManager {

    // Глобальная переменная для хранения TabPane, с которым работает класс
    private final TabPane tabPane;

    // Глобальная переменная для хранения внутреннего TabPane для графиков
    private TabPane innerTabPane;

    protected Map<Tab, TabPane> innerTabPanes;

    // Глобальная переменная для хранения TableView с данными спектра
    private TableView<SpectralDataTable.SpectralData> spectralDataTableView;

    /**
     * Конструктор класса TabManager
     *
     * @param tabPane - TabPane, с которым будет работать класс
     */
    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;
        this.innerTabPanes = new HashMap<>();
    }

    /**
     * Метод для создания новой вкладки
     *
     * @param title      - название новой вкладки
     * @param controller - контроллер приложения
     */
    public void createNewTab(String title, HelloController controller) {
        // Инициализация TableView для новой вкладки
        SpectralDataTable spectralDataTable = new SpectralDataTable();
        spectralDataTableView = spectralDataTable.getTableView();
        spectralDataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Создание новой вкладки с контентом
        Tab newTab = new Tab(title, createTabContent(controller));

        // Добавление новой вкладки в TabPane
        tabPane.getTabs().add(newTab);

        // Выбор новой вкладки
        tabPane.getSelectionModel().select(newTab);

        // Сохранение TableView для новой вкладки в контроллере
        controller.spectralDataTableViews.put(newTab, spectralDataTableView);

        // Сохраняем innerTabPane в Map

        innerTabPanes.put(newTab, innerTabPane); // Добавляем innerTabPane в Map

        //System.out.println("HERE: " + innerTabPanes.get(newTab).getTabs());

        handleAddButtonClick(controller);
    }

    /**
     * Метод для создания содержимого новой вкладки
     *
     * @param controller - контроллер приложения
     * @return SplitPane - содержимое вкладки
     */
    private SplitPane createTabContent(HelloController controller) {
        // Создание внутреннего TabPane для графиков
        this.innerTabPane = new TabPane();


        // Создание кнопок управления
        Button addChartButton = new Button("Добавить график");
        addChartButton.setOnAction(event -> handleAddButtonClick(controller));

        // Размещение кнопок в VBox
        VBox buttonsVBox = new VBox(addChartButton);
        buttonsVBox.setSpacing(5);

        // Размещение TableView и кнопки обновления в VBox
        VBox tableVBox = new VBox();
        tableVBox.getChildren().addAll(spectralDataTableView);

        // Создание SplitPane для разделения TableView и области с графиками (innerTabPane)
        SplitPane tableAndChartsSplitPane = new SplitPane();
        tableAndChartsSplitPane.setOrientation(Orientation.HORIZONTAL);
        tableAndChartsSplitPane.getItems().addAll(tableVBox, innerTabPane);
        tableAndChartsSplitPane.setDividerPositions(0.2); // Установка начального положения разделителя

        // Создание SplitPane для объединения tableAndChartsSplitPane и кнопок управления
        SplitPane tabsAndButtonsSplitPane = new SplitPane();
        tabsAndButtonsSplitPane.setOrientation(Orientation.HORIZONTAL);
        tabsAndButtonsSplitPane.getItems().addAll(tableAndChartsSplitPane, buttonsVBox);
        tabsAndButtonsSplitPane.setDividerPositions(0.9); // Установка начального положения разделителя

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

        // Создание SplitPane для разделения основного изображения и миниатюр
        SplitPane imageAndThumbnailsSplitPane = new SplitPane();
        imageAndThumbnailsSplitPane.setOrientation(Orientation.HORIZONTAL);
        imageAndThumbnailsSplitPane.getItems().addAll(mainImageScrollPane, thumbnailsScrollPane);
        imageAndThumbnailsSplitPane.setDividerPositions(0.8); // Установка начального положения разделителя

        // Создание главного вертикального SplitPane для объединения всего содержимого вкладки
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(tabsAndButtonsSplitPane, imageAndThumbnailsSplitPane);
        mainSplitPane.setDividerPositions(0.6); // Установка начального положения разделителя

        // Возврат главного SplitPane как содержимого вкладки
        return mainSplitPane;
    }

    /**
     * Метод для обработки нажатия кнопки "Добавить график"
     */
    private void handleAddButtonClick(HelloController controller) {
        // Создание осей для графика
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Создание LineChart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Новый график");

        // Создание новой вкладки с графиком
        Tab newInnerTab = new Tab("График", lineChart);

        TabPane innerTabPaneCurrent = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem());

        innerTabPaneCurrent.getTabs().add(newInnerTab);
        innerTabPaneCurrent.getSelectionModel().select(newInnerTab);

        Tab selectedNewInnerTab = innerTabPaneCurrent.getSelectionModel().getSelectedItem();

        // Добавление слушателя для обновления TableView при выборе вкладки с графиком
        selectedNewInnerTab.setOnSelectionChanged(event -> {
            if (selectedNewInnerTab.isSelected()) {
                updateTableViewFromActiveTab(controller);
            }
        });
    }

    /**
     * Метод для обновления TableView с использованием данных из графика на активной вкладке
     *
     * @param controller - контроллер приложения
     */
    protected void updateTableViewFromActiveTab(HelloController controller) {
        // Получение активной вкладки во внутреннем TabPane
        Tab currentInnerTab = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();

        // Проверка, что вкладка содержит LineChart
        if (currentInnerTab.getContent() instanceof LineChart) {
            LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();
            if (!currentChart.getData().isEmpty()) {
                XYChart.Series<Number, Number> series = currentChart.getData().get(0);
                // Получение TableView, соответствующей активной вкладке во внешнем TabPane
                TableView<SpectralDataTable.SpectralData> tableViewToUpdate = controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());
                // Обновление TableView
                SpectralDataTable.updateTableViewInTab(tabPane.getSelectionModel().getSelectedItem(), series.getData(), tableViewToUpdate);
            }
        }
    }
}