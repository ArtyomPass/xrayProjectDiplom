package com.example.funproject;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BackgroundSubtractionWindow extends Stage {

    // Элементы управления
    private ComboBox<String> backgroundTypeComboBox;
    private Button subtractButton;

    // Элементы для линейного фона
    private Label x1Label;
    private TextField x1TextField;
    private Button x1Button;
    private Label x2Label;
    private TextField x2TextField;
    private Button x2Button;

    // Элементы для экспоненциального фона
    private Label beforeLineLabel;
    private Label afterLineLabel;
    // "От" и "До" для точек до линии
    private Label beforeFromLabel;
    private TextField beforeFromTextField;
    private Button beforeFromButton;
    private Label beforeToLabel;
    private TextField beforeToTextField;
    private Button beforeToButton;
    // "От" и "До" для точек после линии
    private Label afterFromLabel;
    private TextField afterFromTextField;
    private Button afterFromButton;
    private Label afterToLabel;
    private TextField afterToTextField;
    private Button afterToButton;

    private String xValueStr; // Переменная для хранения значения X из графика

    // Элементы управления (RadioButton, TextField etc.)
    public BackgroundSubtractionWindow(HelloController controller, LineChart<Number, Number> chart) {

        // Инициализация элементов управления
        backgroundTypeComboBox = new ComboBox<>();
        backgroundTypeComboBox.getItems().addAll("Линейный", "Экспоненциальный");
        subtractButton = new Button("Вычесть фон");

        // Инициализация элементов для линейного фона
        x1Label = new Label("X1:");
        x1TextField = new TextField();
        x1Button = new Button("...");
        x2Label = new Label("X2:");
        x2TextField = new TextField();
        x2Button = new Button("...");

        // Инициализация элементов для экспоненциального фона
        beforeLineLabel = new Label("Точки до линии:");
        afterLineLabel = new Label("Точки после линии:");
        // "От" и "До" для точек до линии
        beforeFromLabel = new Label("От:");
        beforeFromTextField = new TextField();
        beforeFromButton = new Button("...");
        beforeToLabel = new Label("До:");
        beforeToTextField = new TextField();
        beforeToButton = new Button("...");
        // "От" и "До" для точек после линии
        afterFromLabel = new Label("От:");
        afterFromTextField = new TextField();
        afterFromButton = new Button("...");
        afterToLabel = new Label("До:");
        afterToTextField = new TextField();
        afterToButton = new Button("...");

        // Создание GridPane для размещения элементов
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        // Добавление элементов на GridPane
        gridPane.add(new Label("Тип фона:"), 0, 0);
        gridPane.add(backgroundTypeComboBox, 1, 0);
        gridPane.add(subtractButton, 1, 8);

        // Добавление элементов для линейного фона (скрытые по умолчанию)
        gridPane.add(x1Label, 0, 2);
        gridPane.add(x1TextField, 1, 2);
        gridPane.add(x1Button, 2, 2);
        gridPane.add(x2Label, 0, 3);
        gridPane.add(x2TextField, 1, 3);
        gridPane.add(x2Button, 2, 3);

        // Скрыть элементы для линейного фона
        hideLinearBackground();

        // Добавление элементов для экспоненциального фона (скрытые по умолчанию)
        gridPane.add(beforeLineLabel, 0, 2);
        gridPane.add(beforeFromLabel, 0, 3);
        gridPane.add(beforeFromTextField, 1, 3);
        gridPane.add(beforeFromButton, 2, 3);
        gridPane.add(beforeToLabel, 0, 4);
        gridPane.add(beforeToTextField, 1, 4);
        gridPane.add(beforeToButton, 2, 4);

        gridPane.add(afterLineLabel, 0, 5);
        gridPane.add(afterFromLabel, 0, 6);
        gridPane.add(afterFromTextField, 1, 6);
        gridPane.add(afterFromButton, 2, 6);
        gridPane.add(afterToLabel, 0, 7);
        gridPane.add(afterToTextField, 1, 7);
        gridPane.add(afterToButton, 2, 7);

        // Скрыть элементы для экспоненциального фона
        hideExpBackground();

        // Настройка окна
        this.setTitle("Вычитание фона");
        this.setScene(new Scene(gridPane));
        this.setAlwaysOnTop(true);

        // Если это включить, то я не смогу выбирать точки на графике, потому что окно будет основным и не
        // позволит взаимодействовать с основной программой
        // this.initModality(Modality.APPLICATION_MODAL);

        // Обработка выбора типа фона
        backgroundTypeComboBox.setOnAction(event -> {
            String selectedType = backgroundTypeComboBox.getValue();
            if (selectedType.equals("Линейный")) {
                // Показать элементы для линейного фона
                showLinearBackgroud();

                // Скрыть элементы для экспоненциального фона
                hideExpBackground();

            } else if (selectedType.equals("Экспоненциальный")) {
                // Показать элементы для экспоненциального фона
                showExpBackground();

                // Скрыть элементы для линейного фона
                hideLinearBackground();
            }
        });

        // Обработка нажатия кнопки "Вычесть фон"
        subtractButton.setOnAction(event -> {
            // ... (код для вычитания фона в зависимости от выбранного типа и параметров)
        });

        // Обработка нажатия кнопки X1
        x1Button.setOnAction(event -> {
            chart.setOnMouseClicked(mouseEvent -> {
                double xValue = mouseEvent.getX();
                x1TextField.setText(String.valueOf(xValue));
            });
        });

        // Обработка нажатия кнопки X2
        x2Button.setOnAction(event -> {
            chart.setOnMouseClicked(mouseEvent -> {
                double xValue = mouseEvent.getX();
                x2TextField.setText(String.valueOf(xValue));
            });
        });

        // Обработка нажатия кнопки "От" до линии
        beforeFromButton.setOnAction(event -> {
            chart.setOnMouseClicked(mouseEvent -> {
                double xValue = mouseEvent.getX();
                beforeFromTextField.setText(String.valueOf(xValue));
            });
        });

        // Обработка нажатия кнопки "До" до линии
        beforeToButton.setOnAction(event -> {
            chart.setOnMouseClicked(mouseEvent -> {
                double xValue = mouseEvent.getX();
                beforeToTextField.setText(String.valueOf(xValue));
            });
        });

        // Обработка нажатия кнопки "От" после линии
        afterFromButton.setOnAction(event -> {
            chart.setOnMouseClicked(mouseEvent -> {
                double xValue = mouseEvent.getX();
                afterFromTextField.setText(String.valueOf(xValue));
            });
        });

        // Обработка нажатия кнопки "До" после линии
        afterToButton.setOnAction(event -> {
            chart.setOnMouseClicked(mouseEvent -> {
                double xValue = mouseEvent.getX();
                afterToTextField.setText(String.valueOf(xValue));
            });
        });


    }

    private String getXFromChart(LineChart<Number, Number> chart){
        chart.setOnMouseClicked(mouseEvent -> {
            double xValue = mouseEvent.getX();
            xValueStr = String.valueOf(xValue);
        });
    return xValueStr;
    }

    private void showLinearBackgroud() {
        // Показать элементы для линейного фона
        x1Label.setVisible(true);
        x1TextField.setVisible(true);
        x1Button.setVisible(true);
        x2Label.setVisible(true);
        x2TextField.setVisible(true);
        x2Button.setVisible(true);

    }

    private void hideLinearBackground() {
        // Скрыть элементы для линейного фона
        x1Label.setVisible(false);
        x1TextField.setVisible(false);
        x1Button.setVisible(false);

        x2Label.setVisible(false);
        x2TextField.setVisible(false);
        x2Button.setVisible(false);
    }

    private void showExpBackground() {
        beforeLineLabel.setVisible(true);
        afterLineLabel.setVisible(true);

        beforeFromLabel.setVisible(true);
        beforeFromTextField.setVisible(true);
        beforeFromButton.setVisible(true);

        beforeToLabel.setVisible(true);
        beforeToTextField.setVisible(true);
        beforeToButton.setVisible(true);

        afterFromLabel.setVisible(true);
        afterFromTextField.setVisible(true);
        afterFromButton.setVisible(true);

        afterToLabel.setVisible(true);
        afterToTextField.setVisible(true);
        afterToButton.setVisible(true);
    }

    private void hideExpBackground() {
        beforeLineLabel.setVisible(false);
        afterLineLabel.setVisible(false);

        beforeFromLabel.setVisible(false);
        beforeFromTextField.setVisible(false);
        beforeFromButton.setVisible(false);

        beforeToLabel.setVisible(false);
        beforeToTextField.setVisible(false);
        beforeToButton.setVisible(false);

        afterFromLabel.setVisible(false);
        afterFromTextField.setVisible(false);
        afterFromButton.setVisible(false);

        afterToLabel.setVisible(false);
        afterToTextField.setVisible(false);
        afterToButton.setVisible(false);
    }

}
