package com.example.funproject;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ImageProcessor {

    private double initialMouseX;
    private double initialMouseY;
    private VBox sidebar;

    public ImageProcessor(VBox sidebar) {
        this.sidebar = sidebar;
    }

    public void makeDraggable(ImageView imageView) {
        imageView.setOnMousePressed(this::onMousePressed);
        imageView.setOnMouseDragged(this::onMouseDragged);
    }

    private void onMousePressed(MouseEvent event) {
        // Record initial mouse positions
        initialMouseX = event.getSceneX();
        initialMouseY = event.getSceneY();
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
