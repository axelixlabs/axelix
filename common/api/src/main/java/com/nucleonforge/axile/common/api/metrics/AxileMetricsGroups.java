package com.nucleonforge.axile.common.api.metrics;

import java.util.List;

import com.nucleonforge.axile.common.api.KeyValue;

/**
 * The response returned by the custom Axile metrics group list endpoint.
 *
 * @param metricsGroups the list of groups.
 *
 * @author Sergey Cherkasov
 */
public record AxileMetricsGroups(List<MetricsGroup> metricsGroups) {

    /**
     * Metrics Group.
     *
     * @param groupName the name of the group to which the {@link #metrics} belong to.
     * @param metrics   the names and descriptions of the metrics inside the given group.
     */
    public record MetricsGroup(String groupName, List<KeyValue> metrics) {}
}
