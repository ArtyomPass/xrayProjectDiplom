package com.example.funproject;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML private BorderPane mainContainer;
    @FXML private VBox sidebar;
    @FXML private ImageView imageView;
    @FXML private TabPane tabPane;

    private final SidebarController sidebarController = new SidebarController();
    private final FileImporter fileImporter = new FileImporter();
    private ImageProcessor imageProcessor;
    private TabManager tabManager;
    private final List<Image> xRayImages = new ArrayList<>();

    @FXML
    public void initialize() {
        // Configure sidebar
        sidebarController.setSidebar(sidebar);
        mainContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                sidebarController.adjustSidebarHeight(newVal.doubleValue()));

        // Setup draggable images
        imageProcessor = new ImageProcessor(sidebar);
        imageProcessor.makeDraggable(imageView);

        // Setup tab management
        tabManager = new TabManager(tabPane);
    }

    @FXML
    private void handleImportXRayImage() {
        // Get new images from the file importer
        List<Image> newImages = fileImporter.importData(mainContainer.getScene().getWindow());
        xRayImages.addAll(newImages);

        if (!xRayImages.isEmpty()) {
            // Get the currently selected tab
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
            if (currentTab != null) {
                // Find the ImageView in the current tab
                StackPane contentArea = (StackPane) currentTab.getContent();
                for (Node node : contentArea.getChildren()) {
                    if (node instanceof ImageView) {
                        // Set the image to the found ImageView
                        ((ImageView) node).setImage(xRayImages.get(xRayImages.size() - 1));
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void handleNewTab() {
        tabManager.createNewTab("New Tab " + (tabPane.getTabs().size() + 1));
    }
}
