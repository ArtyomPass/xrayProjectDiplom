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

    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox sidebar;
    @FXML
    private TabPane tabPane;
    @FXML
    private Button spectrumCalibration;


    private final FileImporter fileImporter = new FileImporter();
    private ImageProcessor imageProcessor;
    private TabManager tabManager;
    private DataPreprocessing dataPreprocessing;
    private SpectraAnalysis spectraAnalysis;

    protected Map<Tab, List<Image>> xRayImages = new HashMap<>(); // Keeps all images for spectrum
    private Map<Tab, Image> calibrationImages = new HashMap<>(); // keeps images for calibration
    private Map<Tab, XYChart.Series<Number, Number>> spectralDataSeries = new HashMap<>(); //keeps series data for chart and table
    private Map<Tab, List<XYChart.Data<Number, Number>>> detectedPeaks = new HashMap<>();


    private int windowSize = 20;
    private double minPeakDistance = 6.0;
    private double threshold = 20.1;
    private int kernelSize = 9;

    @FXML
    public void initialize() {

        tabManager = new TabManager(tabPane);
        dataPreprocessing = new DataPreprocessing();
        spectraAnalysis = new SpectraAnalysis();
        System.out.println("The program is started, ready to work");
        handleNewTab();
        imageProcessor = new ImageProcessor(HelloController.this);
    }

    @FXML
    public void handleNewTab() {
        tabManager.createNewTab("Tab " + (tabPane.getTabs().size() + 1));
    }

    @FXML
    public void handleImportXRayImage() {
        xRayImages.put(tabPane.getSelectionModel().getSelectedItem(), fileImporter.importData(mainContainer.getScene().getWindow()));
        System.out.println(xRayImages);
        imageProcessor.putImagesOnTabPane(xRayImages, tabPane.getSelectionModel().getSelectedItem());
    }

    public void handleImageSmoothing(ActionEvent event) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        Image selectedImage = imageProcessor.selectedImage;
        if (selectedImage == null) {
            System.out.println("No image selected for smoothing.");
            return;
        }
        // The id of selectedImage is changing every time when smooth, the tracking and rewriting id as required
        imageProcessor.selectedImage = dataPreprocessing.imageSmoothing(selectedImage, kernelSize);
        List<Image> currentImages = xRayImages.getOrDefault(currentTab, new ArrayList<>());
        System.out.println(currentImages);
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

    public void spectraVisualization(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        Image selectedImage = imageProcessor.selectedImage;
        if (selectedImage == null) {
            System.out.println("Image is not selected");
            return;
        }
        XYChart.Series<Number, Number> series = spectraAnalysis.updateChartWithSplineData(currentTab, selectedImage);
        if (series == null) {
            System.out.println("Unable to generate spectral data for the selected Image");
            return;
        }
        spectralDataSeries.put(currentTab, series);
        List<XYChart.Data<Number, Number>> seriesData = new ArrayList<>(series.getData());
        SpectralDataTable.updateTableViewInTab(currentTab, seriesData);
    }

    public void peakAnalysis(ActionEvent actionEvent) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        XYChart.Series<Number, Number> series = spectralDataSeries.get(currentTab);
        detectedPeaks.put(currentTab, spectraAnalysis.visualizePeaks(currentTab, series, threshold, windowSize, minPeakDistance));
        System.out.println(detectedPeaks.get(currentTab));
    }

    public void spectrumCalibration(ActionEvent actionEvent) {
        ContextMenu contextMenu = SpectrometerCalibration.createInstrumentCalibrationMenu();
        contextMenu.show(spectrumCalibration, Side.BOTTOM, 0,0);

        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();

        //  List<Image> selectedImages = fileImporter.importData(mainContainer.getScene().getWindow());//if (!selectedImages.isEmpty()) {
        //    calibrationImages.put(currentTab, selectedImages.get(0));
        //     System.out.println("Calibration image updated for the tab: " + currentTab.getText());
        // }
        //XYChart.Series<Number, Number> series = spectraAnalysis.updateChartWithSplineData(currentTab,
        //        dataPreprocessing.preprocessImage(selectedImages, kernelSize).get(0));
        //List<XYChart.Data<Number, Number>> peaks = spectraAnalysis.visualizePeaks(currentTab, series, threshold, windowSize, minPeakDistance);

        double[] knownPositions;
        double[] knownEnergies;

       // System.out.println(peaks);
    }
}




