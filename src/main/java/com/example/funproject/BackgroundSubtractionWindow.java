package com.example.funproject;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BackgroundSubtractionWindow extends Stage {

    private ComboBox<String> backgroundTypeComboBox;

    // Elements for linear background subtraction
    private Label x1Label;
    private TextField x1TextField;
    private Button x1Button;
    private Label x2Label;
    private TextField x2TextField;
    private Button x2Button;
    private Button subtractButton;

    // Elements for exponential background subtraction
    private Label aLabel;
    private TextField aTextField;
    private Button aButton;
    private Label bLabel;
    private TextField bTextField;
    private Button bButton;
    private Label nLabel;
    private TextField nTextField;
    private Button nButton;
    private Label cLabel;
    private TextField cTextField;
    private Button cButton;

    private TextField currentWaitingTextField = null;
    private List<Double> selectedPoints = new ArrayList<>();

    // Constructor for the background subtraction window
    public BackgroundSubtractionWindow(LineChart<Number, Number> chart) {

        // Initialize control elements
        initializeControls();

        // Create GridPane for arranging control elements
        GridPane gridPane = createGridPane();

        // Add control elements to GridPane
        addControlsToGridPane(gridPane);

        // Configure the window
        configureWindow(gridPane);

        // Handle subtract button click
        handleSubtractButtonClick(chart);

        // Handle point selection button clicks
        handlePointSelectionButtonsClick(chart);

        // Handle background type selection
        handleBackgroundTypeSelection();
    }

    // Initialize control elements
    private void initializeControls() {
        backgroundTypeComboBox = new ComboBox<>();
        backgroundTypeComboBox.getItems().addAll("Линейный", "Экспоненциальный");
        backgroundTypeComboBox.setValue("Линейный"); // Set initial value

        subtractButton = new Button("Вычесть фон");

        // Elements for linear background subtraction
        x1Label = new Label("X1:");
        x1TextField = new TextField();
        x1Button = new Button("...");
        x2Label = new Label("X2:");
        x2TextField = new TextField();
        x2Button = new Button("...");

        // Elements for exponential background subtraction
        aLabel = new Label("a:");
        aTextField = new TextField();
        aButton = new Button("...");
        bLabel = new Label("b:");
        bTextField = new TextField();
        bButton = new Button("...");
        nLabel = new Label("n:");
        nTextField = new TextField();
        nButton = new Button("...");
        cLabel = new Label("c:");
        cTextField = new TextField();
        cButton = new Button("...");
    }

    // Create GridPane for arranging control elements
    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        return gridPane;
    }

    // Add control elements to GridPane
    private void addControlsToGridPane(GridPane gridPane) {
        gridPane.add(new Label("Тип фона:"), 0, 0);
        gridPane.add(backgroundTypeComboBox, 1, 0);
        gridPane.add(subtractButton, 1, 8);

        // Add elements for linear background subtraction
        gridPane.add(x1Label, 0, 1);
        gridPane.add(x1TextField, 1, 1);
        gridPane.add(x1Button, 2, 1);
        gridPane.add(x2Label, 0, 2);
        gridPane.add(x2TextField, 1, 2);
        gridPane.add(x2Button, 2, 2);

        // Add elements for exponential background subtraction in a 2x2 grid
        gridPane.add(aLabel, 0, 3);
        gridPane.add(aTextField, 1, 3);
        gridPane.add(aButton, 2, 3);
        gridPane.add(bLabel, 0, 4);
        gridPane.add(bTextField, 1, 4);
        gridPane.add(bButton, 2, 4);
        gridPane.add(nLabel, 0, 5);
        gridPane.add(nTextField, 1, 5);
        gridPane.add(nButton, 2, 5);
        gridPane.add(cLabel, 0, 6);
        gridPane.add(cTextField, 1, 6);
        gridPane.add(cButton, 2, 6);
    }

    // Configure the window
    private void configureWindow(GridPane gridPane) {
        this.setTitle("Вычитание фона");
        this.setScene(new Scene(gridPane));
        this.setAlwaysOnTop(true);
    }

    // Handle subtract button click
    private void handleSubtractButtonClick(LineChart<Number, Number> chart) {
        subtractButton.setOnAction(event -> {
            String backgroundType = backgroundTypeComboBox.getValue();
            if ("Линейный".equals(backgroundType)) {
                subtractLinearBackground(chart);
            } else if ("Экспоненциальный".equals(backgroundType)) {
                if (selectedPoints.size() == 4) {
                    calculateExponentialParameters();
                    subtractExponentialBackground(chart);
                } else {
                    showAlert("Пожалуйста, выберите четыре точки на графике для экспоненциального вычитания фона.");
                }
            }
        });
    }

    // Handle point selection button clicks
    private void handlePointSelectionButtonsClick(LineChart<Number, Number> chart) {
        x1Button.setOnAction(event -> setTextFieldToWaitingMode(chart, x1TextField));
        x2Button.setOnAction(event -> setTextFieldToWaitingMode(chart, x2TextField));
        aButton.setOnAction(event -> setTextFieldToWaitingMode(chart, aTextField));
        bButton.setOnAction(event -> setTextFieldToWaitingMode(chart, bTextField));
        nButton.setOnAction(event -> setTextFieldToWaitingMode(chart, nTextField));
        cButton.setOnAction(event -> setTextFieldToWaitingMode(chart, cTextField));
    }

    // Set text field to waiting mode
    private void setTextFieldToWaitingMode(LineChart<Number, Number> chart, TextField textField) {
        if (currentWaitingTextField != null) {
            currentWaitingTextField.setStyle(""); // Reset style of previous text field
        }
        currentWaitingTextField = textField;
        textField.setStyle("-fx-background-color: yellow;");

        chart.setOnMouseClicked(mouseEvent -> {
            double sceneX = mouseEvent.getSceneX();
            double sceneY = mouseEvent.getSceneY();
            NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            double mouseXValue = xAxis.sceneToLocal(sceneX, sceneY).getX();
            Number xValue = xAxis.getValueForDisplay(mouseXValue);
            textField.setText(String.valueOf(xValue));
            selectedPoints.add(xValue.doubleValue());
            textField.setStyle(""); // Reset style

            currentWaitingTextField = null;

            // Reset event handler
            chart.setOnMouseClicked(null);
        });
    }

    // Linear background subtraction
    private void subtractLinearBackground(LineChart<Number, Number> chart) {
        XYChart.Series<Number, Number> lastSeries = chart.getData().get(chart.getData().size() - 1);
        ObservableList<XYChart.Data<Number, Number>> data = lastSeries.getData();

        // Get X1 and X2 values
        double x1 = Double.parseDouble(x1TextField.getText());
        double x2 = Double.parseDouble(x2TextField.getText());

        // Find closest points to X1 and X2
        XYChart.Data<Number, Number> point1 = findClosestPoint(data, x1);
        XYChart.Data<Number, Number> point2 = findClosestPoint(data, x2);

        // Get y1 and y2 values from closest points
        double y1 = point1.getYValue().doubleValue();
        double y2 = point2.getYValue().doubleValue();

        // Calculate coefficients of linear function (y = mx + b)
        double m = (y2 - y1) / (point2.getXValue().doubleValue() - point1.getXValue().doubleValue());
        double b = y1 - m * point1.getXValue().doubleValue();

        // Create new series for subtracted data and background line
        XYChart.Series<Number, Number> subtractedSeries = new XYChart.Series<>();
        subtractedSeries.setName("Intensities");
        XYChart.Series<Number, Number> backgroundLineSeries = new XYChart.Series<>();
        backgroundLineSeries.setName("Baseline");

        // Subtract background and add points to new series
        for (XYChart.Data<Number, Number> point : data) {
            double x = point.getXValue().doubleValue();
            double yValue = point.getYValue().doubleValue();
            double backgroundValue = m * x + b;
            double subtractedY = yValue - backgroundValue;
            subtractedSeries.getData().add(new XYChart.Data<>(x, subtractedY));
            backgroundLineSeries.getData().add(new XYChart.Data<>(x, backgroundValue));
        }

        // Add series to chart and hide legend
        chart.getData().clear();
        chart.getData().add(backgroundLineSeries);
        backgroundLineSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke-width: 1; -fx-stroke-dash-array: 2 2;");
        chart.getData().add(subtractedSeries);
        chart.setLegendVisible(false);
    }

    // Exponential background subtraction
    private void subtractExponentialBackground(LineChart<Number, Number> chart) {
        XYChart.Series<Number, Number> lastSeries = chart.getData().get(chart.getData().size() - 1);
        ObservableList<XYChart.Data<Number, Number>> data = lastSeries.getData();

        // Get a, b, n, and c values
        double a = Double.parseDouble(aTextField.getText());
        double b = Double.parseDouble(bTextField.getText());
        double n = Double.parseDouble(nTextField.getText());
        double c = Double.parseDouble(cTextField.getText());

        // Create new series for subtracted data and background curve
        XYChart.Series<Number, Number> subtractedSeries = new XYChart.Series<>();
        subtractedSeries.setName("Intensities");
        XYChart.Series<Number, Number> backgroundCurveSeries = new XYChart.Series<>();
        backgroundCurveSeries.setName("Baseline");

        // Subtract background and add points to new series
        for (XYChart.Data<Number, Number> point : data) {
            double x = point.getXValue().doubleValue();
            double yValue = point.getYValue().doubleValue();
            double backgroundValue = 1 / (a + b * Math.pow(x, n) + Math.exp(a * x)) + c;
            double subtractedY = yValue - backgroundValue;
            subtractedSeries.getData().add(new XYChart.Data<>(x, subtractedY));
            backgroundCurveSeries.getData().add(new XYChart.Data<>(x, backgroundValue));
        }

        // Add series to chart and hide legend
        chart.getData().clear();
        chart.getData().add(backgroundCurveSeries);
        backgroundCurveSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke-width: 1; -fx-stroke-dash-array: 2 2;");
        chart.getData().add(subtractedSeries);
        chart.setLegendVisible(false);
    }

    // Find closest point to given x value
    private XYChart.Data<Number, Number> findClosestPoint(ObservableList<XYChart.Data<Number, Number>> data, double x) {
        XYChart.Data<Number, Number> closestPoint = null;
        double minDistance = Double.MAX_VALUE;
        for (XYChart.Data<Number, Number> point : data) {
            double distance = Math.abs(point.getXValue().doubleValue() - x);
            if (distance < minDistance) {
                closestPoint = point;
                minDistance = distance;
            }
        }
        return closestPoint;
    }

    // Calculate exponential parameters using the selected points
    private void calculateExponentialParameters() {
        // Implement the method to calculate the parameters a, b, n, and c using the selected points
        // For simplicity, you can assign some dummy values to these parameters
        double a = 1.0;
        double b = 1.0;
        double n = 1.0;
        double c = 1.0;

        // Set calculated values to the respective text fields
        aTextField.setText(String.valueOf(a));
        bTextField.setText(String.valueOf(b));
        nTextField.setText(String.valueOf(n));
        cTextField.setText(String.valueOf(c));
    }

    // Handle background type selection
    private void handleBackgroundTypeSelection() {
        backgroundTypeComboBox.setOnAction(event -> {
            String selectedType = backgroundTypeComboBox.getValue();
            boolean isLinear = "Линейный".equals(selectedType);

            // Show/hide elements for linear background subtraction
            x1Label.setVisible(isLinear);
            x1TextField.setVisible(isLinear);
            x1Button.setVisible(isLinear);
            x2Label.setVisible(isLinear);
            x2TextField.setVisible(isLinear);
            x2Button.setVisible(isLinear);

            // Show/hide elements for exponential background subtraction
            aLabel.setVisible(!isLinear);
            aTextField.setVisible(!isLinear);
            aButton.setVisible(!isLinear);
            bLabel.setVisible(!isLinear);
            bTextField.setVisible(!isLinear);
            bButton.setVisible(!isLinear);
            nLabel.setVisible(!isLinear);
            nTextField.setVisible(!isLinear);
            nButton.setVisible(!isLinear);
            cLabel.setVisible(!isLinear);
            cTextField.setVisible(!isLinear);
            cButton.setVisible(!isLinear);
        });

        // Initially show elements for linear background subtraction
        String selectedType = backgroundTypeComboBox.getValue();
        boolean isLinear = "Линейный".equals(selectedType);

        x1Label.setVisible(isLinear);
        x1TextField.setVisible(isLinear);
        x1Button.setVisible(isLinear);
        x2Label.setVisible(isLinear);
        x2TextField.setVisible(isLinear);
        x2Button.setVisible(isLinear);

        aLabel.setVisible(!isLinear);
        aTextField.setVisible(!isLinear);
        aButton.setVisible(!isLinear);
        bLabel.setVisible(!isLinear);
        bTextField.setVisible(!isLinear);
        bButton.setVisible(!isLinear);
        nLabel.setVisible(!isLinear);
        nTextField.setVisible(!isLinear);
        nButton.setVisible(!isLinear);
        cLabel.setVisible(!isLinear);
        cTextField.setVisible(!isLinear);
        cButton.setVisible(!isLinear);
    }

    // Show alert dialog
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
