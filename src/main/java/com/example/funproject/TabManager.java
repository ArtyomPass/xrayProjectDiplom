package com.example.funproject;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class TabManager {

    private final TabPane tabPane;

    public TabManager(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public void createNewTab(String title) {
        Tab newTab = new Tab(title, createTabContent());
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    private StackPane createTabContent() {
        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-border-color: blue; -fx-border-width: 10px;");

        // Add an ImageView to the tab content
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        contentArea.getChildren().add(imageView);

        return contentArea;
    }
}
