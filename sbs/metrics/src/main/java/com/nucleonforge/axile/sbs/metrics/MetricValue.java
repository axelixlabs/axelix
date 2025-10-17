package com.nucleonforge.axile.sbs.metrics;

import org.jspecify.annotations.Nullable;

/**
 * Interface that represents the value of the exported metric.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public interface MetricValue<T> {

    /**
     * @return the value itself, used for possible compositions and computations
     */
    T getValue();

    /**
     * @return displayable value (i.e. to be displayed on the end-user side)
     */
    String getDisplayableValue();

    /**
     * @return true if the value of the given metric is not within a rang that is considered healthy
     */
    boolean valueAlarm();

    /**
     * @return the description that explains why this metric value is not good. Might be null if {@link #valueAlarm()} is false.
     */
    @Nullable
    String alarmDescription();
}
