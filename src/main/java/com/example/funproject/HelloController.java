package com.example.funproject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    private Map<Tab, List<Image>> xRayImages = new HashMap<>();

    @FXML
    public void initialize() {
        imageProcessor = new ImageProcessor();
        tabManager = new TabManager(tabPane, imageProcessor);
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

    }
}























