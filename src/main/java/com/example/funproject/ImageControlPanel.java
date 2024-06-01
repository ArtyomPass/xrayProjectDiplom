package com.example.funproject;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageControlPanel extends HBox {

    private final ImageProcessor imageProcessor;
    private final TabPane innerTabPane;
    private final Map<Image, List<LineInfo>> imageLines;
    private final Map<Tab, List<LineInfo>> chartLines;
    private final ComboBox<String> lineTypeComboBox;
    private final TextField elementInput;
    private final Button pickPeaksButton;
    private final Button resetButton;
    private final TextField angleInput;
    private boolean peakSelectionMode = false;

    public ImageControlPanel(HelloController controller, ImageProcessor imageProcessor, ImageView mainImageView, TabPane innerTabPane) {
        this.imageProcessor = imageProcessor;
        this.imageLines = controller.imageLines;
        this.chartLines = controller.chartLines;
        this.innerTabPane = innerTabPane;

        lineTypeComboBox = new ComboBox<>();
        elementInput = new TextField();
        pickPeaksButton = new Button("Отметить Пики");
        resetButton = new Button("Восстановить Изображение");
        angleInput = new TextField();

        lineTypeComboBox.getItems().addAll("X1", "X2");
        lineTypeComboBox.setPromptText("Стандарт");
        elementInput.setPromptText("Название элемента");
        angleInput.setPromptText("Угол");

        lineTypeComboBox.getStyleClass().add("combo-box-line-type");
        elementInput.getStyleClass().add("text-field-element");
        pickPeaksButton.getStyleClass().add("button-pick-peaks");
        resetButton.getStyleClass().add("button-reset");
        angleInput.getStyleClass().add("text-field-angle");

        resetButton.setOnAction(event -> handleResetButtonClick(mainImageView));
        pickPeaksButton.setOnAction(event -> togglePeakSelectionModeAndHandleClicks());

        lineTypeComboBox.setOnAction(event -> loadAngleFromNotebook());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(pickPeaksButton, elementInput, lineTypeComboBox, angleInput, spacer, resetButton);

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(3, 10, 3, 10));

        elementInput.textProperty().addListener((observable, oldValue, newValue) -> loadAngleFromNotebook());
    }

    private void loadAngleFromNotebook() {
        if (elementInput.getText().isEmpty()) {
            angleInput.clear();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("notebook.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Элемент: " + elementInput.getText()) && line.contains("Линия: " + lineTypeComboBox.getValue())) {
                    String angle = line.split(", Угол: ")[1];
                    angleInput.setText(angle);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        angleInput.clear();
    }

    private void handleResetButtonClick(ImageView imageView) {
        Image selectedImage = imageProcessor.selectedImage;
        if (imageView != null && selectedImage != null) {
            imageView.setTranslateX(0);
            imageView.setTranslateY(0);
            imageView.setScaleX(1);
            imageView.setScaleY(1);
            imageProcessor.imageViewStates.put(selectedImage, new double[]{1, 1, 0, 0});
        }
    }

    private void togglePeakSelectionModeAndHandleClicks() {
        peakSelectionMode = !peakSelectionMode;
        updateButtonStyle();

        LineChart<Number, Number> lineChart = getActiveLineChart();
        ImageView imageView = imageProcessor.imageView;

        if (peakSelectionMode) {
            setupPeakSelectionMode(lineChart, imageView);
        } else {
            disablePeakSelectionMode(lineChart, imageView);
        }
    }

    private void setupPeakSelectionMode(LineChart<Number, Number> lineChart, ImageView imageView) {
        if (lineChart != null) {
            lineChart.setCursor(Cursor.CROSSHAIR);
            lineChart.setOnMouseClicked(event -> handleLineChartClick(event, lineChart));
        }

        if (imageView != null) {
            imageView.setCursor(Cursor.CROSSHAIR);
            imageView.setOnMouseClicked(event -> handleImageViewClick(event, imageView));
        }
    }

    private void disablePeakSelectionMode(LineChart<Number, Number> lineChart, ImageView imageView) {
        if (lineChart != null) {
            lineChart.setCursor(Cursor.DEFAULT);
            lineChart.setOnMouseClicked(null);
        }

        if (imageView != null) {
            imageView.setCursor(Cursor.DEFAULT);
            imageView.setOnMouseClicked(null);
        }
    }

    private void handleLineChartClick(MouseEvent event, LineChart<Number, Number> lineChart) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (lineChart.getData().isEmpty()) {
                showAlert("Нет данных на графике для добавления линии.");
                return;
            }

            if (elementInput.getText().isEmpty() || angleInput.getText().isEmpty() || lineTypeComboBox.getValue() == null) {
                showAlert("Пожалуйста, заполните все поля (элемент, угол, тип линии).");
                return;
            }

            double mouseXValue = lineChart.getXAxis().sceneToLocal(event.getSceneX(), 0).getX();
            if (!Double.isNaN(mouseXValue)) {
                Number xValue = lineChart.getXAxis().getValueForDisplay(mouseXValue);

                NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
                xAxis.setAutoRanging(false);
                NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                yAxis.setAutoRanging(false);

                XYChart.Series<Number, Number> verticalLineSeries = new XYChart.Series<>();
                verticalLineSeries.setName("Вертикальная линия");
                verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getLowerBound()));
                verticalLineSeries.getData().add(new XYChart.Data<>(xValue, yAxis.getUpperBound()));
                lineChart.getData().add(0, verticalLineSeries);

                verticalLineSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: blue; -fx-stroke-width: 2px;");
                verticalLineSeries.getData().forEach(data -> {
                    if (data.getNode() != null) data.getNode().setVisible(false);
                });

                Tab currentTab = innerTabPane.getSelectionModel().getSelectedItem();
                LineInfo lineInfo = new LineInfo(verticalLineSeries, null, xValue.doubleValue(), lineTypeComboBox.getValue(), elementInput.getText(), Double.parseDouble(angleInput.getText()));
                chartLines.computeIfAbsent(currentTab, k -> new ArrayList<>()).add(lineInfo);

                setupLineDragHandlers(verticalLineSeries, lineInfo, lineChart);
                saveAngleToNotebook(lineTypeComboBox.getValue(), angleInput.getText());
            }
        }
    }

    private void handleImageViewClick(MouseEvent event, ImageView imageView) {
        if (event.getButton() == MouseButton.PRIMARY) {
            addPeakLine(event.getX(), imageView);
        }
    }

    private void addPeakLine(double xClick, ImageView imageView) {
        Line peakLine = new Line();
        peakLine.setStroke(Color.BLUE);
        peakLine.setStrokeWidth(2);

        final double finalXPosition = Math.max(0, Math.min(xClick, imageView.getImage().getWidth()));

        peakLine.startXProperty().bind(Bindings.createDoubleBinding(() -> {
            double minX = imageView.getBoundsInParent().getMinX();
            return minX + finalXPosition * imageView.getScaleX();
        }, imageView.boundsInParentProperty(), imageView.scaleXProperty()));

        peakLine.endXProperty().bind(peakLine.startXProperty());
        peakLine.startYProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMinY(), imageView.boundsInParentProperty()));
        peakLine.endYProperty().bind(Bindings.createDoubleBinding(() -> imageView.getBoundsInParent().getMaxY(), imageView.boundsInParentProperty()));

        Pane imageContainer = (Pane) imageView.getParent();
        if (!imageContainer.getChildren().contains(peakLine)) {
            imageContainer.getChildren().add(peakLine);
        }

        LineInfo lineInfo = new LineInfo(null, peakLine, finalXPosition, lineTypeComboBox.getValue(), elementInput.getText(), Double.parseDouble(angleInput.getText()));
        imageLines.computeIfAbsent(imageProcessor.getSelectedImage(), k -> new ArrayList<>()).add(lineInfo);

        setupImageViewLineDragHandlers(peakLine, imageView, lineInfo);
    }

    private void setupLineDragHandlers(XYChart.Series<Number, Number> series, LineInfo lineInfo, LineChart<Number, Number> lineChart) {
        final Delta dragDelta = new Delta();

        series.getNode().setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                dragDelta.x = mouseEvent.getX();
                lineChart.getScene().setCursor(Cursor.MOVE);
            }
        });

        series.getNode().setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                lineChart.setAnimated(false);
                double newX = mouseEvent.getX();
                Number xValue = lineChart.getXAxis().getValueForDisplay(newX);

                if (xValue != null) {
                    series.getData().get(0).setXValue(xValue);
                    series.getData().get(1).setXValue(xValue);
                    lineInfo.setXPosition(xValue.doubleValue());
                }
            }
        });

        series.getNode().setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                lineChart.getScene().setCursor(Cursor.DEFAULT);
                lineChart.setAnimated(true);
            }
        });

        series.getNode().setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                lineChart.getScene().setCursor(Cursor.HAND);
            }
        });

        series.getNode().setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                lineChart.getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    private void setupImageViewLineDragHandlers(Line line, ImageView imageView, LineInfo lineInfo) {
        final Delta dragDelta = new Delta();

        line.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                dragDelta.x = mouseEvent.getX();
                line.getScene().setCursor(Cursor.MOVE);
            }
        });

        line.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                double newX = mouseEvent.getX();
                double minX = imageView.getBoundsInParent().getMinX();
                double maxX = minX + imageView.getImage().getWidth() * imageView.getScaleX();
                newX = Math.max(minX, Math.min(newX, maxX));

                line.setStartX(newX);
                line.setEndX(newX);

                double newRelativeX = (newX - minX) / imageView.getScaleX();
                lineInfo.setXPosition(newRelativeX);
            }
        });

        line.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                line.getScene().setCursor(Cursor.DEFAULT);
            }
        });

        line.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                line.getScene().setCursor(Cursor.HAND);
            }
        });

        line.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                line.getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    private LineChart<Number, Number> getActiveLineChart() {
        if (innerTabPane != null) {
            Tab selectedTab = innerTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null && selectedTab.getContent() instanceof LineChart) {
                return (LineChart<Number, Number>) selectedTab.getContent();
            }
        }
        return null;
    }

    private void updateButtonStyle() {
        if (peakSelectionMode) {
            pickPeaksButton.setText("Режим пиков: ВКЛ");
            pickPeaksButton.setStyle("-fx-background-color: lightgreen;");
        } else {
            pickPeaksButton.setText("Отметить Пики");
            pickPeaksButton.setStyle("");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void saveAngleToNotebook(String lineType, String angle) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("notebook.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Элемент: " + elementInput.getText()) && line.contains("Линия: " + lineType)) {
                    if (!angle.isEmpty()) {
                        lines.add("Элемент: " + elementInput.getText() + ", Линия: " + lineType + ", Угол: " + angle);
                    }
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!angle.isEmpty() && lines.stream().noneMatch(line -> line.contains("Элемент: " + elementInput.getText()) && line.contains("Линия: " + lineType))) {
            lines.add("Элемент: " + elementInput.getText() + ", Линия: " + lineType + ", Угол: " + angle);
        }

        try (FileWriter writer = new FileWriter("notebook.txt")) {
            for (String l : lines) {
                writer.write(l + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Delta {
        double x, y;
    }
}
