package com.nucleonforge.axile.sbs.spring.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint.MetricDescriptor;
import org.springframework.lang.Nullable;

import com.nucleonforge.axile.common.api.metrics.MetricProfile;
import com.nucleonforge.axile.common.api.metrics.MetricProfile.Measurement;

/**
 * Custom Spring Boot Actuator endpoint providing an extended view of the application's environment.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "axile-metrics")
public class AxileMetricsEndpoint {

    private final MetricsEndpoint delegate;
    private final MeterRegistry registry;

    public AxileMetricsEndpoint(MetricsEndpoint delegate, MeterRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    // IMPORTANT! For Spring Actuator endpoints @Endpoint, we must use org.springframework.lang.Nullable.
    @ReadOperation
    public MetricProfile metric(@Selector String requiredMetricName, @Nullable List<String> tag) {
        MetricDescriptor originalDescriptor = delegate.metric(requiredMetricName, tag);

        return new MetricProfile(
                originalDescriptor.getName(),
                originalDescriptor.getDescription(),
                originalDescriptor.getBaseUnit(),
                originalDescriptor.getMeasurements().stream()
                        .map(sample -> new Measurement(sample.getStatistic().name(), sample.getValue()))
                        .toList(),
                getValidTagCombinations(requiredMetricName));
    }

    private List<Map<String, String>> getValidTagCombinations(String metricName) {
        Collection<Meter> meters = this.registry.find(metricName).meters();
        List<Map<String, String>> allCombinations = new ArrayList<>();

        for (Meter meter : meters) {

            Map<String, String> tagMap = new LinkedHashMap<>();

            for (Tag tag : meter.getId().getTags()) {
                tagMap.put(tag.getKey(), tag.getValue());
            }

            allCombinations.add(tagMap);
        }

        return allCombinations;
    }
}
