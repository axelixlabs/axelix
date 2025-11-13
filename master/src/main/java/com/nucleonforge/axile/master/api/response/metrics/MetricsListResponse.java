package com.nucleonforge.axile.master.api.response.metrics;

import java.util.List;

/**
 * Response to the {@link com.nucleonforge.axile.master.api.ApiPaths.MetricsApi#MAIN}.
 *
 * @author Mikhail Polivakha
 */
public record MetricsListResponse(List<MetricsGroup> metricsGroups) {

    /**
     * Metrics Group.
     *
     * @param groupName the name of the group to which the {@link #metrics} belong to.
     * @param metrics the names of the metrics inside teh given group.
     */
    public record MetricsGroup(String groupName, List<String> metrics) {}
}
