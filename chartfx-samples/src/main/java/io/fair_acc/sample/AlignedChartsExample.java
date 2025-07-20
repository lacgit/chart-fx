package io.fair_acc.sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;

public class AlignedChartsExample extends Application {

    private static final double FIXED_Y_AXIS_WIDTH = 10; // Fixed width for Y-axis

    @Override
    public void start(Stage primaryStage) {
        // Create charts with fixed Y-axis width
        LineChart<Number, Number> chart1 = createChart("Chart 1");
        LineChart<Number, Number> chart2 = createChart("Chart 2");

        // Add sample data with different Y ranges
        addSampleData(chart1, "Series 1", 0, 100);
        addSampleData(chart2, "Series 2", 0, 10000); // Large Y range

        // Layout
        VBox root = new VBox(chart1, chart2);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 600);

        // Synchronize after layout pass
        scene.addPostLayoutPulseListener(() -> {
            synchronizePlotAreas(chart1, chart2);
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private LineChart<Number, Number> createChart(String title) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Force fixed Y-axis width
        yAxis.setPrefWidth(FIXED_Y_AXIS_WIDTH);
        yAxis.setMaxWidth(FIXED_Y_AXIS_WIDTH);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(false);

        // CSS to control padding
        chart.setStyle("-fx-padding: 10 15 20 " + FIXED_Y_AXIS_WIDTH + ";");

        return chart;
    }

    private void synchronizePlotAreas(LineChart<Number, Number> master, LineChart<Number, Number> slave) {
        // 1. Synchronize X-axis ranges
        NumberAxis masterXAxis = (NumberAxis) master.getXAxis();
        NumberAxis slaveXAxis = (NumberAxis) slave.getXAxis();

        masterXAxis.lowerBoundProperty().addListener((obs, oldVal, newVal) ->
                slaveXAxis.setLowerBound(newVal.doubleValue()));
        masterXAxis.upperBoundProperty().addListener((obs, oldVal, newVal) ->
                slaveXAxis.setUpperBound(newVal.doubleValue()));

        // 2. Get plot areas
        Node masterPlot = master.lookup(".chart-plot-background");
        Node slavePlot = slave.lookup(".chart-plot-background");

        if (masterPlot != null && slavePlot != null) {
            // 3. Force identical plot area bounds
            Bounds masterBounds = masterPlot.getBoundsInParent();

            // Calculate required scale factor
            Bounds slaveBounds = slavePlot.getBoundsInParent();
            double scaleX = masterBounds.getWidth() / slaveBounds.getWidth();

            // Apply scaling
            slavePlot.setScaleX(scaleX);

            // Adjust translation to align
            double translateX = masterBounds.getMinX() - (slaveBounds.getMinX() * scaleX);
            slavePlot.setTranslateX(translateX);
        }
    }

    private void addSampleData(LineChart<Number, Number> chart, String seriesName, double yMin, double yMax) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            double yValue = yMin + (yMax - yMin) * random.nextDouble();
            series.getData().add(new XYChart.Data<>(i, yValue));
        }

        chart.getData().add(series);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

