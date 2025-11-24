package com.nucleonforge.axile.master.service.convert.response.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricsList;
import com.nucleonforge.axile.master.api.response.metrics.MetricsListResponse;
import com.nucleonforge.axile.master.api.response.metrics.MetricsListResponse.MetricsGroup;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * Converter from the {@link MetricsList} to the {@link MetricsListResponse}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class GetAllMetricsConverter implements Converter<MetricsList, MetricsListResponse> {

    public static final String OTHER_GROUP_NAME = "Others";

    @Override
    public @NonNull MetricsListResponse convertInternal(@NonNull MetricsList source) {

        Map<String, List<String>> metricsByGroupName =
                source.names().stream().collect(Collectors.groupingBy(GetAllMetricsConverter::extractGroupName));

        List<MetricsGroup> metricsGroup = new ArrayList<>(metricsByGroupName.size());

        for (var entry : metricsByGroupName.entrySet()) {
            metricsGroup.add(new MetricsGroup(entry.getKey(), entry.getValue()));
        }

        return new MetricsListResponse(metricsGroup);
    }

    private static String extractGroupName(String it) {
        String[] parts = it.split("\\.");

        if (parts.length == 1) {
            return OTHER_GROUP_NAME;
        }

        return parts[0];
    }
}
