package com.example.funproject;

import com.example.funproject.imageutils.LineInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
    private TabPane tabPane;
    @FXML
    private Button spectrumCalibration;

    // Вспомогательные классы для работы с данными и изображениями
    private final FileImporter fileImporter = new FileImporter();
    private TabManager tabManager;
    private DataPreprocessing dataPreprocessing;
    private SpectraAnalysis spectraAnalysis;

    // Хранилища данных для различных типов информации
    public Map<Tab, List<Image>> xRayImages = new HashMap<>(); // Хранит все изображения для спектра
    private Map<Tab, XYChart.Series<Number, Number>> spectralDataSeries = new HashMap<>(); // Хранит данные для графика и таблицы
    private Map<Tab, List<XYChart.Data<Number, Number>>> detectedPeaks = new HashMap<>(); // Хранит обнаруженные пики
    private Map<Tab, ImageProcessor> imageProcessors = new HashMap<>();
    protected Map<Image, List<LineInfo>> imageLines;

    public Map<Image, List<LineInfo>> getImageLines() {
        return imageLines;
    }

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
        imageLines = new HashMap<>();

        handleNewTab();
    }

    /**
     * Создает новую вкладку с заданным названием.
     */
    @FXML
    public void handleNewTab() {
        tabManager.createNewTab("Tab " + (tabPane.getTabs().size() + 1));
        imageProcessors.put(tabPane.getSelectionModel().getSelectedItem(), new ImageProcessor(this));
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
        imageProcessors.get(currentTab).putImagesAndButtonsOnTabPane(xRayImages, currentTab);
    }

    /**
     * Обрабатывает сглаживание изображения.
     * Применяет фильтр усреднения (Box Blur) к выбранному изображению.
     */
    public void handleImageSmoothing(ActionEvent event) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        Image selectedImage = imageProcessors.get(currentTab).selectedImage;

        // Проверяем, выбрано ли изображение
        if (selectedImage == null) {
            System.out.println("No image selected for smoothing.");
            return;
        }

        // Сглаживаем изображение
        imageProcessors.get(currentTab).selectedImage = dataPreprocessing.imageSmoothing(selectedImage, kernelSize);

        // Обновляем изображение в хранилище и на вкладке
        List<Image> currentImages = xRayImages.getOrDefault(currentTab, new ArrayList<>());
        int selectedIndex = currentImages.indexOf(selectedImage);
        if (selectedIndex != -1) {
            currentImages.set(selectedIndex, imageProcessors.get(currentTab).selectedImage);
            imageProcessors.get(currentTab).imageView.setImage(imageProcessors.get(currentTab).selectedImage);
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
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

        // Справочные энергии для пиков Mn
        Map<String, Double> knownEnergiesMn = new HashMap<>();
        knownEnergiesMn.put("K-Alpha 1", 5898.0);
        knownEnergiesMn.put("K-Alpha 2", 5887.0);
        knownEnergiesMn.put("K-Beta 1", 6490.0);

        // Списки для хранения данных калибровки
        List<Double> knownPositions = new ArrayList<>();
        List<Double> knownEnergies = new ArrayList<>();

        // Поиск пиков Mn среди выделенных пиков
        for (Image image : imageLines.keySet()) {
            List<LineInfo> lines = imageLines.get(image);
            for (LineInfo line : lines) {
                if (line.getElementName().equals("Mn") && knownEnergiesMn.containsKey(line.getPeakType())) {
                    knownPositions.add(line.getXPosition());
                    knownEnergies.add(knownEnergiesMn.get(line.getPeakType()));
                }
            }
        }

        // Проверка, достаточно ли данных для калибровки
        if (knownPositions.size() < 2) {
            System.out.println("Недостаточно данных для калибровки. Нужно как минимум два пика.");
            return;
        }

        // Преобразование списков в массивы
        double[] xValues = knownPositions.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yValues = knownEnergies.stream().mapToDouble(Double::doubleValue).toArray();
        System.out.println("xValues: " + Arrays.toString(xValues) + "; yValues: " + Arrays.toString(yValues));

        // Вычисление параметров калибровочной кривой (линейная регрессия)
        double[] calibrationParams = SpectrometerCalibration.linearRegression(xValues, yValues);

        // Apply calibration and create a new series for the calibrated spectrum
        XYChart.Series<Number, Number> series = spectralDataSeries.get(currentTab);
        if (series != null) {
            List<XYChart.Data<Number, Number>> data = series.getData();
            double[] positions = data.stream().mapToDouble(d -> d.getXValue().doubleValue()).toArray();
            double[] calibratedEnergies = SpectrometerCalibration.applyCalibrationCurve(positions, calibrationParams);

            XYChart.Series<Number, Number> calibratedSeries = new XYChart.Series<>();
            calibratedSeries.setName("Calibrated Spectrum");
            for (int i = 0; i < data.size(); i++) {
                calibratedSeries.getData().add(new XYChart.Data<>(calibratedEnergies[i], data.get(i).getYValue()));
            }

            // Update the chart with the new axis bounds and calibrated series
            LineChart<Number, Number> chart = spectraAnalysis.getLineChartFromTab(currentTab);
            if (chart != null && !calibratedSeries.getData().isEmpty()) {
                NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                xAxis.setAutoRanging(false);
                xAxis.setLowerBound(calibratedSeries.getData().get(0).getXValue().doubleValue());
                xAxis.setUpperBound(calibratedSeries.getData().get(calibratedSeries.getData().size() - 1).getXValue().doubleValue());

                chart.getData().clear();
                chart.getData().add(calibratedSeries);
            } else {
                System.out.println("Chart is null or calibrated series is empty.");
            }
        } else {
            System.out.println("Series is null or empty, cannot calibrate spectrum.");
        }
    }
}