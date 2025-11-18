package com.nucleonforge.axile.common.api.metrics;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The metric profile filtered by specific tags as returned by the Actuator API.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/metrics.html#metrics.retrieving-metric">Metrics Actuator API</a>
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
public record MetricProfileByTags(
        String name,
        @Nullable String description,
        String baseUnit,
        List<Measurement> measurements,
        List<AvailableTag> availableTags) {

    /**
     * Single metric value, measured at a particular point in time.
     *
     * @param statistic the statistic of the measurement (we're not sure what it actually is)
     * @param value     the value of the given metric.
     */
    public record Measurement(String statistic, double value) {}

    /**
     * Tags that are available for this metric.
     *
     * @param tag    the tag name.
     * @param values the tag value.
     */
    public record AvailableTag(String tag, List<String> values) {}
}
