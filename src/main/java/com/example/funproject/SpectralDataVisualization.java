package com.example.funproject;

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpectralDataVisualization {
    private Line line1 = new Line();
    private Line line2 = new Line();
    private double initialY;
    private Image selectedImage;
    public boolean isInVisualizationMode = false;

    private EventHandler<MouseEvent> visualizationMousePressedHandler;
    private EventHandler<MouseEvent> visualizationMouseDraggedHandler;
    private EventHandler<MouseEvent> visualizationMouseReleasedHandler;

    public SpectralDataVisualization() {
        // Empty constructor
    }

    /**
     * Updates the LineChart and TableView based on the spline data derived from the image.
     */
    public XYChart.Series<Number, Number> updateChartWithSplineData(Tab tab, Image image, TabPane innerTabPane, TableView<SpectralDataTable.SpectralData> tableView) {
        if (innerTabPane == null || image == null) return null;

        Tab currentTab = innerTabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null || !(currentTab.getContent() instanceof LineChart)) return null;

        LineChart<Number, Number> chart = (LineChart<Number, Number>) currentTab.getContent();

        // Process the image and get data for the spline
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Spectrum");

        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        List<SpectralDataTable.SpectralData> tableData = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            double totalIntensity = 0;
            for (int y = 0; y < height; y++) {
                Color color = pixelReader.getColor(x, y);
                double intensity = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                totalIntensity += intensity;
            }
            double averageIntensity = totalIntensity / height;
            series.getData().add(new XYChart.Data<>(x, averageIntensity * 100));
            tableData.add(new SpectralDataTable.SpectralData(x, averageIntensity * 100));
        }

        // Update the table with the new data
        updateTableWithSplineData(tableView, tableData);

        // Visualize data from the updated table
        visualizeFromTable(tab, chart, tableView);

        return series;
    }

    /**
     * Updates the TableView with the new spline data.
     */
    public void updateTableWithSplineData(TableView<SpectralDataTable.SpectralData> tableView, List<SpectralDataTable.SpectralData> tableData) {
        tableView.setItems(FXCollections.observableArrayList(tableData));
    }

    /**
     * Imports data from the table and visualizes it on the chart and table.
     */
    public void importTableData(TableView<SpectralDataTable.SpectralData> tableViewToUpdate, File selectedFile) {
        if (selectedFile != null) {
            List<SpectralDataTable.SpectralData> tableData = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        try {
                            double xValue = Double.parseDouble(parts[0]);
                            double yValue = Double.parseDouble(parts[1]);
                            tableData.add(new SpectralDataTable.SpectralData(xValue, yValue));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing data: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            tableViewToUpdate.setItems(FXCollections.observableArrayList(tableData));
            exitVisualizationMode((Pane) tableViewToUpdate.getParent());
        } else {
            // TODO: Handle the case when no file is selected
        }
    }

    /**
     * Visualizes data from the table on the chart.
     */
    public void visualizeFromTable(Tab currentTab, LineChart<Number, Number> currentChart, TableView<SpectralDataTable.SpectralData> tableView) {
        currentChart.getData().clear();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Intensities");

        for (SpectralDataTable.SpectralData data : tableView.getItems()) {
            series.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
        }

        currentChart.getData().add(series);
        currentChart.setCreateSymbols(false);
        currentChart.setLegendVisible(false);
    }

    /**
     * Sets cursor for ImageView and adds lines that follow the cursor.
     */
    public void setImageViewCursorAndLines(ImageView imageView, Tab currentTab, TabPane innerTabPane, TableView<SpectralDataTable.SpectralData> tableView) {
        visualizationMousePressedHandler = event -> {
            Pane parentPane = (Pane) imageView.getParent();
            initialY = event.getY();

            addHorizontalLine(initialY, imageView, parentPane, line1);
            addHorizontalLine(initialY, imageView, parentPane, line2);
        };

        visualizationMouseDraggedHandler = event -> {
            double currentY = event.getY();

            line1.startYProperty().unbind();
            line1.endYProperty().unbind();
            line1.setStartY(currentY);
            line1.setEndY(currentY);

            double oppositeY = 2 * initialY - currentY;
            line2.startYProperty().unbind();
            line2.endYProperty().unbind();
            line2.setStartY(oppositeY);
            line2.setEndY(oppositeY);

            addHorizontalLine(line1.getStartY(), imageView, (Pane) imageView.getParent(), line1);
            addHorizontalLine(line2.getStartY(), imageView, (Pane) imageView.getParent(), line2);
        };

        visualizationMouseReleasedHandler = event -> {
            Pane parentPane = (Pane) imageView.getParent();
            selectedImage = getSelectedRegionImage(imageView);
            if (selectedImage != null) {
                updateChartWithSplineData(currentTab, selectedImage, innerTabPane, tableView);
            }
            exitVisualizationMode(parentPane);
        };

        imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, visualizationMousePressedHandler);
        imageView.addEventHandler(MouseEvent.MOUSE_DRAGGED, visualizationMouseDraggedHandler);
        imageView.addEventHandler(MouseEvent.MOUSE_RELEASED, visualizationMouseReleasedHandler);

        isInVisualizationMode = true;

        imageView.setOnMouseEntered(event -> imageView.setCursor(Cursor.CROSSHAIR));
        imageView.setOnMouseExited(event -> imageView.setCursor(Cursor.DEFAULT));
    }

    /**
     * Adds a horizontal line.
     */
    private void addHorizontalLine(double yClick, ImageView imageView, Pane parentPane, Line line) {
        line.setStroke(Color.RED);
        line.setStrokeWidth(2);

        final double finalYPosition = Math.max(0, Math.min(yClick, imageView.getImage().getHeight()));

        line.startYProperty().bind(Bindings.createDoubleBinding(() -> {
            double minY = imageView.getBoundsInParent().getMinY();
            return minY + finalYPosition * imageView.getScaleY();
        }, imageView.boundsInParentProperty(), imageView.scaleYProperty()));

        line.endYProperty().bind(line.startYProperty());
        line.startXProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMinX(), imageView.boundsInParentProperty()));
        line.endXProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMaxX(), imageView.boundsInParentProperty()));

        if (!parentPane.getChildren().contains(line)) {
            parentPane.getChildren().add(line);
        }
    }

    /**
     * Gets the selected region of the image.
     */
    private Image getSelectedRegionImage(ImageView imageView) {
        double y1 = line1.getStartY();
        double y2 = line2.getStartY();
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);

        Image image = imageView.getImage();
        if (image != null && image.getPixelReader() != null) {
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            double scaleY = imageView.getBoundsInParent().getHeight() / height;

            int startY = (int) (minY / scaleY);
            int endY = (int) (maxY / scaleY);

            if (startY < 0) startY = 0;
            if (endY > height) endY = height;

            WritableImage selectedImage = new WritableImage(width, endY - startY);
            for (int x = 0; x < width; x++) {
                for (int y = startY; y < endY; y++) {
                    selectedImage.getPixelWriter().setColor(x, y - startY, pixelReader.getColor(x, y));
                }
            }
            return selectedImage;
        }
        return null;
    }

    /**
     * Exits the visualization mode, resetting the cursor and removing lines.
     */
    public void exitVisualizationMode(Pane parentPane) {
        parentPane.getChildren().remove(line1);
        parentPane.getChildren().remove(line2);

        if (isInVisualizationMode) {
            parentPane.getChildren().filtered(node -> node instanceof ImageView).forEach(node -> {
                ImageView imageView = (ImageView) node;

                imageView.removeEventHandler(MouseEvent.MOUSE_PRESSED, visualizationMousePressedHandler);
                imageView.removeEventHandler(MouseEvent.MOUSE_DRAGGED, visualizationMouseDraggedHandler);
                imageView.removeEventHandler(MouseEvent.MOUSE_RELEASED, visualizationMouseReleasedHandler);

                imageView.setOnMouseEntered(null);
                imageView.setOnMouseExited(null);

                imageView.setCursor(Cursor.DEFAULT);
            });
            isInVisualizationMode = false;
        }
    }

    /**
     * Enters the visualization mode by setting the cursor and adding lines.
     */
    public void enterVisualizationMode(ImageView imageView, Tab currentTab, TabPane innerTabPane, TableView<SpectralDataTable.SpectralData> tableView) {
        setImageViewCursorAndLines(imageView, currentTab, innerTabPane, tableView);
    }
}
