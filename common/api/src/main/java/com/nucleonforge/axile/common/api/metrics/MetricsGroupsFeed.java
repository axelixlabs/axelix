package com.nucleonforge.axile.common.api.metrics;

import java.util.List;

/**
 * The response returned by the custom Axile metric groups list endpoint.
 *
 * @param metricsGroups the list of groups.
 *
 * @author Sergey Cherkasov
 */
public record MetricsGroupsFeed(List<MetricsGroup> metricsGroups) {

    /**
     * DTO that encapsulates information about a metrics group.
     *
     * @param groupName the name of the group to which the {@link #metrics} belong to.
     * @param metrics   the names and descriptions of the metrics inside the given group.
     */
    public record MetricsGroup(String groupName, List<MetricDescription> metrics) {

        /**
         * DTO that encapsulates information about a metric.
         *
         * @param metricName   the name of the metric.
         * @param description  the description of the metrics.
         */
        public record MetricDescription(String metricName, String description) {}
    }
}
