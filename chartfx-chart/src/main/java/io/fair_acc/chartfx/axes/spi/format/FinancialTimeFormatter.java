package io.fair_acc.chartfx.axes.spi.format;

import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.axes.TickUnitSupplier;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author rstein
 */
public class FinancialTimeFormatter extends AbstractFormatter {
    private static final TickUnitSupplier DEFAULT_TICK_UNIT_SUPPLIER = new FinancialTimeTickUnitSupplier();
    private static final DateTimeFormatter HIGHRES_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss +SSS",
            Locale.ENGLISH);
    protected final DateTimeFormatter[] dateFormat;
    protected int oldIndex = -1;
    protected int formatterIndex;

    protected ObjectProperty<ZoneOffset> timeZone = new SimpleObjectProperty<>(ZoneOffset.UTC);

    /**
     * Construct a DefaultFormatter for the given NumberAxis
     */
    public FinancialTimeFormatter() {
        this(null);
    }

    /**
     * Construct a DefaultFormatter for the given NumberAxis
     *
     * @param axis The axis to format tick marks for
     */
    public FinancialTimeFormatter(final Axis axis) {
        super(axis);
        setTickUnitSupplier(FinancialTimeFormatter.DEFAULT_TICK_UNIT_SUPPLIER);

        dateFormat = new DateTimeFormatter[FinancialTimeTickUnitSupplier.TICK_UNIT_FORMATTER_DEFAULTS.length];
        for (int i = 0; i < dateFormat.length; i++) {
            final String format = FinancialTimeTickUnitSupplier.TICK_UNIT_FORMATTER_DEFAULTS[i];
            if (format.contains(FinancialTimeTickUnitSupplier.HIGHRES_MODE)) {
                dateFormat[i] = FinancialTimeFormatter.HIGHRES_FORMATTER;
            } else {
                dateFormat[i] = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);
            }
        }
    }

    public String formatHighResString(final Number utcValueSeconds) {
        final double timeAbs = Math.abs(utcValueSeconds.doubleValue());
        final long timeUS = (long) (TimeUnit.SECONDS.toMicros(1) * timeAbs);
        final long longUTCSeconds = Math.abs(utcValueSeconds.longValue());
        final int longNanoSeconds = (int) ((timeAbs - longUTCSeconds) * 1e9);
        final LocalDateTime dateTime = LocalDateTime.ofEpochSecond(longUTCSeconds, longNanoSeconds,
                getTimeZoneOffset());
        return dateTime.format(FinancialTimeFormatter.HIGHRES_FORMATTER).concat(Long.toString(timeUS % 1000)).concat("us").replaceAll(" ", System.lineSeparator());
    }

    @Override
    public Number fromString(final String string) {
        return null;
    }

    public String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    private String getTimeString(final Number utcValueSeconds) {
        if (formatterIndex <= FinancialTimeTickUnitSupplier.HIGHRES_MODE_INDICES) {
            return formatHighResString(utcValueSeconds);
        }

        long longUTCSeconds = utcValueSeconds.longValue();
        int nanoSeconds = (int) ((utcValueSeconds.doubleValue() - longUTCSeconds) * 1e9);
        if (nanoSeconds < 0) { // Correctly Handle dates before EPOCH
            longUTCSeconds -= 1;
            nanoSeconds += (int) 1e9;
        }
        final LocalDateTime dateTime = LocalDateTime.ofEpochSecond(longUTCSeconds, nanoSeconds,
                getTimeZoneOffset());

        return dateTime.format(dateFormat[formatterIndex]).replaceAll(" ", System.lineSeparator());
    }

    /**
     * @return Returns the A time-zone offset from Greenwich/UTC, such as {@code +02:00}.
     */
    public ZoneOffset getTimeZoneOffset() {
        return timeZoneOffsetProperty().get();
    }

    @Override
    protected void rangeUpdated() {
        // set formatter based on range if necessary
        formatterIndex = FinancialTimeTickUnitSupplier.getTickIndex(getRange());
        if (oldIndex != formatterIndex) {
            labelCache.clear();
            oldIndex = formatterIndex;
        }
    }

    /**
     * @param newOffset the ZoneOffset to be taken into account (UTC if 'null'.
     */
    public void setTimeZoneOffset(final ZoneOffset newOffset) {
        if (newOffset != null) {
            timeZoneOffsetProperty().set(newOffset);
            return;
        }
        timeZoneOffsetProperty().set(ZoneOffset.UTC);
    }

    /**
     * A time-zone offset from Greenwich/UTC, such as {@code +02:00}.
     * <p>
     * A time-zone offset is the amount of time that a time-zone differs from Greenwich/UTC. This is usually a fixed
     * number of hours and minutes.
     *
     * @return ZoneOffset property that is being used to compute the local time axis
     */
    public ObjectProperty<ZoneOffset> timeZoneOffsetProperty() {
        return timeZone;
    }

    @Override
    public String toString(final Number utcValueSeconds) {
        return labelCache.computeIfAbsent(utcValueSeconds, this::getTimeString);
    }
}
