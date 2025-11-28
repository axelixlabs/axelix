package com.nucleonforge.axile.master.api.response.metrics;

import java.util.List;

/**
 * Response to the {@link com.nucleonforge.axile.master.api.ApiPaths.MetricsApi#MAIN}.
 *
 * @param metricsGroups the list of groups.
 *
 * @author Mikhail Polivakha
 */
public record MetricsGroupsFeedResponse(List<MetricsGroup> metricsGroups) {

    /**
     * Information about the metrics group.
     *
     * @param groupName the name of the group to which the {@link #metrics} belong to.
     * @param metrics the names of the metrics inside teh given group.
     */
    public record MetricsGroup(String groupName, List<MetricDescription> metrics) {

        /**
         * Information about the metric.
         *
         * @param metricName   the name of the metric.
         * @param description  the description of the metrics.
         */
        public record MetricDescription(String metricName, String description) {}
    }
}
