package com.example.funproject;

import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
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
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpectralDataVisualization {

    private Line line1 = new Line();
    private Line line2 = new Line();
    private double initialY; // Исходная координата Y клика

    public SpectralDataVisualization() {
        // Пустой конструктор
    }

    /**
     * Обновляет LineChart на основе данных изображения, преобразованных в сплайны.
     *
     * @param tab            текущая вкладка
     * @param image          изображение для обработки
     * @param innerTabPane   TabPane, содержащий LineChart
     * @return               серия данных для сплайна или null, если возникла ошибка
     */
    public XYChart.Series<Number, Number> updateChartWithSplineData(Tab tab, Image image, TabPane innerTabPane) {
        if (innerTabPane == null || image == null) {
            return null; // Ошибка: не найден TabPane или изображение
        }

        Tab currentTab = innerTabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null || !(currentTab.getContent() instanceof LineChart)) {
            return null; // Ошибка: не найдена текущая вкладка или она не содержит LineChart
        }

        LineChart<Number, Number> chart = (LineChart<Number, Number>) currentTab.getContent();

        // Обрабатываем изображение и получаем данные для сплайна
        XYChart.Series<Number, Number> series = processImageForSplineData(image);

        // Добавляем данные в LineChart и настраиваем его внешний вид
        chart.getData().add(series);
        chart.setCreateSymbols(false); // Отключаем отображение символов точек данных
        chart.setLegendVisible(false); // Отключаем отображение легенды

        return series;
    }

    /**
     * Обрабатывает изображение и возвращает серию данных для сплайна.
     *
     * @param image  изображение для обработки
     * @return       серия данных для сплайна
     */
    private XYChart.Series<Number, Number> processImageForSplineData(Image image) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Спектр"); // Название серии данных

        if (image != null && image.getPixelReader() != null) {
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            for (int x = 0; x < width; x++) {
                double totalIntensity = 0;
                for (int y = 0; y < height; y++) {
                    Color color = pixelReader.getColor(x, y);
                    double intensity = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                    totalIntensity += intensity;
                }
                double averageIntensity = totalIntensity / height;
                series.getData().add(new XYChart.Data<>(x, averageIntensity * 100));
            }
        }

        return series;
    }

    /**
     * Импортирует данные из таблицы и визуализирует их на графике и в таблице.
     *
     * @param tableViewToUpdate     TableView для обновления данными
     * @param selectedFile          выбранный файл с данными
     */
    public void importTableData(TableView<SpectralDataTable.SpectralData> tableViewToUpdate, File selectedFile) {
        if (selectedFile != null) {
            List<SpectralDataTable.SpectralData> tableData = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\s+"); // Разделение по пробелам
                    if (parts.length >= 2) {
                        try {
                            double xValue = Double.parseDouble(parts[0]);
                            double yValue = Double.parseDouble(parts[1]);
                            tableData.add(new SpectralDataTable.SpectralData(xValue, yValue));
                        } catch (NumberFormatException e) {
                            System.err.println("Ошибка при разборе данных: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Обновление TableView
            tableViewToUpdate.setItems(FXCollections.observableArrayList(tableData));
        } else {
            // TODO: Обработка случая, когда файл не выбран
        }
    }

    public void visualizeFromTable(Tab currentTab, LineChart<Number, Number> currentChart, TableView<SpectralDataTable.SpectralData> tableView) {
        // Очищаем текущий график
        // currentChart.getData().clear();

        // Создаем новую серию данных
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Intensities");

        // Извлекаем данные из каждой строки таблицы и добавляем в серию
        for (SpectralDataTable.SpectralData data : tableView.getItems()) {
            series.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
        }

        // Добавляем серию на график
        currentChart.getData().add(series);
        currentChart.setCreateSymbols(false); // Отключаем отображение символов точек данных
        currentChart.setLegendVisible(false); // Отключаем отображение легенды
    }

    /**
     * Устанавливает курсор для ImageView и добавляет линии, следующие за курсором.
     *
     * @param imageView ImageView для установки курсора
     */
    public void setImageViewCursorAndLines(ImageView imageView) {
        imageView.setOnMouseEntered(event -> imageView.setCursor(Cursor.CROSSHAIR));
        imageView.setOnMouseExited(event -> imageView.setCursor(Cursor.DEFAULT));

        imageView.setOnMousePressed(event -> {
            Pane parentPane = (Pane) imageView.getParent();
            initialY = event.getY();

            // Установка начальных координат для линий
            addHorizontalLine(initialY, imageView, parentPane, line1);
            addHorizontalLine(initialY, imageView, parentPane, line2);
        });

        imageView.setOnMouseDragged(event -> updateLines(event, imageView));
        imageView.setOnMouseReleased(event -> updateLines(event, imageView));
    }

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

    private void updateLines(MouseEvent event, ImageView imageView) {
        double currentY = event.getY();

        // Линия, следующая за курсором
        line1.startYProperty().unbind();
        line1.endYProperty().unbind();
        line1.setStartY(currentY);
        line1.setEndY(currentY);

        // Линия, движущаяся в противоположную сторону
        double oppositeY = 2 * initialY - currentY;
        line2.startYProperty().unbind();
        line2.endYProperty().unbind();
        line2.setStartY(oppositeY);
        line2.setEndY(oppositeY);

        // Обновляем биндинги, чтобы линии корректно масштабировались
        addHorizontalLine(line1.getStartY(), imageView, (Pane) imageView.getParent(), line1);
        addHorizontalLine(line2.getStartY(), imageView, (Pane) imageView.getParent(), line2);
    }
}
