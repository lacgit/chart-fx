package io.fair_acc.sample;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.AxisMode;
import io.fair_acc.chartfx.axes.spi.DefaultFinancialAxis;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.OhlcvTooltip;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.renderer.ErrorStyle;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.chartfx.renderer.spi.financial.CandleStickRenderer;
import io.fair_acc.chartfx.renderer.spi.financial.FinancialTheme;
import io.fair_acc.chartfx.ui.geometry.Side;
import io.fair_acc.chartfx.utils.AxisSynchronizer;
import io.fair_acc.chartfx.utils.NumberFormatterImpl;
import io.fair_acc.dataset.spi.DefaultDataSet;
import io.fair_acc.dataset.spi.financial.OhlcvDataSet;
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcv;
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcvItem;
import io.fair_acc.dataset.utils.ProcessingProfiler;
import io.fair_acc.sample.chart.utils.ReorderableSplitPane;
import io.fair_acc.sample.financial.service.SimpleOhlcvMinuteParser;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.IOException;
import java.util.Arrays;

public class DynamicallyAlignedCharts extends Application {
    protected AxisSynchronizer financialAxisSynchronizer = new AxisSynchronizer();

    @Override
    public void start(Stage primaryStage) throws IOException {
        final String resourceFile1 = "@ES-[TF1D]";
        final String resourceFile2 = "@HS-[TF1D]";

        OhlcvDataSet dataSet1   =   new OhlcvDataSet("ES");
        OhlcvDataSet dataSet2   =   new OhlcvDataSet("HS");
        DefaultDataSet indiSet  =   new DefaultDataSet("MA24");
        try {
            loadTestData(resourceFile1, dataSet1, indiSet, resourceFile2, dataSet2);
        } catch (IOException e) {
            throw new IOException(e.getMessage(), e);
        }

        DefaultDataSet rsiSet   =   getRsiDataSet(dataSet1, 9);

        // Create charts
        XYChart chart1 = getMasterChart("ES", dataSet1);
        XYChart chart2 = getIndicatorChart("RSI", dataSet1, rsiSet);
        financialAxisSynchronizer.add(chart1.getXAxis());
        financialAxisSynchronizer.add(chart2.getXAxis());

        var avgRenderer = new ErrorDataSetRenderer();
        avgRenderer.setDrawMarker(false);
        avgRenderer.setErrorStyle(ErrorStyle.NONE);
        avgRenderer.getDatasets().addAll(indiSet);

        final DefaultNumericAxis yAxis2 = new DefaultNumericAxis("HS", "points");
        yAxis2.setSide(Side.RIGHT);
        yAxis2.setTickLabelFormatter(new NumberFormatterImpl());
        yAxis2.getAxisLabel().setVisible(false);
        final ErrorDataSetRenderer errorRenderer2 = new ErrorDataSetRenderer();
        errorRenderer2.getAxes().add(yAxis2);
        errorRenderer2.getDatasets().setAll(dataSet2);

        chart1.getRenderers().addAll(avgRenderer, errorRenderer2);
        chart1.getYAxis().setSide(Side.LEFT);
        chart2.getYAxis().setSide(Side.LEFT);
        StackPane region1 = new StackPane();
        StackPane region2 = new StackPane();
        StackPane region3 = new StackPane();
        region1.getChildren().add(chart1);
        region2.getChildren().add(chart2);
        chart1.setMouseTransparent(true);
        chart2.setMouseTransparent(true);

        synchronizeAxes(chart1, chart2);
        alignYAxisWidths(chart1, chart2);

        // Layout
        ReorderableSplitPane root = new ReorderableSplitPane(region1, region2, region3);
        root.setPadding(new Insets(10));
        root.setOrientation(Orientation.VERTICAL);
        root.setDividerPositions(0.6);
        root.getStylesheets().add(getClass().getResource("DynamicallyAlignedCharts.css").toExternalForm());

        Scene scene = new Scene(root, 800, 600);

        // Synchronize after layout pass
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            alignPlotAreas(chart1, chart2); // Update padding on resize
        });

        scene.addPostLayoutPulseListener(() -> {
            alignPlotAreas(chart1, chart2); // Update padding on resize
        //  alignYAxisWidths(chart1, chart2);
        //  synchronizePlotAreas(chart1, chart2);
        });

        // Handle window resizing
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            alignPlotAreas(chart1, chart2); // Update padding on resize
        //  alignYAxisWidths(chart1, chart2);
        //  synchronizePlotAreas(chart1, chart2);
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected void loadTestData(String resourceFile1, final OhlcvDataSet dataSet1, DefaultDataSet indiSet, String resourceFile2, OhlcvDataSet dataSet2) throws IOException {
        final String datePattern = "MM/dd/yyyy";
        final long startTime = ProcessingProfiler.getTimeStamp();

        IOhlcv ohlcv = new SimpleOhlcvMinuteParser(datePattern).getContinuousOHLCV(resourceFile1);
        dataSet1.setData(ohlcv);

        DescriptiveStatistics stats = new DescriptiveStatistics(24);
        for (IOhlcvItem ohlcvItem : ohlcv) {
            double timestamp = ohlcvItem.getTimeStamp().getTime() / 1000.0;
            stats.addValue(ohlcvItem.getClose());
            indiSet.add(timestamp, stats.getMean());
        }

        IOhlcv ohlcv2 = new SimpleOhlcvMinuteParser(datePattern).getContinuousOHLCV(resourceFile2);
        dataSet2.setData(ohlcv2);
        /**
        for (IOhlcvItem ohlcvItem : ohlcv2) {
            double timestamp = ohlcvItem.getTimeStamp().getTime() / 1000.0;
            dataSet2.add(timestamp, ohlcvItem.getClose());
        }
        */
        ProcessingProfiler.getTimeDiff(startTime, "adding data into DataSet");
    }

    private DefaultDataSet getRsiDataSet(OhlcvDataSet dataSet, int rsiRange) {
        DefaultDataSet indiSet = new DefaultDataSet("RSI");
        if (dataSet.getDataCount()<rsiRange)
            return  indiSet;
        double movingMin = Double.MAX_VALUE;
        double movingMax = -Double.MAX_VALUE;
        for (int i=0; i<rsiRange; i++) {
            if (dataSet.getItem(i).getClose()<movingMin)
                movingMin = dataSet.getItem(i).getClose();
            if (dataSet.getItem(i).getClose()>movingMax)
                movingMax = dataSet.getItem(i).getClose();
            indiSet.add(dataSet.getItem(i).getTimeStamp().getTime()/1000.0, 0.0);
        }
        for (int i=rsiRange; i<dataSet.getDataCount(); i++) {
            IOhlcvItem ohlcvItem = dataSet.getItem(i);
            double timestamp = ohlcvItem.getTimeStamp().getTime() / 1000.0;
            double value = ohlcvItem.getClose();
            double pValue = dataSet.getItem(i-rsiRange).getClose();
            double rsi = (value - pValue) / (movingMax - movingMin);
            indiSet.add(timestamp, rsi);
            if (dataSet.getItem(i).getClose()<movingMin)
                movingMin = dataSet.getItem(i).getClose();
            if (dataSet.getItem(i).getClose()>movingMax)
                movingMax = dataSet.getItem(i).getClose();
        }

        return  indiSet;
    }

    private XYChart getMasterChart(String title, OhlcvDataSet candleStickDataSet) {
        final DefaultFinancialAxis xAxis = new DefaultFinancialAxis("Time", "iso", candleStickDataSet);
        final DefaultNumericAxis yAxis = new DefaultNumericAxis("ES", "points");

        // prepare chart structure
        final XYChart chart = new XYChart(xAxis, yAxis);
        chart.setTitle(FinancialTheme.Blackberry.name());
        chart.setLegendVisible(true);
        // set them false to make the plot faster
        chart.setAnimated(false);
        chart.getLegend().setSide(Side.TOP);


        // prepare plugins
        chart.getPlugins().add(new Zoomer(AxisMode.X));
        chart.getPlugins().add(new EditAxis());
        chart.getPlugins().add(new OhlcvTooltip());

        // basic chart financial structure style
        chart.getGridRenderer().setDrawOnTop(false);
        yAxis.setAutoRangeRounding(true);
        yAxis.setSide(Side.RIGHT);
        yAxis.setTickLabelFormatter(new NumberFormatterImpl());
        yAxis.getAxisLabel().setVisible(false);

        var candleStickRenderer = new CandleStickRenderer(true);
        candleStickRenderer.getDatasets().addAll(candleStickDataSet);

        chart.getRenderers().clear();
        chart.getRenderers().add(candleStickRenderer);

        GridPane.setHgrow(chart, Priority.ALWAYS);
        GridPane.setVgrow(chart, Priority.ALWAYS);

        return chart;
    }

    private void alignPlotAreas(XYChart... charts) {
        double maxLeft = 0;
        double maxRight = 0;
        double maxTop = 0;
        double maxBottom = 0;

        // Find maximum dimensions across all charts
        for (XYChart chart : charts) {
            Node plotArea = chart.lookup(".chart-plot-background");
            if (plotArea == null) continue;

            Bounds plotBounds = plotArea.getBoundsInParent();
            // Skip charts with collapsed plot areas
            if (plotBounds.getWidth() <= 1 || plotBounds.getHeight() <= 1) {
                continue;
            }

            maxLeft = Math.max(maxLeft, plotBounds.getMinX());
            maxRight = Math.max(maxRight, chart.getWidth() - plotBounds.getMaxX());
            maxTop = Math.max(maxTop, plotBounds.getMinY());
            maxBottom = Math.max(maxBottom, chart.getHeight() - plotBounds.getMaxY());
        }

        // Apply uniform padding to all charts
        for (XYChart chart : charts) {
            Node plotArea = chart.lookup(".chart-plot-background");
            if (plotArea == null) continue;

            Bounds plotBounds = plotArea.getBoundsInParent();
            // Skip charts with collapsed plot areas
            if (plotBounds.getWidth() <= 1 || plotBounds.getHeight() <= 1) {
                continue;
            }

            double leftPadding = maxLeft - plotBounds.getMinX();
            double rightPadding = maxRight - (chart.getWidth() - plotBounds.getMaxX());
            double topPadding = maxTop - plotBounds.getMinY();
            double bottomPadding = maxBottom - (chart.getHeight() - plotBounds.getMaxY());

            // Add compensation padding to match maximum dimensions
            Insets current = chart.getPadding();
            chart.setPadding(new Insets(
                        current.getTop() + topPadding,
                        current.getRight() + rightPadding,
                        current.getBottom() + bottomPadding,
                        current.getLeft() + leftPadding
            ));
        }
    }

    private void alignYAxisWidths(XYChart... charts) {
        // Find maximum Y-axis width needed
        double maxWidth = 0;
        for (XYChart chart : charts) {
            DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
            yAxis.applyCss();
            yAxis.layout();
            maxWidth = Math.max(maxWidth, yAxis.prefWidth(-1));
        }

        // Apply this width to all Y-axes
        for (XYChart chart : charts) {
            DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
            yAxis.setPrefWidth(maxWidth);
            yAxis.setMinWidth(maxWidth);
            yAxis.setMaxWidth(maxWidth);
        }
    }

    private void synchronizeAxes(XYChart... charts) {
        // Synchronize X axes
        DefaultFinancialAxis firstXAxis = (DefaultFinancialAxis) Arrays.stream(charts).toList().get(0).getXAxis();
        firstXAxis.maxProperty().addListener((obs, oldVal, newVal) -> {
            for (XYChart chart : charts) {
                chart.getXAxis().setMax(newVal.doubleValue());
            }
        });

        // Synchronize Y axes
        DefaultNumericAxis firstYAxis = (DefaultNumericAxis) Arrays.stream(charts).toList().get(0).getYAxis();
        firstYAxis.maxProperty().addListener((obs, oldVal, newVal) -> {
            for (XYChart chart : charts) {
                chart.getYAxis().setMax(newVal.doubleValue());
            }
        });
    }

    private XYChart getIndicatorChart(String title, OhlcvDataSet timeAxisDataSet, DefaultDataSet indiSet) {
        DefaultFinancialAxis xAxis = new DefaultFinancialAxis("time", "iso", timeAxisDataSet);
        DefaultNumericAxis yAxis = new DefaultNumericAxis(title, "ind");
        xAxis.getTickLabelStyle().setVisible(false);
        final XYChart chart = new XYChart(xAxis, yAxis);
        yAxis.setSide(Side.RIGHT);
        chart.getPlugins().add(new Zoomer());
        chart.getPlugins().add(new EditAxis());
        chart.getPlugins().add(new DataPointTooltip());

        var indiRenderer = new ErrorDataSetRenderer();
        indiRenderer.setDrawMarker(false);
        indiRenderer.setErrorStyle(ErrorStyle.NONE);
        indiRenderer.getDatasets().addAll(indiSet);

        chart.getRenderers().clear();
        chart.getRenderers().add(indiRenderer);

        GridPane.setHgrow(chart, Priority.ALWAYS);
        GridPane.setVgrow(chart, Priority.ALWAYS);

        return chart;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
