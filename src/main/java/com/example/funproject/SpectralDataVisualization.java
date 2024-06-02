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
    private final Line line1 = new Line();
    private final Line line2 = new Line();
    private double initialY;
    private Image selectedImage;
    public boolean isInVisualizationMode = false;

    private EventHandler<MouseEvent> visualizationMousePressedHandler;
    private EventHandler<MouseEvent> visualizationMouseDraggedHandler;
    private EventHandler<MouseEvent> visualizationMouseReleasedHandler;

    public SpectralDataVisualization() {
        // Пустой конструктор
    }

    /**
     * Обновляет LineChart и TableView на основе данных, полученных из изображения.
     */
    public XYChart.Series<Number, Number> updateChartWithSplineData(Tab tab, Image image, TabPane innerTabPane, TableView<SpectralDataTable.SpectralData> tableView) {
        if (innerTabPane == null || image == null) return null;

        Tab currentTab = innerTabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null || !(currentTab.getContent() instanceof LineChart)) return null;

        LineChart<Number, Number> chart = (LineChart<Number, Number>) currentTab.getContent();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Spectrum");

        List<SpectralDataTable.SpectralData> tableData = new ArrayList<>();
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        for (int x = 0; x < width; x++) {
            double totalIntensity = 0;
            for (int y = 0; y < height; y++) {
                Color color = pixelReader.getColor(x, y);
                totalIntensity += (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
            }
            double averageIntensity = totalIntensity / height;
            series.getData().add(new XYChart.Data<>(x, averageIntensity * 100));
            tableData.add(new SpectralDataTable.SpectralData(x, averageIntensity * 100));
        }

        updateTableWithSplineData(tableView, tableData);
        visualizeFromTable(tab, chart, tableView);

        return series;
    }

    /**
     * Обновляет TableView новыми данными.
     */
    public void updateTableWithSplineData(TableView<SpectralDataTable.SpectralData> tableView, List<SpectralDataTable.SpectralData> tableData) {
        tableView.setItems(FXCollections.observableArrayList(tableData));
    }

    /**
     * Отображает данные из TableView на графике.
     */
    public void visualizeFromTable(Tab currentTab, LineChart<Number, Number> currentChart, TableView<SpectralDataTable.SpectralData> tableView) {
        currentChart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Спектр");

        for (SpectralDataTable.SpectralData data : tableView.getItems()) {
            series.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
        }

        currentChart.getData().add(series);
        currentChart.setCreateSymbols(false);
        currentChart.setLegendVisible(false);
    }

    /**
     * Настраивает курсор для ImageView и добавляет линии, следящие за курсором.
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
            updateLinePosition(line1, currentY, imageView);
            updateLinePosition(line2, 2 * initialY - currentY, imageView);
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
     * Добавляет горизонтальную линию.
     */
    private void addHorizontalLine(double yClick, ImageView imageView, Pane parentPane, Line line) {
        line.setStroke(Color.RED);
        line.setStrokeWidth(2);

        final double finalYPosition = Math.max(0, Math.min(yClick, imageView.getImage().getHeight()));

        line.startYProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMinY() + finalYPosition * imageView.getScaleY(),
                imageView.boundsInParentProperty(), imageView.scaleYProperty()));

        line.endYProperty().bind(line.startYProperty());
        line.startXProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMinX(), imageView.boundsInParentProperty()));
        line.endXProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMaxX(), imageView.boundsInParentProperty()));

        if (!parentPane.getChildren().contains(line)) {
            parentPane.getChildren().add(line);
        }
    }

    /**
     * Обновляет позицию линии.
     */
    private void updateLinePosition(Line line, double yPosition, ImageView imageView) {
        line.startYProperty().unbind();
        line.endYProperty().unbind();
        line.setStartY(yPosition);
        line.setEndY(yPosition);
        addHorizontalLine(yPosition, imageView, (Pane) imageView.getParent(), line);
    }

    /**
     * Получает выбранную область изображения.
     */
    private Image getSelectedRegionImage(ImageView imageView) {
        double minY = Math.min(line1.getStartY(), line2.getStartY());
        double maxY = Math.max(line1.getStartY(), line2.getStartY());

        Image image = imageView.getImage();
        if (image == null || image.getPixelReader() == null) return null;

        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        double scaleY = imageView.getBoundsInParent().getHeight() / height;

        int startY = (int) (minY / scaleY);
        int endY = (int) (maxY / scaleY);

        startY = Math.max(0, startY);
        endY = Math.min(height, endY);

        WritableImage selectedImage = new WritableImage(width, endY - startY);
        for (int x = 0; x < width; x++) {
            for (int y = startY; y < endY; y++) {
                selectedImage.getPixelWriter().setColor(x, y - startY, pixelReader.getColor(x, y));
            }
        }
        return selectedImage;
    }

    /**
     * Выход из режима визуализации.
     */
    public void exitVisualizationMode(Pane parentPane) {
        parentPane.getChildren().removeAll(line1, line2);

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
     * Вход в режим визуализации.
     */
    public void enterVisualizationMode(ImageView imageView, Tab currentTab, TabPane innerTabPane, TableView<SpectralDataTable.SpectralData> tableView) {
        setImageViewCursorAndLines(imageView, currentTab, innerTabPane, tableView);
    }
}
