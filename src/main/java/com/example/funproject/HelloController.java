package com.example.funproject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloController {

    // Элементы интерфейса из FXML-файла
    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox sidebar;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button spectrumCalibration;

    // Вспомогательные классы для работы с данными и изображениями
    private final FileImporter fileImporter = new FileImporter();
    private ImageProcessor imageProcessor;
    private TabManager tabManager;
    private DataPreprocessing dataPreprocessing;
    private SpectraAnalysis spectraAnalysis;

    // Хранилища данных для различных типов информации
    protected Map<Tab, List<Image>> xRayImages = new HashMap<>(); // Хранит все изображения для спектра
    private Map<Tab, Image> calibrationImages = new HashMap<>(); // Хранит изображения для калибровки
    private Map<Tab, XYChart.Series<Number, Number>> spectralDataSeries = new HashMap<>(); // Хранит данные для графика и таблицы
    private Map<Tab, List<XYChart.Data<Number, Number>>> detectedPeaks = new HashMap<>(); // Хранит обнаруженные пики

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
        tabManager = new TabManager(tabPane);
        dataPreprocessing = new DataPreprocessing();
        spectraAnalysis = new SpectraAnalysis();
        System.out.println("The program is started, ready to work");

        // Создаем первую вкладку при запуске приложения
        handleNewTab();

        // Инициализируем обработчик изображений
        imageProcessor = new ImageProcessor(HelloController.this);
    }

    /**
     * Создает новую вкладку с заданным названием.
     */
    @FXML
    public void handleNewTab() {
        tabManager.createNewTab("Tab " + (tabPane.getTabs().size() + 1));
    }

    /**
     * Обрабатывает импорт рентгеновского изображения.
     * Открывает диалог выбора файлов и добавляет выбранные изображения на текущую вкладку.
     */
    @FXML
    public void handleImportXRayImage() {
        // Импортируем изображения
        List<Image> importedImages = fileImporter.importData(mainContainer.getScene().getWindow());

        // Получаем текущую вкладку
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

        // Добавляем импортированные изображения в хранилище для текущей вкладки
        xRayImages.put(currentTab, importedImages);

        // Обновляем отображение изображений на вкладке
        imageProcessor.putImagesOnTabPane(xRayImages, currentTab);
    }

    /**
     * Обрабатывает сглаживание изображения.
     * Применяет фильтр усреднения (Box Blur) к выбранному изображению.
     */
    public void handleImageSmoothing(ActionEvent event) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        Image selectedImage = imageProcessor.selectedImage;

        // Проверяем, выбрано ли изображение
        if (selectedImage == null) {
            System.out.println("No image selected for smoothing.");
            return;
        }

        // Сглаживаем изображение
        imageProcessor.selectedImage = dataPreprocessing.imageSmoothing(selectedImage, kernelSize);

        // Обновляем изображение в хранилище и на вкладке
        List<Image> currentImages = xRayImages.getOrDefault(currentTab, new ArrayList<>());
        int selectedIndex = currentImages.indexOf(selectedImage);
        if (selectedIndex != -1) {
            currentImages.set(selectedIndex, imageProcessor.selectedImage);
            imageProcessor.getImageView.setImage(imageProcessor.selectedImage);
        } else {
            System.out.println("Selected image not found in the list.");
            return;
        }
        xRayImages.put(currentTab, currentImages);
        imageProcessor.putImagesOnTabPane(xRayImages, currentTab);
    }

    /**
     * Обрабатывает визуализацию спектра.
     * Извлекает данные из выбранного изображения и строит график спектра.
     */
    public void spectraVisualization(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        Image selectedImage = imageProcessor.selectedImage;

        // Проверяем, выбрано ли изображение
        if (selectedImage == null) {
            System.out.println("Image is not selected");
            return;
        }

        // Извлекаем данные спектра из изображения
        XYChart.Series<Number, Number> series = spectraAnalysis.updateChartWithSplineData(currentTab, selectedImage);
        if (series == null) {
            System.out.println("Unable to generate spectral data for the selected Image");
            return;
        }

        // Сохраняем данные спектра
        spectralDataSeries.put(currentTab, series);

        // Обновляем таблицу данных спектра
        List<XYChart.Data<Number, Number>> seriesData = new ArrayList<>(series.getData());
        SpectralDataTable.updateTableViewInTab(currentTab, seriesData);
    }

    /**
     * Обрабатывает анализ пиков в спектре.
     * Обнаруживает и визуализирует пики в спектре на основе заданных параметров.
     */
    public void peakAnalysis(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        XYChart.Series<Number, Number> series = spectralDataSeries.get(currentTab);

        // Обнаруживаем и визуализируем пики
        detectedPeaks.put(currentTab, spectraAnalysis.visualizePeaks(currentTab, series, threshold, windowSize, minPeakDistance));
        System.out.println(detectedPeaks.get(currentTab));
    }

    /**
     * Обрабатывает калибровку спектрометра.
     * Отображает контекстное меню для выбора метода калибровки.
     */
    public void spectrumCalibration(ActionEvent actionEvent) {
        ContextMenu contextMenu = SpectrometerCalibration.createInstrumentCalibrationMenu();
        contextMenu.show(spectrumCalibration, Side.BOTTOM, 0,0);

        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

        //  List<Image> selectedImages = fileImporter.importData(mainContainer.getScene().getWindow());//if (!selectedImages.isEmpty()) {
        //    calibrationImages.put(currentTab, selectedImages.get(0));
        //     System.out.println("Calibration image updated for the tab: " + currentTab.getText());
        // }
        //XYChart.Series<Number, Number> series = spectraAnalysis.updateChaЦrtWithSplineData(currentTab,
        //        dataPreprocessing.preprocessImage(selectedImages, kernelSize).get(0));
        //List<XYChart.Data<Number, Number>> peaks = spectraAnalysis.visualizePeaks(currentTab, series, threshold, windowSize, minPeakDistance);

        double[] knownPositions;
        double[] knownEnergies;

       // System.out.println(peaks);
    }
}




