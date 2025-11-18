package com.nucleonforge.axile.master.service.convert.metrics;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricProfileByTags;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileByTagsResponse;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileByTagsResponse.AvailableTag;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileByTagsResponse.Measurement;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * Converter from {@link MetricProfileByTags} to {@link SingleMetricProfileByTagsResponse}.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Component
public class SingleMetricByTagsConverter implements Converter<MetricProfileByTags, SingleMetricProfileByTagsResponse> {

    @Override
    public @NonNull SingleMetricProfileByTagsResponse convertInternal(@NonNull MetricProfileByTags source) {
        return new SingleMetricProfileByTagsResponse(
                source.name(), source.description(), source.baseUnit(), mapMeasurements(source), mapTags(source));
    }

    private static List<AvailableTag> mapTags(MetricProfileByTags source) {
        return source.availableTags().stream()
                .map(it -> new AvailableTag(it.tag(), it.values()))
                .toList();
    }

    private static List<Measurement> mapMeasurements(MetricProfileByTags source) {
        return source.measurements().stream()
                .map(it -> new Measurement(it.statistic(), it.value()))
                .toList();
    }
}
