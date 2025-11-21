package com.nucleonforge.axile.common.api.metrics;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * The metric profile as returned by the Actuator API.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/metrics.html#metrics.retrieving-metric">Metrics Actuator API</a>
 * @author Mikhail Polivakha
 */
public record MetricProfile(
        String name,
        @Nullable String description,
        String baseUnit,
        List<Measurement> measurements,
        List<Map<String, String>> validTagCombinations) {

    /**
     * Single metric value, measured at a particular point in time.
     *
     * @param statistic the statistic of the measurement (we're not sure what it actually is)
     * @param value     the value of the given metric.
     */
    public record Measurement(String statistic, double value) {}
}
