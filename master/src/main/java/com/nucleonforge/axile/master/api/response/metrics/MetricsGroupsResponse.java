package com.nucleonforge.axile.master.api.response.metrics;

import java.util.List;

import com.nucleonforge.axile.common.api.KeyValue;

/**
 * Response to the {@link com.nucleonforge.axile.master.api.ApiPaths.MetricsApi#MAIN}.
 *
 * @param metricsGroups the list of groups.
 *
 * @author Mikhail Polivakha
 */
public record MetricsGroupsResponse(List<MetricsGroup> metricsGroups) {

    /**
     * Metrics Group.
     *
     * @param groupName the name of the group to which the {@link #metrics} belong to.
     * @param metrics the names of the metrics inside teh given group.
     */
    public record MetricsGroup(String groupName, List<KeyValue> metrics) {}
}
