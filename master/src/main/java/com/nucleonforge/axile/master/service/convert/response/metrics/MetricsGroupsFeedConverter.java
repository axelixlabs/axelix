package com.nucleonforge.axile.master.service.convert.response.metrics;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsFeedResponse;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsFeedResponse.MetricsGroup;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * Converter from the {@link MetricsGroupsFeed} to the {@link MetricsGroupsFeedResponse}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class MetricsGroupsFeedConverter implements Converter<MetricsGroupsFeed, MetricsGroupsFeedResponse> {

    @Override
    public @NonNull MetricsGroupsFeedResponse convertInternal(@NonNull MetricsGroupsFeed source) {
        List<MetricsGroup> metricsGroups = source.metricsGroups().stream()
                .map(metricsGroup ->
                        new MetricsGroup(metricsGroup.groupName(), convertMetricDescription(metricsGroup.metrics())))
                .toList();

        return new MetricsGroupsFeedResponse(metricsGroups);
    }

    private List<MetricsGroupsFeedResponse.MetricsGroup.MetricDescription> convertMetricDescription(
            List<MetricsGroupsFeed.MetricsGroup.MetricDescription> metrics) {
        return metrics.stream()
                .map(metric -> new MetricsGroup.MetricDescription(metric.metricName(), metric.description()))
                .toList();
    }
}
