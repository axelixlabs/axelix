package com.nucleonforge.axile.master.service.convert.response.metrics;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricProfile;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileResponse;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileResponse.Measurement;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * Converter from {@link MetricProfile} to {@link SingleMetricProfileResponse}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class SingleMetricConverter implements Converter<MetricProfile, SingleMetricProfileResponse> {

    @Override
    public @NonNull SingleMetricProfileResponse convertInternal(@NonNull MetricProfile source) {
        return new SingleMetricProfileResponse(
                source.name(),
                source.description(),
                source.baseUnit(),
                mapMeasurements(source),
                source.validTagCombinations());
    }

    private static List<Measurement> mapMeasurements(MetricProfile source) {
        return source.measurements().stream()
                .map(it -> new Measurement(it.statistic(), it.value()))
                .toList();
    }
}
