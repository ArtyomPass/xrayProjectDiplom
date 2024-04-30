package com.example.funproject;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class StatisticsWindow extends Stage {
    public StatisticsWindow(HelloController controller, LineChart<Number, Number> chart) {
        // Создание текстовой области для вывода информации
        TextArea infoArea = new TextArea();
        infoArea.setEditable(false); // Делаем область нередактируемой

        // Формирование текста с теоретической информацией и результатами
        String infoText = generateInfoText(chart);
        infoArea.setText(infoText);

        // Добавление текстовой области в окно
        VBox layout = new VBox(infoArea);
        VBox.setVgrow(infoArea, Priority.ALWAYS); // Добавляем эту строку для автоматического изменения размера
        this.setScene(new Scene(layout));
        this.setAlwaysOnTop(true);
    }

    private String generateInfoText(LineChart<Number, Number> chart) {
        // Вычисление параметров
        double energyMax = calculateEnergyMax(chart);
        double centerOfGravity = calculateCenterOfGravity(chart); ////////////// Тут криво считает
        double width = calculateSpectralLineWidth(chart);
        double asymmetry = calculateAsymmetryIndex(chart);
        double integralIntensity = calculateIntegralIntensity(chart);

        // Формирование текста
        return "**Теоретическая информация**\n\n" +
                "**Энергия максимума:**\n" +
                "Определяется на середине линии, соединяющей точки спектра, расположенные на 95% высоты.\n\n" +
                "**Центр тяжести:**\n" +
                "Отношение суммы произведений интенсивности в данной точке спектра на её энергию к числу точек спектра.\n\n" +
                "**Ширина:**\n" +
                "Расстояние между точками спектра, лежащими на половине высоты.\n\n" +
                "**Асимметрия:**\n" +
                "Отношение сумм интенсивностей в каждой точке спектра слева и справа от максимума.\n\n" +
                "**Интегральная интенсивность:**\n" +
                "Сумма интенсивности в каждой точке спектра.\n\n" +
                "**Результаты**\n\n" +
                "Энергия максимума: " + energyMax + "\n" +
                "Центр тяжести: " + centerOfGravity + "\n" +
                "Ширина: " + width + "\n" +
                "Асимметрия: " + asymmetry + "\n" +
                "Интегральная интенсивность: " + integralIntensity;
    }

    private double calculateEnergyMax(LineChart<Number, Number> chart) {
        // Получение данных из графика
        ObservableList<XYChart.Data<Number, Number>> data = chart.getData().get(chart.getData().size() - 1).getData();

        // Поиск максимума интенсивности
        double maxY = data.stream().mapToDouble(d -> d.getYValue().doubleValue()).max().getAsDouble();

        // Поиск точек на 95% высоты
        List<XYChart.Data<Number, Number>> points95 = data.stream()
                .filter(d -> d.getYValue().doubleValue() >= 0.95 * maxY)
                .collect(Collectors.toList());

        // Проверка, что найдено достаточно точек
        if (points95.size() < 2) {
            throw new IllegalArgumentException("Недостаточно точек для определения энергии максимума");
        }

        // Получение энергий двух точек на 95% высоты
        double energy1 = points95.get(0).getXValue().doubleValue();
        double energy2 = points95.get(points95.size() - 1).getXValue().doubleValue();

        // Расчет энергии максимума как середины между двумя точками
        return (energy1 + energy2) / 2;

    }

    private double calculateCenterOfGravity(LineChart<Number, Number> chart) {
        double sumIntensityEnergy = 0;
        int numPoints = chart.getData().get(chart.getData().size() - 1).getData().size();
        System.out.println(numPoints);
        // Получаем данные один раз для эффективности
        List<XYChart.Data<Number, Number>> dataPoints = chart.getData().get(chart.getData().size() - 1).getData();
        // Используем итератор для доступа к данным
        for (XYChart.Data<Number, Number> data : dataPoints) {
            double energy = data.getXValue().doubleValue();
            double intensity = data.getYValue().doubleValue();
            sumIntensityEnergy += intensity * energy;
            System.out.println(sumIntensityEnergy + " = " + intensity + " * " + energy);
        }
        return sumIntensityEnergy / numPoints;
    }

    private double calculateSpectralLineWidth(LineChart<Number, Number> chart) {
        // Получаем данные последней серии
        List<XYChart.Data<Number, Number>> data = chart.getData().get(chart.getData().size() - 1).getData();

        // 1. Найти максимум
        double maxIntensity = 0;
        int maxIndex = 0;
        for (int i = 0; i < data.size(); i++) {
            double intensity = data.get(i).getYValue().doubleValue();
            if (intensity > maxIntensity) {
                maxIntensity = intensity;
                maxIndex = i;
            }
        }

        // 2. Половина высоты
        double halfMaxIntensity = maxIntensity / 2;

        // 3. Найти точки пересечения
        int leftIndex = 0;
        int rightIndex = data.size() - 1;
        // Поиск слева
        for (int i = maxIndex; i >= 0; i--) {
            if (data.get(i).getYValue().doubleValue() <= halfMaxIntensity) {
                leftIndex = i;
                break;
            }
        }
        // Поиск справа
        for (int i = maxIndex; i < data.size(); i++) {
            if (data.get(i).getYValue().doubleValue() <= halfMaxIntensity) {
                rightIndex = i;
                break;
            }
        }

        // 4. Вычислить расстояние
        double leftEnergy = data.get(leftIndex).getXValue().doubleValue();
        double rightEnergy = data.get(rightIndex).getXValue().doubleValue();
        return Math.abs(rightEnergy - leftEnergy);
    }

    private double calculateAsymmetryIndex(LineChart<Number, Number> chart) {
        // Получить данные последней серии
        List<XYChart.Data<Number, Number>> data = chart.getData().get(chart.getData().size() - 1).getData();

        // Найти индекс максимума
        int maxIndex = 0;
        double maxValue = data.get(0).getYValue().doubleValue();
        for (int i = 1; i < data.size(); i++) {
            double value = data.get(i).getYValue().doubleValue();
            if (value > maxValue) {
                maxValue = value;
                maxIndex = i;
            }
        }

        // Суммировать интенсивности слева и справа от максимума
        double sumLeft = 0;
        double sumRight = 0;
        for (int i = 0; i < data.size(); i++) {
            if (i < maxIndex) {
                sumLeft += data.get(i).getYValue().doubleValue();
            } else if (i > maxIndex) {
                sumRight += data.get(i).getYValue().doubleValue();
            }
            // Интенсивность в точке максимума не учитывается
        }

        // Вычислить индекс ассиметрии
        return sumLeft / sumRight;
    }

    private double calculateIntegralIntensity(LineChart<Number, Number> chart) {
        double integralIntensity = 0;
        int lastChartSeries = chart.getData().size() - 1;
        List<XYChart.Data<Number, Number>> dataPoints = chart.getData().get(lastChartSeries).getData();

        for (XYChart.Data<Number, Number> data : dataPoints) {
            double intensity = data.getYValue().doubleValue();
            integralIntensity += intensity;
        }

        return integralIntensity;
    }

}
