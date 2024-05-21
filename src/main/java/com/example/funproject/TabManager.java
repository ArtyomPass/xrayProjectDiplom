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
import java.util.Map;

/**
 * Класс TabManager управляет вкладками в приложении, включая создание вкладок,
 * отображение данных спектра, создание графиков и обработку событий кнопок управления.
 */
public class TabManager {
    private final TabPane tabPane;
    private final Map<Tab, ImageProcessor> imageProcessors;
    private ImageProcessor imageProcessor;
    private TabPane innerTabPane;
    protected Map<Tab, TabPane> innerTableAndChartTabPanes = new HashMap<>();
    private TableView<SpectralDataTable.SpectralData> spectralDataTableView;
    private Button addChartButton, normalizeButton, interpolateButton, backgroundButton, smoothButton, correctionButton, statisticsButton, seriesManagementButton;
    protected ImageView mainImageView;
    protected TilePane thumbnailsTilePane;
    private Tab newTab;
    private boolean isCroppingMode = false;
    private ChartCropper currentCropper;

    /**
     * Конструктор TabManager.
     *
     * @param tabPane         TabPane, с которым будет работать класс
     * @param imageProcessors Карта, связывающая вкладки с экземплярами ImageProcessor
     */
    public TabManager(TabPane tabPane, Map<Tab, ImageProcessor> imageProcessors) {
        this.tabPane = tabPane;
        this.imageProcessors = imageProcessors;
    }

    /**
     * Создает новую вкладку с заданным названием и содержимым.
     *
     * @param title      Название новой вкладки
     * @param controller Контроллер приложения
     */
    public void createNewTab(String title, HelloController controller) {
        SplitPane tabContent = createTabContent(controller);
        newTab = new Tab(title, tabContent);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        controller.spectralDataTableViews.put(newTab, spectralDataTableView);
        innerTableAndChartTabPanes.put(newTab, innerTabPane);
        handleAddButtonClick(controller);
        imageProcessors.put(newTab, imageProcessor);
    }

    /**
     * Создает содержимое для новой вкладки, включая SplitPane с графиками, TableView и кнопками управления.
     *
     * @param controller Контроллер приложения
     * @return SplitPane содержимое вкладки
     */
    private SplitPane createTabContent(HelloController controller) {
        createControlButtons(controller);
        VBox buttonsVBox = createButtonsVBox();

        innerTabPane = new TabPane();
        spectralDataTableView = new SpectralDataTable().getTableView();
        spectralDataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        SplitPane tableAndChartsSplitPane = createSplitPane(spectralDataTableView, innerTabPane, 0.2);
        SplitPane tabsAndButtonsSplitPane = createSplitPane(tableAndChartsSplitPane, buttonsVBox, 0.84);

        mainImageView = new ImageView();
        mainImageView.setPreserveRatio(true);
        ScrollPane mainImageScrollPane = createScrollPane(mainImageView, true, true);
        thumbnailsTilePane = createThumbnailsTilePane();
        ScrollPane thumbnailsScrollPane = createScrollPane(thumbnailsTilePane, true, false);

        SplitPane imageAndThumbnailsSplitPane = createSplitPane(mainImageScrollPane, thumbnailsScrollPane, 0.8);
        imageProcessor = new ImageProcessor(controller, mainImageView, thumbnailsTilePane);
        ImageControlPanel imageControlPanel = new ImageControlPanel(controller, imageProcessor, mainImageView, innerTabPane);

        BorderPane mainImageAndControlsPane = new BorderPane();
        mainImageAndControlsPane.setCenter(imageAndThumbnailsSplitPane);
        mainImageAndControlsPane.setBottom(imageControlPanel);

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(tabsAndButtonsSplitPane, mainImageAndControlsPane);
        mainSplitPane.setDividerPositions(0.6);

        return mainSplitPane;
    }

    /**
     * Создает VBox для кнопок управления.
     *
     * @return VBox с кнопками управления
     */
    private VBox createButtonsVBox() {
        VBox buttonsVBox = new VBox(addChartButton, normalizeButton, interpolateButton, backgroundButton, smoothButton, statisticsButton, correctionButton, seriesManagementButton);
        buttonsVBox.setSpacing(5);
        return buttonsVBox;
    }

    /**
     * Создает SplitPane с заданными элементами и делителем.
     *
     * @param firstItem  Первый элемент SplitPane
     * @param secondItem Второй элемент SplitPane
     * @param dividerPos Позиция делителя
     * @return Созданный SplitPane
     */
    private SplitPane createSplitPane(javafx.scene.Node firstItem, javafx.scene.Node secondItem, double dividerPos) {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(firstItem, secondItem);
        splitPane.setDividerPositions(dividerPos);
        return splitPane;
    }

    /**
     * Создает ScrollPane с заданным элементом и политиками прокрутки.
     *
     * @param content       Элемент для отображения в ScrollPane
     * @param fitToWidth    Политика прокрутки по ширине
     * @param fitToHeight   Политика прокрутки по высоте
     * @return Созданный ScrollPane
     */
    private ScrollPane createScrollPane(javafx.scene.Node content, boolean fitToWidth, boolean fitToHeight) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(fitToWidth);
        scrollPane.setFitToHeight(fitToHeight);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    /**
     * Создает TilePane для миниатюр изображений.
     *
     * @return Созданный TilePane
     */
    private TilePane createThumbnailsTilePane() {
        TilePane tilePane = new TilePane();
        tilePane.setPrefColumns(1);
        tilePane.setPadding(new Insets(5));
        tilePane.setVgap(5);
        return tilePane;
    }

    /**
     * Создает кнопки управления.
     *
     * @param controller Контроллер приложения
     */
    private void createControlButtons(HelloController controller) {
        addChartButton = createButton("Добавить график", event -> handleAddButtonClick(controller));
        normalizeButton = createButton("Нормировать", event -> handleNormalizeButtonClick(controller));
        interpolateButton = createButton("Интерполяция", event -> handleInterpolateButtonClick(controller));
        backgroundButton = createButton("Фон", event -> handleSubtractBackgroundButtonClick(controller));
        smoothButton = createButton("Сглаживание", event -> handleSmoothButtonClick(controller));
        correctionButton = createButton("Обрезать график", event -> handleCorrectionButtonClick(controller));
        statisticsButton = createButton("Статистика", event -> handleStatisticsButtonClick(controller));
        seriesManagementButton = createButton("Управление сериями", event -> handleSeriesManagementButtonClick(controller));
    }

    /**
     * Создает кнопку с заданным текстом и обработчиком событий.
     *
     * @param text    Текст кнопки
     * @param handler Обработчик событий
     * @return Созданная кнопка
     */
    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setOnAction(handler);
        button.setPrefWidth(Double.MAX_VALUE);
        button.getStyleClass().add("control-button");
        return button;
    }

    /**
     * Обработчик нажатия кнопки "Добавить график".
     *
     * @param controller Контроллер приложения
     */
    private void handleAddButtonClick(HelloController controller) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Новый график");

        Tab newInnerTab = new Tab("График", lineChart);
        TabPane innerTabPaneCurrent = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem());
        innerTabPaneCurrent.getTabs().add(newInnerTab);
        innerTabPaneCurrent.getSelectionModel().select(newInnerTab);

        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());
        tableViewToUpdate.getItems().clear();

        lineChart.getData().addListener((ListChangeListener<XYChart.Series<Number, Number>>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (XYChart.Series<Number, Number> series : change.getAddedSubList()) {
                        series.getNode().setOnMouseClicked(event -> updateTableViewFromSeries(series, tableViewToUpdate));
                    }
                }
            }
        });

        newInnerTab.setOnSelectionChanged(event -> {
            if (newInnerTab.isSelected()) {
                updateTableViewFromActiveTab(controller);
            }
        });
    }

    /**
     * Обновляет TableView на основе выбранной серии данных.
     *
     * @param series           Выбранная серия данных
     * @param tableViewToUpdate TableView для обновления
     */
    private void updateTableViewFromSeries(XYChart.Series<Number, Number> series, TableView<SpectralDataTable.SpectralData> tableViewToUpdate) {
        SpectralDataTable.updateTableViewInTab(tabPane.getSelectionModel().getSelectedItem(), series.getData(), tableViewToUpdate);
    }

    /**
     * Обновляет TableView с использованием данных из графика на активной вкладке.
     *
     * @param controller Контроллер приложения
     */
    protected void updateTableViewFromActiveTab(HelloController controller) {
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();

        if (currentInnerTab.getContent() instanceof LineChart) {
            LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();
            TableView<SpectralDataTable.SpectralData> tableViewToUpdate = controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());

            if (!currentChart.getData().isEmpty()) {
                XYChart.Series<Number, Number> series = findNonVerticalLineSeries(currentChart);
                if (series != null) {
                    SpectralDataTable.updateTableViewInTab(tabPane.getSelectionModel().getSelectedItem(), series.getData(), tableViewToUpdate);
                } else {
                    tableViewToUpdate.getItems().clear();
                }
            } else {
                tableViewToUpdate.getItems().clear();
            }
        }
    }

    /**
     * Находит последнюю серию данных, которая не является "Vertical Line".
     *
     * @param currentChart Текущий график
     * @return Найденная серия данных или null
     */
    private XYChart.Series<Number, Number> findNonVerticalLineSeries(LineChart<Number, Number> currentChart) {
        for (int i = currentChart.getData().size() - 1; i >= 0; i--) {
            XYChart.Series<Number, Number> series = currentChart.getData().get(i);
            if (!"Vertical Line".equals(series.getName())) {
                return series;
            }
        }
        return null;
    }

    // Обработчики событий для кнопок управления
    private void handleNormalizeButtonClick(HelloController controller) {
        LineChart<Number, Number> currentChart = getCurrentChart(controller);
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = getCurrentTableView(controller);
        NormalizationWindow normalizationWindow = new NormalizationWindow(controller, currentChart, tableViewToUpdate);
        normalizationWindow.show();
    }

    private void handleInterpolateButtonClick(HelloController controller) {
        LineChart<Number, Number> currentChart = getCurrentChart(controller);
        InterpolateWindow interpolateWindow = new InterpolateWindow(controller, currentChart);
        interpolateWindow.show();
    }

    private void handleSubtractBackgroundButtonClick(HelloController controller) {
        LineChart<Number, Number> currentChart = getCurrentChart(controller);
        BackgroundSubtractionWindow backgroundWindow = new BackgroundSubtractionWindow(currentChart);;
        backgroundWindow.show();
    }

    private void handleSmoothButtonClick(HelloController controller) {
        LineChart<Number, Number> currentChart = getCurrentChart(controller);
        SmoothWindow smoothWindow = new SmoothWindow(controller, currentChart);
        smoothWindow.show();
    }

    private void handleCorrectionButtonClick(HelloController controller) {
        LineChart<Number, Number> currentChart = getCurrentChart(controller);

        if (isCroppingMode) {
            // Отключаем режим обрезки
            if (currentCropper != null) {
                currentCropper.exitCroppingMode();
                currentCropper = null; // Убедитесь, что текущий объект сброшен
            }
        } else {
            // Включаем режим обрезки
            correctionButton.setStyle("-fx-background-color: lightblue;");
            currentCropper = new ChartCropper(currentChart);
            currentCropper.setOnCropComplete(() -> {
                correctionButton.setStyle("");
                isCroppingMode = false;  // Обновить состояние
            });
            currentCropper.enterCroppingMode();
            isCroppingMode = true;  // Обновить состояние
        }
    }


    private void handleStatisticsButtonClick(HelloController controller) {
        LineChart<Number, Number> currentChart = getCurrentChart(controller);
        StatisticsWindow statisticsWindow = new StatisticsWindow(controller, currentChart);
        statisticsWindow.show();
    }

    private void handleSeriesManagementButtonClick(HelloController controller) {
        LineChart<Number, Number> currentChart = getCurrentChart(controller);
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = getCurrentTableView(controller);
        SeriesManagementWindow seriesManagementWindow = new SeriesManagementWindow(currentChart, tableViewToUpdate, newTab);
        seriesManagementWindow.show();
    }

    /**
     * Получает текущий LineChart из активной вкладки.
     *
     * @param controller Контроллер приложения
     * @return Текущий LineChart
     */
    private LineChart<Number, Number> getCurrentChart(HelloController controller) {
        Tab currentInnerTab = innerTableAndChartTabPanes.get(controller.tabPane.getSelectionModel().getSelectedItem()).getSelectionModel().getSelectedItem();
        return (LineChart<Number, Number>) currentInnerTab.getContent();
    }

    /**
     * Получает текущий TableView из активной вкладки.
     *
     * @param controller Контроллер приложения
     * @return Текущий TableView
     */
    private TableView<SpectralDataTable.SpectralData> getCurrentTableView(HelloController controller) {
        return controller.spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());
    }
}
