package com.example.funproject;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

public class ImageProcessor {

    private double initialMouseX;
    private double initialMouseY;

    public ImageProcessor() {
        System.out.println("Image processor is initialized");
    }

    private void makeDraggable(ImageView imageView) {
        imageView.setOnMousePressed(this::onMousePressed);
        imageView.setOnMouseDragged(this::onMouseDragged);
    }

    public void putImagesOnTabPane(Map<Tab, List<Image>> images, TabPane tabPane) {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null && currentTab.getContent() instanceof SplitPane) {
            SplitPane splitPane = (SplitPane) currentTab.getContent();
            if (splitPane.getItems().size() > 1 && splitPane.getItems().get(1) instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) splitPane.getItems().get(1);
                if (scrollPane.getContent() instanceof ImageView) {
                    ImageView imageView = (ImageView) scrollPane.getContent();
                    makeDraggable(imageView);

                    images.get(currentTab).forEach(imageView::setImage); // Assuming one image for simplicity
                }
            }
        }
    }

    private void onMousePressed(MouseEvent event) {
        // Record initial mouse positions
        initialMouseX = event.getSceneX();
        initialMouseY = event.getSceneY();

        ImageView imageView = (ImageView) event.getSource();
    }

    private void onMouseDragged(MouseEvent event) {
        ImageView imageView = (ImageView) event.getSource();

        // Calculate new position
        double deltaX = event.getSceneX() - initialMouseX;
        double deltaY = event.getSceneY() - initialMouseY;

        // Update the image's position
        imageView.setTranslateX(imageView.getTranslateX() + deltaX);
        imageView.setTranslateY(imageView.getTranslateY() + deltaY);

        // Update initial positions for next calculation
        initialMouseX = event.getSceneX();
        initialMouseY = event.getSceneY();
    }
}
