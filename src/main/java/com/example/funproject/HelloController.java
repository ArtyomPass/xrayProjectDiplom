package com.example.funproject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.*;

public class HelloController {

    // Элементы интерфейса из FXML-файла
    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox sidebar;
    @FXML
    protected TabPane tabPane;


    // Вспомогательные классы для работы с данными и изображениями
    private final FileImporter fileImporter = new FileImporter();
    protected TabManager tabManager;
    private DataPreprocessing dataPreprocessing;
    protected SpectraAnalysis spectraAnalysis;

    // Хранилища данных для различных типов информации
    protected Map<Tab, List<Image>> xRayImages = new HashMap<>(); // Хранит все изображения для спектра
    private Map<Tab, List<XYChart.Data<Number, Number>>> detectedPeaks = new HashMap<>(); // Хранит обнаруженные пики
    private Map<Tab, ImageProcessor> imageProcessors = new HashMap<>();

    // Линии для картинок и для графиков
    protected Map<Image, List<LineInfo>> imageLines;
    protected Map<Tab, List<LineInfo>> chartLines;

    protected Map<Tab, TableView<SpectralDataTable.SpectralData>> spectralDataTableViews = new HashMap<>();
    protected Map<Tab, XYChart.Series<Number, Number>> spectralDataSeries = new HashMap<>(); // Хранит данные для графика и таблицы

    // Параметры для анализа пиков
    private int windowSize = 20;
    private double minPeakDistance = 6.0;
    private double threshold = 20.1;

    // Параметры для сглаживания изображений
    private int kernelSize = 9;

    /**
     * Инициализация контроллера.
     * Создает экземпляры вспомогательных классов и настраивает начальное состояние приложения.
     */
    @FXML
    public void initialize() {
        tabManager = new TabManager(tabPane, imageProcessors);
        dataPreprocessing = new DataPreprocessing();
        spectraAnalysis = new SpectraAnalysis();
        System.out.println("The program is started, ready to work");

        // Создаем первую вкладку при запуске приложения
        imageLines = new HashMap<>();
        chartLines = new HashMap<>();
        handleNewTab();
    }

    /**
     * Создает новую вкладку с заданным названием.
     */
    @FXML
    public void handleNewTab() {
        tabManager.createNewTab("Tab " + (tabPane.getTabs().size() + 1), this);
    }

    /**
     * Обрабатывает импорт рентгеновского изображения.
     * Открывает диалог выбора файлов и добавляет выбранные изображения на текущую вкладку.
     */
    @FXML
    public void handleImportXRayImage() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        List<Image> importedImages = fileImporter.importData(mainContainer.getScene().getWindow());
        this.xRayImages.put(currentTab, importedImages);
        imageProcessors.get(currentTab).putImagesAndButtonsOnTabPane(this.xRayImages, currentTab);
    }

    /**
     * Обрабатывает сглаживание изображения.
     * Применяет фильтр усреднения (Box Blur) к выбранному изображению.
     */
    public void handleImageSmoothing(ActionEvent event) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

        Image selectedImage = imageProcessors.get(currentTab).selectedImage;
        if (selectedImage == null) {
            System.out.println("No image selected for processing.");
            return;
        }

        // Создание контекстного меню
        ContextMenu processingMenu = new ContextMenu();
        MenuItem smoothItem = new MenuItem("Сгладить");
        MenuItem densityItem = new MenuItem("Денситометрия");
        processingMenu.getItems().addAll(smoothItem, densityItem);

        // Обработчики событий для пунктов меню
        smoothItem.setOnAction(e -> {
            Image smoothedImage = dataPreprocessing.imageSmoothing(selectedImage, kernelSize);
            updateImageInTab(currentTab, selectedImage, smoothedImage);
            imageProcessors.get(currentTab).selectedImage = smoothedImage;
        });

        densityItem.setOnAction(e -> {
            WritableImage densityImage = dataPreprocessing.applyDensity(selectedImage);
            updateImageInTab(currentTab, selectedImage, densityImage);
        });

        // Отображение контекстного меню
        processingMenu.show((Node) event.getSource(), Side.BOTTOM, 0, 0);
    }

    private void updateImageInTab(Tab currentTab, Image oldImage, Image newImage) {
        List<Image> currentImages = xRayImages.getOrDefault(currentTab, new ArrayList<>());
        int selectedIndex = currentImages.indexOf(oldImage);
        if (selectedIndex != -1) {
            currentImages.set(selectedIndex, newImage);
            imageProcessors.get(currentTab).imageView.setImage(newImage);
        } else {
            System.out.println("Selected image not found in the list.");
            return;
        }
        xRayImages.put(currentTab, currentImages);
        imageProcessors.get(currentTab).putImagesAndButtonsOnTabPane(xRayImages, currentTab);
    }

    /**
     * Обрабатывает визуализацию спектра.
     * Извлекает данные из выбранного изображения и строит график спектра.
     */
    public void spectraVisualization(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        Image selectedImage = imageProcessors.get(currentTab).selectedImage;
        TabPane currentInnerTabPane = tabManager.innerTableAndChartTabPanes.get(tabPane.getSelectionModel().getSelectedItem()); // Получаем innerTabPane
        XYChart.Series<Number, Number> series = spectraAnalysis.updateChartWithSplineData(currentTab, selectedImage, currentInnerTabPane);
        if (series == null) {
            System.out.println("Unable to generate spectral data for the selected Image");
            return;
        }

        spectralDataSeries.put(tabManager.innerTableAndChartTabPanes.get(tabPane
                        .getSelectionModel()
                        .getSelectedItem())
                .getSelectionModel()
                .getSelectedItem(), series);

        SpectralDataTable.updateTableViewInTab(currentTab, new ArrayList<>(series.getData()), spectralDataTableViews.get(currentTab));
    }

    /**
     * Выполняет калибровку спектрометра.
     * Отображает контекстное меню для выбора метода калибровки (в данном случае, линейная регрессия).
     */
    public void spectrumCalibration(ActionEvent actionEvent) {
        // Получить выбранную вкладку
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        // Получить текущую внутреннюю вкладку и график
        Tab currentInnerTab = tabManager.innerTableAndChartTabPanes.get(selectedTab).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();
        // Получить текущий TableView для обновления
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = spectralDataTableViews.get(tabPane.getSelectionModel().getSelectedItem());

        // Получить линии с текущей открытой вкладки
        List<LineInfo> lineChartInfos = chartLines.get(currentInnerTab);
        // Получить линии с выбранного изображения
        List<LineInfo> lineImageInfos = imageLines.get(imageProcessors.get(selectedTab).selectedImage);

        CalibrationDialog dialog = new CalibrationDialog(selectedTab,lineImageInfos, lineChartInfos, currentChart, tableViewToUpdate);
    }
}