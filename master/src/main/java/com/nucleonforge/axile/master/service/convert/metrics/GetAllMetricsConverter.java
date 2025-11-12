package com.nucleonforge.axile.master.service.convert.metrics;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricsList;
import com.nucleonforge.axile.master.api.response.metrics.MetricsListResponse;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * Converter from the {@link MetricsList} to the {@link MetricsListResponse}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class GetAllMetricsConverter implements Converter<MetricsList, MetricsListResponse> {

    @Override
    public @NonNull MetricsListResponse convertInternal(@NonNull MetricsList source) {
        return new MetricsListResponse(source.names());
    }
}
