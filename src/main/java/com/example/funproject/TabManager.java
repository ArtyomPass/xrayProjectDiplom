package com.example.funproject;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс TabManager управляет вкладками в приложении, включая создание вкладок,
 * отображение данных спектра, создание графиков и обработку событий кнопок управления.
 */
public class TabManager {

    // Глобальная переменная для хранения TabPane, с которым работает класс
    private final TabPane tabPane;

    // Карта, связывающая вкладки с экземплярами ImageProcessor для обработки изображений
    private final Map<Tab, ImageProcessor> imageProcessors;

    // Экземпляр ImageProcessor для текущей вкладки
    private ImageProcessor imageProcessor;

    // Глобальная переменная для хранения внутреннего TabPane для графиков
    private TabPane innerTabPane;

    // Карта, связывающая вкладки с внутренними TabPane для графиков и таблиц
    protected Map<Tab, TabPane> innerTableAndChartTabPanes;

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
    private Button seriesManagementButton;

    // ImageView для отображения основного изображения
    protected ImageView mainImageView;

    // TilePane для отображения миниатюр изображений
    protected TilePane thumbnailsTilePane;

    // Ссылка на новую созданную вкладку
    private Tab newTab;

    /**
     * Конструктор TabManager.
     *
     * @param tabPane       - TabPane, с которым будет работать класс
     * @param imageProcessors - карта, связывающая вкладки с экземплярами ImageProcessor
     */
    public TabManager(TabPane tabPane, Map<Tab, ImageProcessor> imageProcessors) {
        this.tabPane = tabPane;
        this.innerTableAndChartTabPanes = new HashMap<>();
        this.imageProcessors = imageProcessors;
    }

    /**
     * Создает новую вкладку с заданным названием и содержимым.
     *
     * @param title      - название новой вкладки
     * @param controller - контроллер приложения
     */
    public void createNewTab(String title, HelloController controller) {
        // Создание содержимого вкладки
        SplitPane tabContent = createTabContent(controller);

        // Создание новой вкладки с содержимым
        newTab = new Tab(title, tabContent);

        // Добавление новой вкладки в TabPane
        tabPane.getTabs().add(newTab);

        // Выбор новой вкладки
        tabPane.getSelectionModel().select(newTab);

        // Сохранение TableView для новой вкладки в контроллере
        controller.spectralDataTableViews.put(newTab, spectralDataTableView);

        // Сохранение внутреннего TabPane для графиков в Map
        innerTableAndChartTabPanes.put(newTab, innerTabPane);

        // Обработка нажатия кнопки "Добавить график"
        handleAddButtonClick(controller);

        // Сохранение ImageProcessor для новой вкладки
        imageProcessors.put(newTab, imageProcessor);
    }

    /**
     * Создает содержимое для новой вкладки, включая SplitPane с графиками, TableView и кнопками управления.
     *
     * @param controller - контроллер приложения
     * @return SplitPane - содержимое вкладки
     */
    private SplitPane createTabContent(HelloController controller) {
        // Создание кнопок управления
        addChartButton = createButton("Добавить график", event -> handleAddButtonClick(controller));
        normalizeButton = createButton("Нормировать", event -> handleNormalizeButtonClick(controller));
        interpolateButton = createButton("Интерполяция", event -> handleInterpolateButtonClick(controller));
        backgroundButton = createButton("Фон", event -> handleSubtractBackgroundButtonClick(controller));
        smoothButton = createButton("Сглаживание", event -> handleSmoothButtonClick(controller));
        correctionButton = createButton("Коррекция", event -> handleCorrectionButtonClick(controller));
        statisticsButton = createButton("Статистика", event -> handleStatisticsButtonClick(controller));
        seriesManagementButton = createButton("Управление сериями", event -> handleSeriesManagementButtonClick(controller));

        // Размещение кнопок в VBox
        VBox buttonsVBox = new VBox(addChartButton, normalizeButton, interpolateButton,
                backgroundButton, smoothButton, correctionButton, statisticsButton, seriesManagementButton);
        buttonsVBox.setSpacing(5);

        // Создание внутреннего TabPane для графиков и инициализация TableView для новой вкладки
        this.innerTabPane = new TabPane();
        spectralDataTableView = new SpectralDataTable().getTableView();
        spectralDataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Создание SplitPane для разделения TableView и области с графиками (innerTabPane)
        SplitPane tableAndChartsSplitPane = new SplitPane();
        tableAndChartsSplitPane.setOrientation(Orientation.HORIZONTAL);
        tableAndChartsSplitPane.getItems().addAll(spectralDataTableView, innerTabPane);
        tableAndChartsSplitPane.setDividerPositions(0.2);

        // Создание SplitPane для объединения tableAndChartsSplitPane и кнопок управления
        SplitPane tabsAndButtonsSplitPane = new SplitPane();
        tabsAndButtonsSplitPane.setOrientation(Orientation.HORIZONTAL);
        tabsAndButtonsSplitPane.getItems().addAll(tableAndChartsSplitPane, buttonsVBox);
        tabsAndButtonsSplitPane.setDividerPositions(0.9);

        // Инициализация ImageView для основного изображения
        mainImageView = new ImageView();
        mainImageView.setPreserveRatio(true);
        ScrollPane mainImageScrollPane = new ScrollPane(mainImageView);
        mainImageScrollPane.setFitToWidth(true);
        mainImageScrollPane.setFitToHeight(true);
        mainImageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainImageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Инициализация TilePane для миниатюр изображений
        thumbnailsTilePane = new TilePane();
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
        imageAndThumbnailsSplitPane.setDividerPositions(0.8);

        // Создаем экземпляр ImageProcessor для обработки изображений
        imageProcessor = new ImageProcessor(controller, mainImageView, thumbnailsTilePane);

        // Создаем панель управления изображениями
        ImageControlPanel imageControlPanel = new ImageControlPanel(
                controller,
                imageProcessor,
                mainImageView,
                innerTabPane);

        // Создайте VBox для размещения изображения и панели управления
        BorderPane mainImageAndControlsPane = new BorderPane();
        mainImageAndControlsPane.setCenter(imageAndThumbnailsSplitPane);
        mainImageAndControlsPane.setBottom(imageControlPanel);

        // Создание главного вертикального SplitPane для объединения всего содержимого вкладки
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(tabsAndButtonsSplitPane, mainImageAndControlsPane);
        mainSplitPane.setDividerPositions(0.6);

        // Возврат главного SplitPane как содержимого вкладки
        return mainSplitPane;
    }

    // Метод для создания кнопки с заданным текстом и обработчиком событий
    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setOnAction(handler);
        button.setPrefWidth(150);
        button.getStyleClass().add("control-button");
        return button;
    }

    // Обработчики событий для кнопок управления
    private void handleNormalizeButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel()
                .getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Получить TableView
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate =
                controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());

        // Создать и показать окно нормировки
        NormalizationWindow normalizationWindow = new NormalizationWindow(controller, currentChart, tableViewToUpdate);
        normalizationWindow.show();
    }

    private void handleInterpolateButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Создать и показать окно интерполяции
        InterpolateWindow interpolateWindow = new InterpolateWindow(controller, currentChart);
        interpolateWindow.show();
    }

    private void handleSubtractBackgroundButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Создать и показать окно вычитания фона
        BackgroundSubtractionWindow backgroundWindow = new BackgroundSubtractionWindow(controller, currentChart);
        backgroundWindow.show();
    }

    private void handleSmoothButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
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
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Создать и показать окно статистики
        StatisticsWindow statisticsWindow = new StatisticsWindow(controller, currentChart);
        statisticsWindow.show();
    }

    private void handleSeriesManagementButtonClick(HelloController controller) {
        // Получить активную вкладку и график
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // Получить TableView
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate =
                controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());

        // Создать и показать окно управления сериями
        SeriesManagementWindow seriesManagementWindow = new SeriesManagementWindow(currentChart,
                tableViewToUpdate,
                newTab);
        seriesManagementWindow.show();
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
        TabPane innerTabPaneCurrent = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem());
        innerTabPaneCurrent.getTabs().add(newInnerTab);
        innerTabPaneCurrent.getSelectionModel().select(newInnerTab);

        // Получить TableView для текущей вкладки
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());

        // Очистить данные в TableView
        tableViewToUpdate.getItems().clear();

        // Добавить слушатель для обновления TableView при выборе серии данных
        lineChart.getData().addListener((ListChangeListener<XYChart.Series<Number, Number>>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (XYChart.Series<Number, Number> series : change.getAddedSubList()) {
                        series.getNode().setOnMouseClicked(event -> updateTableViewFromSeries(series, tableViewToUpdate));
                    }
                }
            }
        });

        // Добавление слушателя для обновления TableView при выборе вкладки с графиком
        newInnerTab.setOnSelectionChanged(event -> {
            if (newInnerTab.isSelected()) {
                updateTableViewFromActiveTab(controller);
            }
        });
    }

    // Метод для обновления таблицы на основе выбранной серии данных
    private void updateTableViewFromSeries(XYChart.Series<Number, Number> series, TableView<SpectralDataTable.SpectralData> tableViewToUpdate) {
        // Обновить TableView с данными из выбранной серии
        SpectralDataTable.updateTableViewInTab(tabPane.getSelectionModel().getSelectedItem(), series.getData(), tableViewToUpdate);
    }

    /**
     * Обновляет TableView с использованием данных из графика на активной вкладке.
     *
     * @param controller - контроллер приложения
     */
    protected void updateTableViewFromActiveTab(HelloController controller) {
        // Получение активной вкладки во внутреннем TabPane
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();

        // Проверка, что вкладка содержит LineChart
        if (currentInnerTab.getContent() instanceof LineChart) {
            LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

            // Получение TableView, соответствующей активной вкладке во внешнем TabPane
            TableView<SpectralDataTable.SpectralData> tableViewToUpdate = controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());

            // Проверка, что график содержит данные
            if (!currentChart.getData().isEmpty()) {
                // Поиск последней серии, которая не "Vertical Lines"
                XYChart.Series<Number, Number> series = null;
                for (int i = currentChart.getData().size() - 1; i >= 0; i--) {
                    if (!currentChart.getData().get(i).getName().equals("Vertical Line")) {
                        series = currentChart.getData().get(i);
                        break; // Выходим из цикла, как только нашли подходящую серию
                    }
                }

                // Обновление TableView, если подходящая серия найдена
                if (series != null) {
                    SpectralDataTable.updateTableViewInTab(tabPane.getSelectionModel().getSelectedItem(), series.getData(), tableViewToUpdate);
                } else {
                    // Очистить данные в TableView, если подходящая серия не найдена
                    tableViewToUpdate.getItems().clear();
                }
            } else {
                // Очистить данные в TableView, если график пуст
                tableViewToUpdate.getItems().clear();
            }
        }
    }
}