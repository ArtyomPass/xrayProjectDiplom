package com.example.funproject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloController {

    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox sidebar;
    @FXML
    private TabPane tabPane;


    private final FileImporter fileImporter = new FileImporter();
    private ImageProcessor imageProcessor;
    private TabManager tabManager;
    private DataPreprocessing dataPreprocessing;
    private SpectraAnalysis spectraAnalysis;

    private Map<Tab, List<Image>> xRayImages = new HashMap<>();
    private Map<Tab, XYChart.Series<Number, Number>> spectralDataSeries = new HashMap<>();


    @FXML
    public void initialize() {
        imageProcessor = new ImageProcessor();
        tabManager = new TabManager(tabPane);
        dataPreprocessing = new DataPreprocessing();
        spectraAnalysis = new SpectraAnalysis();
        System.out.println("The program is started, ready to work");
    }

    @FXML
    public void handleImportXRayImage() {
        xRayImages.put(tabPane.getSelectionModel().getSelectedItem(), fileImporter.importData(mainContainer.getScene().getWindow()));
        System.out.println(xRayImages);
        imageProcessor.putImagesOnTabPane(xRayImages, tabPane);
    }

    @FXML
    public void handleNewTab() {
        tabManager.createNewTab("Tab " + (tabPane.getTabs().size() + 1));
    }

    public void handleDataPreprocessing(ActionEvent event) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        List<Image> originalImages = xRayImages.get(currentTab);
        xRayImages.put(currentTab, dataPreprocessing.preprocessImage(originalImages));
        imageProcessor.putImagesOnTabPane(xRayImages, tabPane);
    }

    public void spectraVisualization(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        List<Image> images = xRayImages.get(currentTab);
        spectralDataSeries.put(currentTab, spectraAnalysis.updateChartWithPeaks(currentTab, images.get(0)));
    }
}




