package com.example.funproject;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ImageProcessingWindow {

    private Image originalImage;
    private Image resultImage;
    private boolean isTopPointSet = false, isBottomPointSet = false;
    private Circle topPoint, bottomPoint;
    private Label angleLabel, pointsAngleLabel;
    private double currentAngle = 0; // Хранит текущий угол поворота
    private double originalImageWidth, originalImageHeight;
    private double scale = 1.0;

    public ImageProcessingWindow(Image image, int initialKernelSize) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Обработка изображения");

        this.originalImage = image;
        this.originalImageWidth = image.getWidth();
        this.originalImageHeight = image.getHeight();

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        StackPane imagePane = new StackPane(imageView);
        imagePane.setAlignment(Pos.CENTER);

        Pane parentPane = new Pane(imagePane);
        topPoint = new Circle(5, Color.RED);
        bottomPoint = new Circle(5, Color.RED);

        // Добавляем обработчики событий
        imagePane.setOnMousePressed(event -> handleMousePressed(event, parentPane, imageView));
        imagePane.setOnScroll(this::handleScroll);

        Label kernelSizeLabel = new Label("Коэффициент сглаживания:");
        TextField kernelSizeInput = new TextField(String.valueOf(initialKernelSize));

        Button smoothButton = new Button("Сгладить");
        smoothButton.setOnAction(e -> {
            int kernelSize = Integer.parseInt(kernelSizeInput.getText());
            resultImage = imageSmoothing(originalImage, kernelSize);
            imageView.setImage(resultImage);
        });

        pointsAngleLabel = new Label("Угол между точками: 0°");
        angleLabel = new Label("Угол поворота: 0°");

        TextField angleInput = new TextField("0");
        Label angleInputLabel = new Label("Введите угол поворота:");

        Button rotateButton = new Button("Повернуть");
        rotateButton.setOnAction(e -> {
            double angle = Double.parseDouble(angleInput.getText());
            currentAngle += angle; // Обновляем текущий угол поворота
            resultImage = rotateImage(originalImage, currentAngle);
            imageView.setImage(resultImage);
            angleLabel.setText(String.format("Угол поворота: %.2f°", currentAngle));

            // Очистить старые точки и сбросить состояние рисования
            parentPane.getChildren().removeAll(topPoint, bottomPoint);
            topPoint = new Circle(5, Color.RED);
            bottomPoint = new Circle(5, Color.RED);
            isTopPointSet = false;
            isBottomPointSet = false;
            pointsAngleLabel.setText("Угол между точками: 0°");
        });

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 10; -fx-border-style: solid inside; -fx-border-width: 2; -fx-border-insets: 5; -fx-border-radius: 5; -fx-border-color: gray;");

        HBox kernelBox = new HBox(10, kernelSizeLabel, kernelSizeInput, smoothButton);
        kernelBox.setAlignment(Pos.CENTER);

        HBox angleBox = new HBox(10, angleInputLabel, angleInput, rotateButton);
        angleBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(parentPane, kernelBox, pointsAngleLabel, angleBox, angleLabel);

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void handleMousePressed(MouseEvent event, Pane parentPane, ImageView imageView) {
        double x = event.getX();
        double y = event.getY();

        // Получаем текущий масштаб изображения
        double scaleX = imageView.getScaleX();
        double scaleY = imageView.getScaleY();

        // Вычисляем координаты точки с учетом текущего масштаба
        double adjustedX = (x - imageView.getBoundsInParent().getMinX()) / scaleX;
        double adjustedY = (y - imageView.getBoundsInParent().getMinY()) / scaleY;

        if (!isTopPointSet) {
            topPoint.setCenterX(adjustedX);
            topPoint.setCenterY(adjustedY);
            bindPointToImage(topPoint, adjustedX, adjustedY, imageView);
            isTopPointSet = true;
            parentPane.getChildren().add(topPoint);
        } else if (!isBottomPointSet) {
            bottomPoint.setCenterX(adjustedX);
            bottomPoint.setCenterY(adjustedY);
            bindPointToImage(bottomPoint, adjustedX, adjustedY, imageView);
            isBottomPointSet = true;
            parentPane.getChildren().add(bottomPoint);
            double angle = calculateAngle(topPoint, bottomPoint);
            pointsAngleLabel.setText(String.format("Угол между точками: %.2f°", angle));
        }
    }





    private void bindPointToImage(Circle point, double initialX, double initialY, ImageView imageView) {
        point.centerXProperty().bind(Bindings.createDoubleBinding(() -> {
            double minX = imageView.getBoundsInParent().getMinX();
            return minX + (initialX * imageView.getScaleX());
        }, imageView.boundsInParentProperty(), imageView.scaleXProperty()));

        point.centerYProperty().bind(Bindings.createDoubleBinding(() -> {
            double minY = imageView.getBoundsInParent().getMinY();
            return minY + (initialY * imageView.getScaleY());
        }, imageView.boundsInParentProperty(), imageView.scaleYProperty()));
    }




    private double calculateAngle(Circle top, Circle bottom) {
        double deltaX = bottom.getCenterX() - top.getCenterX();
        double deltaY = bottom.getCenterY() - top.getCenterY();
        return -Math.toDegrees(Math.atan2(deltaX, deltaY));
    }

    private Image rotateImage(Image image, double angle) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage rotatedImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = rotatedImage.getPixelWriter();

        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double radians = Math.toRadians(angle);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = (int) ((x - centerX) * Math.cos(radians) - (y - centerY) * Math.sin(radians) + centerX);
                int newY = (int) ((x - centerX) * Math.sin(radians) + (y - centerY) * Math.cos(radians) + centerY);

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    pixelWriter.setColor(x, y, pixelReader.getColor(newX, newY));
                } else {
                    pixelWriter.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }

        return rotatedImage;
    }

    private Image imageSmoothing(Image originalImage, int kernelSize) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        WritableImage blurredImage = new WritableImage(width, height);
        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = blurredImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color averageColor = calculateAverageColor(pixelReader, x, y, width, height, kernelSize);
                pixelWriter.setColor(x, y, averageColor);
            }
        }

        return blurredImage;
    }

    private Color calculateAverageColor(PixelReader pixelReader, int x, int y, int width, int height, int kernelSize) {
        double red = 0.0;
        double green = 0.0;
        double blue = 0.0;
        double opacity = 0.0;
        int count = 0;
        int halfKernel = kernelSize / 2;

        for (int ny = -halfKernel; ny <= halfKernel; ny++) {
            for (int nx = -halfKernel; nx <= halfKernel; nx++) {
                int currentX = x + nx;
                int currentY = y + ny;

                if (currentX >= 0 && currentX < width && currentY >= 0 && currentY < height) {
                    Color color = pixelReader.getColor(currentX, currentY);
                    red += color.getRed();
                    green += color.getGreen();
                    blue += color.getBlue();
                    opacity += color.getOpacity();
                    count++;
                }
            }
        }

        return new Color(red / count, green / count, blue / count, opacity / count);
    }

    private void handleScroll(ScrollEvent event) {
        double delta = 1.1;
        double scaleFactor = (event.getDeltaY() > 0) ? delta : 1 / delta;

        scale *= scaleFactor;

        ImageView imageView = (ImageView) ((StackPane) event.getSource()).getChildren().get(0);
        imageView.setScaleX(scale);
        imageView.setScaleY(scale);
    }







    public Image getResultImage() {
        return resultImage;
    }
}