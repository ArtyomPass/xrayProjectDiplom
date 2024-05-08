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

import java.io.File;
import java.util.*;

public class HelloController {

    // Элементы интерфейса из FXML-файла
    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox sidebar;
    @FXML
    protected TabPane tabPane;


    // Вспомогательные классы
    private final FileImporter fileImporter = new FileImporter();
    protected TabManager tabManager;
    private DataPreprocessing dataPreprocessing;
    protected SpectralDataVisualization spectralDataVisualization;

    // Хранилища данных
    protected Map<Tab, List<Image>> xRayImages = new HashMap<>();
    private Map<Tab, ImageProcessor> imageProcessors = new HashMap<>();
    protected Map<Image, List<LineInfo>> imageLines;
    protected Map<Tab, List<LineInfo>> chartLines;
    protected Map<Tab, TableView<SpectralDataTable.SpectralData>> spectralDataTableViews = new HashMap<>();

    // Параметры для сглаживания
    private int kernelSize = 9;

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        tabManager = new TabManager(tabPane, imageProcessors);
        dataPreprocessing = new DataPreprocessing();
        spectralDataVisualization = new SpectralDataVisualization();
        imageLines = new HashMap<>();
        chartLines = new HashMap<>();
        handleNewTab();
        System.out.println("Программа запущена и готова к работе.");
    }

    /**
     * Создание новой вкладки.
     */
    @FXML
    public void handleNewTab() {
        tabManager.createNewTab("Вкладка " + (tabPane.getTabs().size() + 1), this);
    }

    /**
     * Импорт рентгеновского изображения.
     */
    @FXML
    public void handleImportXRayImage() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        List<Image> importedImages = fileImporter.importImages(mainContainer.getScene().getWindow());
        xRayImages.put(currentTab, importedImages);
        imageProcessors.get(currentTab).putImagesAndButtonsOnTabPane(xRayImages, currentTab);
    }

    /**
     * Импорт данных из таблицы.
     *
     * @param actionEvent событие, вызвавшее метод
     */
    @FXML
    public void handleImportTable(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = spectralDataTableViews.get(currentTab);
        File selectedFile = fileImporter.importTable(mainContainer.getScene().getWindow());

        // Импортируем данные из таблицы и обновляем график и таблицу
        spectralDataVisualization.importTableData(tableViewToUpdate, selectedFile);
    }

    /**
     * Сглаживание изображения.
     *
     * @param event событие, вызвавшее метод
     */
    public void handleImageSmoothing(ActionEvent event) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        Image selectedImage = imageProcessors.get(currentTab).selectedImage;
        if (selectedImage == null) {
            System.out.println("Изображение не выбрано.");
            return;
        }

        // Контекстное меню с опциями обработки
        ContextMenu processingMenu = new ContextMenu();
        MenuItem smoothItem = new MenuItem("Сгладить");
        MenuItem densityItem = new MenuItem("Денситометрия");
        processingMenu.getItems().addAll(smoothItem, densityItem);

        // Обработчик события сглаживания
        smoothItem.setOnAction(e -> {
            Image smoothedImage = dataPreprocessing.imageSmoothing(selectedImage, kernelSize);
            updateImageInTab(currentTab, selectedImage, smoothedImage);
            imageProcessors.get(currentTab).selectedImage = smoothedImage;
        });

        // Обработчик события денситометрии
        densityItem.setOnAction(e -> {
            WritableImage densityImage = dataPreprocessing.applyDensity(selectedImage);
            updateImageInTab(currentTab, selectedImage, densityImage);
        });

        // Отображение контекстного меню
        processingMenu.show((Node) event.getSource(), Side.BOTTOM, 0, 0);
    }


    //* Обновление изображения на вкладке.

    private void updateImageInTab(Tab currentTab, Image oldImage, Image newImage) {
        List<Image> currentImages = xRayImages.getOrDefault(currentTab, new ArrayList<>());
        int selectedIndex = currentImages.indexOf(oldImage);
        if (selectedIndex != -1) {
            currentImages.set(selectedIndex, newImage);
            imageProcessors.get(currentTab).imageView.setImage(newImage);
            xRayImages.put(currentTab, currentImages);
            imageProcessors.get(currentTab).putImagesAndButtonsOnTabPane(xRayImages, currentTab);
        } else {
            System.out.println("Выбранное изображение не найдено.");
        }
    }

    @FXML
    public void spectraVisualization(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null) return; // Проверка на null

        Image selectedImage = imageProcessors.get(currentTab).selectedImage;
        TabPane currentInnerTabPane = tabManager.innerTableAndChartTabPanes.get(currentTab);

        // Создание контекстного меню
        ContextMenu visualizationMenu = new ContextMenu();

        // Пункт меню для построения графика по изображению
        MenuItem imageBasedItem = new MenuItem("Построить график по изображению");
        imageBasedItem.setOnAction(e -> {
            XYChart.Series<Number, Number> series = spectralDataVisualization.updateChartWithSplineData(currentTab, selectedImage, currentInnerTabPane);
            if (series == null) {
                System.out.println("Не удалось получить данные спектра.");
                return;
            }
            // Обновляем таблицу спектральных данных
            SpectralDataTable.updateTableViewInTab(currentTab, new ArrayList<>(series.getData()), spectralDataTableViews.get(currentTab));
        });

        // Пункт меню для построения графика по таблице
        MenuItem tableBasedItem = new MenuItem("Построить график по таблице");
        tableBasedItem.setOnAction(e -> {
            // Получаем текущую внутреннюю вкладку и график
            Tab currentInnerTab = tabManager.innerTableAndChartTabPanes.get(currentTab).getSelectionModel().getSelectedItem();
            LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

            // Получаем данные из таблицы
            TableView<SpectralDataTable.SpectralData> tableView = spectralDataTableViews.get(currentTab);

            // Вызываем новый метод для визуализации
            spectralDataVisualization.visualizeFromTable(currentTab, currentChart, tableView);
        });

        // Добавляем пункты меню в контекстное меню
        visualizationMenu.getItems().addAll(imageBasedItem, tableBasedItem);

        // Отображение контекстного меню
        visualizationMenu.show((Node) actionEvent.getSource(), Side.BOTTOM, 0, 0);
    }

    /**
     * Калибровка спектрометра.
     *
     * @param actionEvent событие, вызвавшее метод
     */
    public void spectrumCalibration(ActionEvent actionEvent) {
        // Выбранная вкладка
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        // Текущая внутренняя вкладка и график
        Tab currentInnerTab = tabManager.innerTableAndChartTabPanes.get(selectedTab).getSelectionModel().getSelectedItem();
        LineChart<Number, Number> currentChart = (LineChart<Number, Number>) currentInnerTab.getContent();

        // TableView для обновления
        TableView<SpectralDataTable.SpectralData> tableViewToUpdate = spectralDataTableViews.get(selectedTab);

        // Линии с вкладок
        List<LineInfo> lineChartInfos = chartLines.get(currentInnerTab);
        List<LineInfo> lineImageInfos = imageLines.get(imageProcessors.get(selectedTab).selectedImage);

        // Открытие диалогового окна калибровки
        new CalibrationDialog(selectedTab, lineImageInfos, lineChartInfos, currentChart, tableViewToUpdate);
    }
}