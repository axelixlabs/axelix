package com.nucleonforge.axile.sbs.metrics;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The abstract metric value that contains common implementation for {@link MetricValue}.
 *
 * @since 09.07.25
 * @author Mikhail Polivakha
 */
public class AbstractMetric<T> implements MetricValue<T> {

    private final T value;
    private final String display;
    private final boolean alarmValue;

    @Nullable
    private final String alarmDescription;

    AbstractMetric(T value, String display, @NonNull String alarmDescription) {
        this.value = value;
        this.display = display;
        this.alarmValue = true;
        this.alarmDescription = alarmDescription;
    }

    AbstractMetric(T value, String display) {
        this.value = value;
        this.display = display;
        this.alarmValue = false;
        this.alarmDescription = null;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String getDisplayableValue() {
        return display;
    }

    @Override
    public boolean valueAlarm() {
        return alarmValue;
    }

    @Override
    public @Nullable String alarmDescription() {
        return alarmDescription;
    }
}
