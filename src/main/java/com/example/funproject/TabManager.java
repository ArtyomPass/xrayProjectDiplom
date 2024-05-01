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
    // TableView для отображения данных спектра в каждой вкладке
    private TableView<SpectralDataTable.SpectralData> spectralDataTableView;

    // Кнопки управления
    private Button addChartButton;
    private Button normalizeButton;
    private Button interpolateButton;
    private Button backgroundButton;
    private Button smoothButton;
    private Button correctionButton;
    private Button statisticsButton;

    /**
     * Конструктор TabManager.
     *
     * @param tabPane - TabPane, с которым будет работать класс
     */
    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;
        this.innerTabPanes = new HashMap<>();
    }

    /**
     * Создает новую вкладку с заданным названием и содержимым.
     *
     * @param title      - название новой вкладки
     * @param controller - контроллер приложения
     */
    public void createNewTab(String title, HelloController controller, Map<Tab, ImageProcessor> imageProcessors) {
        // Инициализация TableView для новой вкладки
        SpectralDataTable spectralDataTable = new SpectralDataTable();
        spectralDataTableView = spectralDataTable.getTableView();
        spectralDataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Создание содержимого вкладки
        SplitPane tabContent = createTabContent(controller);

        // Создание новой вкладки с содержимым
        Tab newTab = new Tab(title, tabContent);

        // Добавление новой вкладки в TabPane
        tabPane.getTabs().add(newTab);

        // Выбор новой вкладки
        tabPane.getSelectionModel().select(newTab);

        // Сохранение TableView для новой вкладки в контроллере
        controller.spectralDataTableViews.put(newTab, spectralDataTableView);

        // Сохранение внутреннего TabPane для графиков в Map
        innerTabPanes.put(newTab, innerTabPane);

        // Обработка нажатия кнопки "Добавить график"
        handleAddButtonClick(controller);



    }

    /**
     * Создает содержимое для новой вкладки, включая SplitPane с графиками, TableView и кнопками управления.
     *
     * @param controller - контроллер приложения
     * @return SplitPane - содержимое вкладки
     */
    private SplitPane createTabContent(HelloController controller) {
        // Создание внутреннего TabPane для графиков
        this.innerTabPane = new TabPane();

        // Создание кнопок управления
        addChartButton = new Button("Добавить график");
        addChartButton.setOnAction(event -> handleAddButtonClick(controller));
        normalizeButton = new Button("Нормировать");
        normalizeButton.setOnAction(event -> handleNormalizeButtonClick(controller));
        interpolateButton = new Button("Интерполяция");
        interpolateButton.setOnAction(event -> handleInterpolateButtonClick(controller));
        backgroundButton = new Button("Фон");
        backgroundButton.setOnAction(event -> handleSubtractBackgroundButtonClick(controller));
        smoothButton = new Button("Сглаживание");
        smoothButton.setOnAction(event -> handleSmoothButtonClick(controller));
        correctionButton = new Button("Коррекция");
        correctionButton.setOnAction(event -> handleCorrectionButtonClick(controller));
        statisticsButton = new Button("Статистика");
        statisticsButton.setOnAction(event -> handleStatisticsButtonClick(controller));

        // Настройка внешнего вида кнопок
        addChartButton.setMaxWidth(Double.MAX_VALUE);
        normalizeButton.setMaxWidth(Double.MAX_VALUE);
        interpolateButton.setMaxWidth(Double.MAX_VALUE);
        backgroundButton.setMaxWidth(Double.MAX_VALUE);
        smoothButton.setMaxWidth(Double.MAX_VALUE);
        correctionButton.setMaxWidth(Double.MAX_VALUE);
        statisticsButton.setMaxWidth(Double.MAX_VALUE);

        // Размещение кнопок в VBox
        VBox buttonsVBox = new VBox(addChartButton, normalizeButton, interpolateButton,
                backgroundButton, smoothButton, correctionButton, statisticsButton);
        buttonsVBox.setSpacing(5);

        // Размещение TableView в VBox
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

    // Обработчики событий для кнопок управления

    private void handleNormalizeButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Получить TableView
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());

        // Создать и показать окно нормировки
        NormalizationWindow normalizationWindow = new NormalizationWindow(controller, currentChart, tableViewToUpdate);
        normalizationWindow.show();
    }

    private void handleInterpolateButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Создать и показать окно интерполяции
        InterpolateWindow interpolateWindow = new InterpolateWindow(controller, currentChart);
        interpolateWindow.show();
    }

    private void handleSubtractBackgroundButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Создать и показать окно вычитания фона
        BackgroundSubtractionWindow backgroundWindow = new BackgroundSubtractionWindow(controller, currentChart);
        backgroundWindow.show();
    }

    private void handleSmoothButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Создать и показать окно сглаживания
        SmoothWindow smoothWindow = new SmoothWindow(controller, currentChart);
        smoothWindow.show();
    }

    private void handleCorrectionButtonClick(HelloController controller) {
        // TODO: Реализовать обработку нажатия кнопки "Коррекция"
    }

    private void handleStatisticsButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Создать и показать окно статистики
        StatisticsWindow statisticsWindow = new StatisticsWindow(controller, currentChart);
        statisticsWindow.show();
    }

    /**
     * Обрабатывает нажатие кнопки "Добавить график".
     * Создает новый LineChart и добавляет его на новую вкладку во внутреннем TabPane.
     *
     * @param controller - контроллер приложения
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

        // Добавление слушателя для обновления TableView при выборе вкладки с графиком
        newInnerTab.setOnSelectionChanged(event -> {
            if (newInnerTab.isSelected()) {
                updateTableViewFromActiveTab(controller);
            }
        });
    }

    /**
     * Обновляет TableView с использованием данных из графика на активной вкладке.
     *
     * @param controller - контроллер приложения
     */
    protected void updateTableViewFromActiveTab(HelloController controller) {
        // Получение активной вкладки во внутреннем TabPane
        Tab currentInnerTab = innerTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();

        // Проверка, что вкладка содержит LineChart
        if (currentInnerTab.getContent() instanceof LineChart) {
            LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

            // Проверка, что график содержит данные
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