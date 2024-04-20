package com.example.funproject;

import com.example.funproject.imageutils.ButtonHandler;
import com.example.funproject.imageutils.ImageUtils;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageProcessor {

    private final HelloController controller; // Ссылка на контроллер для доступа к его переменным
    protected ImageView imageView; // ImageView для отображения обрабатываемого изображения
    protected Image selectedImage; // Текущее выбранное изображение
    private ImageView selectedThumbnailView; // ImageView для отслеживания эффекта DropShadow на миниатюре
    private final ImageUtils imageUtils; // Утилиты для работы с изображениями
    private ButtonHandler buttonHandler;


    // Сохраняем состояния масштабирования и позиции для каждого изображения (масштаб X, масштаб Y, сдвиг X, сдвиг Y)
    protected Map<Image, double[]> imageViewStates = new HashMap<>();

    /**
     * Получает ScrollPane из SplitPane по указанным индексам.
     */
    private ScrollPane getScrollPaneFromSplitPane(SplitPane splitPane, int splitPaneIndex, int itemIndex) {
        return (ScrollPane) ((SplitPane) splitPane.getItems().get(splitPaneIndex)).getItems().get(itemIndex);
    }

    // Геттеры и сеттеры
    public ImageView getImageView() {
        return imageView;
    }

    public ImageView getSelectedThumbnailView() {
        return selectedThumbnailView;
    }

    public Image getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(Image selectedImage) {
        this.selectedImage = selectedImage;
    }

    public void setSelectedThumbnailView(ImageView selectedThumbnailView) {
        this.selectedThumbnailView = selectedThumbnailView;
    }

    public Map<Image, double[]> getImageViewStates() {
        return new HashMap<>(imageViewStates); // Возвращаем копию для защиты данных
    }

    public void setImageViewStates(Map<Image, double[]> imageViewStates) {
        this.imageViewStates = new HashMap<>(imageViewStates); // Копируем данные для сохранения
    }

    public HelloController getController() {
        return this.controller;
    }

    public ImageProcessor(HelloController controller) {
        this.controller = controller;
        buttonHandler = new ButtonHandler(this, controller.imageLines);
        this.imageUtils = new ImageUtils(this, buttonHandler, controller);
    }

    /**
     * Размещает изображения на вкладке и настраивает UI:
     * - создает миниатюры для изображений на вкладке
     * - добавляет обработчики кликов по миниатюрам
     * - настраивает зум, перетаскивание и кнопку сброса для основного изображения
     *
     * @param images     - карта, где ключ - вкладка, значение - список изображений для этой вкладки
     * @param currentTab - текущая активная вкладка
     */
    public void putImagesOnTabPane(Map<Tab, List<Image>> images, Tab currentTab) {
        if (currentTab != null && currentTab.getContent() instanceof SplitPane mainSplitPane) {

            // Получаем элементы интерфейса
            ScrollPane thumbnailsScrollPane = getScrollPaneFromSplitPane(mainSplitPane, 1, 1); // Панель с миниатюрами
            TilePane thumbnailsTilePane = (TilePane) thumbnailsScrollPane.getContent(); // Контейнер для миниатюр
            ScrollPane mainImageScrollPane = getScrollPaneFromSplitPane(mainSplitPane, 1, 0); // Панель с основным изображением

            this.imageView = (ImageView) mainImageScrollPane.getContent(); // Сохраняем ссылку на ImageView

            // Очищаем панель с миниатюрами и настраиваем ее
            thumbnailsTilePane.getChildren().clear();
            thumbnailsTilePane.setPrefColumns(1);
            thumbnailsTilePane.setVgap(10);
            thumbnailsTilePane.setHgap(10);

            // Обрабатываем изображения и создаем миниатюры
            List<Image> tabImages = images.getOrDefault(currentTab, new ArrayList<>());
            for (Image img : tabImages) {
                imageUtils.createThumbnail(img, currentTab, thumbnailsTilePane);
            }

            // Настраиваем зум, перетаскивание и кнопку сброса для основного изображения
            imageUtils.setupZoom(this.imageView);
            imageUtils.setupImageDrag(this.imageView);
            //imageUtils.addButtonsBelowImageView(mainImageScrollPane);
            buttonHandler.addButtonsBelowImageView(mainImageScrollPane);
        }
    }

}