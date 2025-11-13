package com.nucleonforge.axile.master.service.convert.metrics;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricProfile;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileResponse;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileResponse.AvailableTag;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileResponse.Measurement;
import com.nucleonforge.axile.master.service.convert.Converter;

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
                source.name(), source.description(), source.baseUnit(), mapMeasurements(source), mapTags(source));
    }

    private static List<AvailableTag> mapTags(MetricProfile source) {
        return source.availableTags().stream()
                .map(it -> new AvailableTag(it.tag(), it.values()))
                .toList();
    }

    private static List<Measurement> mapMeasurements(MetricProfile source) {
        return source.measurements().stream()
                .map(it -> new Measurement(it.statistic(), it.value()))
                .toList();
    }
}
