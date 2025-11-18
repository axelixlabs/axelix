package com.nucleonforge.axile.master.api.response.metrics;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Object that encapsulates the profile of the given metric.
 *
 * @author Mikhail Polivakha
 */
public record SingleMetricProfileResponse(
        String name,
        @Nullable String description,
        String baseUnit,
        List<Measurement> measurements,
        List<Map<String, String>> validTagCombinations) {

    /**
     * Single metric value, measured at a particular point in time.
     *
     * @param statistic the statistic of the measurement (we're not sure what it actually is)
     * @param value the value of the given metric.
     */
    public record Measurement(String statistic, double value) {}
}
