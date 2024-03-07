package com.example.funproject;

import javafx.geometry.Orientation;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TabManager{

    private final TabPane tabPane;


    public TabManager(TabPane tabPane){
        this.tabPane = tabPane;

    }

    public void createNewTab(String title){
        Tab newTab = new Tab(title, createTabContent());
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    private SplitPane createTabContent(){
        TableView<?> dataTable = new TableView();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Channel");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Intensity");
        LineChart<Number, Number> spectrumChart = new LineChart<>(xAxis, yAxis);


        SplitPane dataChartSplitPane = new SplitPane();
        dataChartSplitPane.setOrientation(Orientation.HORIZONTAL);
        dataChartSplitPane.getItems().addAll(dataTable, spectrumChart);
        dataChartSplitPane.setDividerPositions(0.3);


        ImageView xrayImageView = new ImageView();
        xrayImageView.setPreserveRatio(true);
        xrayImageView.setSmooth(true);

        ScrollPane imageViewScrollPane = new ScrollPane(xrayImageView);
        imageViewScrollPane.setFitToWidth(true);
        imageViewScrollPane.setFitToHeight(true);

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(dataChartSplitPane, imageViewScrollPane);
        mainSplitPane.setDividerPositions(0.5);

        return mainSplitPane;

    }



}