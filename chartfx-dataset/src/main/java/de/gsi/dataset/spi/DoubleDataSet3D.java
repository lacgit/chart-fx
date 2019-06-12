package de.gsi.dataset.spi;

import de.gsi.dataset.event.RemovedDataEvent;
import de.gsi.dataset.utils.AssertUtils;

/**
 * Implementation of a AbstractDataSet3D backed by arrays. The z-values are
 * stored in a 2-dim array d[row][column] or d[y][x].
 *
 * @author braeun
 */
public class DoubleDataSet3D extends AbstractDataSet3D<DoubleDataSet3D> {

    private double[] xValues;
    private double[] yValues;
    private double[][] zValues;

    /**
     * 
     * @param name of data set
     */
    public DoubleDataSet3D(final String name) {
        super(name);
        xValues = new double[0];
        yValues = new double[0];
        zValues = new double[0][0];
    }

    /**
     * 
     * @param name of data set
     * @param dimX horizontal binning dimension (equidistant model)
     * @param dimY vertical binning dimension (equidistant model)
     */
    public DoubleDataSet3D(final String name, final int dimX, final int dimY) {
        super(name);
        zValues = new double[dimY][dimX];
        yValues = new double[zValues.length];
        for (int y = 0; y < yValues.length; y++) {
            yValues[y] = y;
        }
        if (yValues.length > 0) {
            xValues = new double[zValues[0].length];
            for (int x = 0; x < xValues.length; x++) {
                xValues[x] = x;
            }
        } else {
            xValues = new double[0];
        }
    }

    /**
     * 
     * @param name of data set
     * @param zValues array containing new X coordinates
     */
    public DoubleDataSet3D(final String name, final double[][] zValues) {
        super(name);
        yValues = new double[zValues.length];
        for (int y = 0; y < yValues.length; y++) {
            yValues[y] = y;
        }
        if (yValues.length > 0) {
            xValues = new double[zValues[0].length];
            for (int x = 0; x < xValues.length; x++) {
                xValues[x] = x;
            }
        } else {
            xValues = new double[0];
        }
        this.zValues = zValues;
    }

    /**
     * 
     * @param name of data set
     * @param xValues array containing new X coordinates
     * @param yValues array containing new X coordinates
     * @param zValues array containing new X coordinates
     */
    public DoubleDataSet3D(final String name, final double[] xValues, final double[] yValues,
            final double[][] zValues) {
        super(name);
        checkDimensionConsistency(xValues, yValues, zValues);
        this.xValues = xValues;
        this.yValues = yValues;
        this.zValues = zValues;
    }

    /**
     * overwrites/replaces data points with new coordinates 
     * @param xValues array containing new X coordinates
     * @param yValues array containing new X coordinates
     * @param zValues array containing new X coordinates
     */
    public void set(final double[] xValues, final double[] yValues, final double[][] zValues) {
        checkDimensionConsistency(xValues, yValues, zValues);
        this.xValues = xValues;
        this.yValues = yValues;
        this.zValues = zValues;
    }

    private static void checkDimensionConsistency(final double[] xValues, final double[] yValues,
            final double[][] zValues) {
        if (xValues == null) {
            throw new IllegalArgumentException("xValues array is null");
        }
        if (xValues.length == 0) {
            throw new IllegalArgumentException("xValues array length is '0'");
        }
        if (yValues == null) {
            throw new IllegalArgumentException("yValues array is null");
        }
        if (yValues.length == 0) {
            throw new IllegalArgumentException("yValues array length is '0'");
        }
        if (zValues == null) {
            throw new IllegalArgumentException("zValues array is null");
        }
        if (zValues.length == 0) {
            throw new IllegalArgumentException("zValues array length is '0'");
        }
        if (zValues.length != yValues.length) {
            final String msg = String.format("array dimension mismatch: zValues.length = %d != yValues.length = %d",
                    zValues.length, yValues.length);
            throw new IllegalArgumentException(msg);
        }
        if (zValues[0].length != xValues.length) {
            final String msg = String.format("array dimension mismatch: zValues[0].length = %d != xValues.length = %d",
                    zValues.length, yValues.length);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public double getZ(final int xIndex, final int yIndex) {
        return zValues[yIndex][xIndex];
    }

    /**
     * fast filling of an array with a default value <br>
     * initialize a smaller piece of the array and use the System.arraycopy call
     * to fill in the rest of the array in an expanding binary fashion
     *
     * @param array to be initialized
     * @param indexStart the first index to be set
     * @param indexStop the last index to be set
     * @param value the value for each to be set element
     */
    protected static void fillArray(final double[] array, final int indexStart, final int indexStop,
            final double value) {
        AssertUtils.notNull("array", array);
        final int len = indexStop - indexStart;

        if (len > 0) {
            array[indexStart] = value;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, indexStart, array, i, (len - i) < i ? len - i : i);
        }
    }

    /**
     * clears all data points
     * @return itself (fluent design)
     */
    public DoubleDataSet3D clearData() {
        lock();
        for (int i = 0; i < zValues.length; i++) {
            fillArray(zValues[i], 0, zValues[i].length, 0.0);
        }
        unlock();
        fireInvalidated(new RemovedDataEvent(this, "clearData()"));
        return this;
    }

    @Override
    public void set(final int xIndex, final int yIndex, final double x, final double y, final double z) {
        xValues[xIndex] = x;
        yValues[yIndex] = y;
        zValues[yIndex][xIndex] = z;

    }

    /**
     * 
     * @param xIndex index of the to be modified point
     * @param x new X coordinate
     */
    public void setX(final int xIndex, final double x) {
        xValues[xIndex] = x;
    }

    /**
     * 
     * @param yIndex index of the to be modified point
     * @param y new Y coordinate
     */
    public void setY(final int yIndex, final double y) {
        yValues[yIndex] = y;
    }

    /**
     * 
     * @param xIndex index of the to be modified point
     * @param yIndex index of the to be modified point
     * @param z new Z coordinate
     */
    public void set(final int xIndex, final int yIndex, final double z) {
        zValues[yIndex][xIndex] = z;

    }

    @Override
    public int getXDataCount() {
        return xValues.length;
    }

    @Override
    public int getYDataCount() {
        return yValues.length;
    }

    @Override
    public double getX(final int i) {
        return xValues[i];
    }

    @Override
    public double getY(final int i) {
        return yValues[i];
    }

    @Override
    public String getStyle(final int index) {
        return null;
    }

}