package com.nucleonforge.axile.master.service.convert.response.metrics;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsResponse;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsResponse.MetricsGroup;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * Converter from the {@link AxileMetricsGroups} to the {@link MetricsGroupsResponse}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class AxileMetricsGroupsConverter implements Converter<AxileMetricsGroups, MetricsGroupsResponse> {

    @Override
    public @NonNull MetricsGroupsResponse convertInternal(@NonNull AxileMetricsGroups source) {
        List<MetricsGroup> metricsGroups = source.metricsGroups().stream()
                .map(entry -> new MetricsGroup(entry.groupName(), entry.metrics()))
                .toList();

        return new MetricsGroupsResponse(metricsGroups);
    }
}
