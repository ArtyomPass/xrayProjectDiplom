package com.example.funproject;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SidebarController {

    private VBox sidebar;

    private Timeline spacingTimeline = new Timeline();
    private Timeline heightTimeline = new Timeline();

    public void setSidebar(VBox sidebar) {
        this.sidebar = sidebar;
    }

    public void adjustSidebarHeight(double height) {
        double targetSpacing = (height < 400) ? 5 : 10;
        animateSpacingChange(targetSpacing);

        for (Node node : sidebar.getChildren()) {
            if (node instanceof Button) {
                double targetHeight = (height < 400) ? 20 : 40;
                animateButtonHeightChange((Button) node, targetHeight);
            }
        }
    }

    private void animateSpacingChange(double newSpacing) {
        spacingTimeline.stop(); // Stop previous animation
        KeyValue keyValue = new KeyValue(sidebar.spacingProperty(), newSpacing, Interpolator.EASE_BOTH);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200), keyValue); // Shorter duration
        spacingTimeline.getKeyFrames().setAll(keyFrame);
        spacingTimeline.play();
    }

    private void animateButtonHeightChange(Button button, double newHeight) {
        heightTimeline.stop(); // Stop previous animation
        KeyValue keyValue = new KeyValue(button.prefHeightProperty(), newHeight, Interpolator.EASE_BOTH);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200), keyValue); // Shorter duration
        heightTimeline.getKeyFrames().setAll(keyFrame);
        heightTimeline.play();
    }
}
