package com.nucleonforge.axile.sbs.spring.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint.MetricDescriptor;
import org.springframework.lang.Nullable;

import com.nucleonforge.axile.common.api.metrics.MetricProfile;
import com.nucleonforge.axile.common.api.metrics.MetricProfile.Measurement;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.sbs.spring.metrics.transform.BaseUnitParser;
import com.nucleonforge.axile.sbs.spring.metrics.transform.BaseUnitValueTransformer;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BaseUnit;

/**
 * Custom Spring Boot Actuator endpoint providing an extended view of the application's environment.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Endpoint(id = "axile-metrics")
public class AxileMetricsEndpoint {

    private final MetricsEndpoint delegate;
    private final MeterRegistry registry;
    private final ServiceMetricsGroupsAssembler defaultMetricsGroupsAssembler;
    private final Map<BaseUnit, BaseUnitValueTransformer> baseUnitValueTransformers;
    private final BaseUnitParser baseUnitParser;

    private final Set<Statistic> ACTUAL_VALUE_STATISTICS = Set.of(Statistic.VALUE, Statistic.TOTAL);

    public AxileMetricsEndpoint(
            MetricsEndpoint delegate,
            MeterRegistry registry,
            BaseUnitParser baseUnitParser,
            ServiceMetricsGroupsAssembler defaultMetricsGroupsAssembler,
            Set<BaseUnitValueTransformer> baseUnitValueTransformers) {
        this.delegate = delegate;
        this.registry = registry;
        this.defaultMetricsGroupsAssembler = defaultMetricsGroupsAssembler;
        this.baseUnitParser = baseUnitParser;
        this.baseUnitValueTransformers = baseUnitValueTransformers.stream()
                .collect(Collectors.toMap(BaseUnitValueTransformer::supports, it -> it));
    }

    @ReadOperation
    public MetricsGroupsFeed metricsGroups() {
        return defaultMetricsGroupsAssembler.assemble();
    }

    // IMPORTANT!
    // For Spring Actuator endpoints @Endpoint, we must use org.springframework.lang.Nullable.
    // Spring Boot 3 does not recognize the Jspecify's @Nullable here, but we still need to tell
    // Spring that tags are optional
    @ReadOperation
    public MetricProfile metric(@Selector String requiredMetricName, @Nullable List<String> tag) {
        MetricDescriptor originalDescriptor = delegate.metric(requiredMetricName, tag);

        TransformedMeasurements measurements = getMeasurements(originalDescriptor.getBaseUnit(), originalDescriptor);

        return new MetricProfile(
                originalDescriptor.getName(),
                originalDescriptor.getDescription(),
                measurements.baseUnit(),
                measurements.measurements(),
                getValidTagCombinations(requiredMetricName));
    }

    private TransformedMeasurements getMeasurements(String baseUnit, MetricDescriptor originalDescriptor) {

        BaseUnitValueTransformer baseUnitValueTransformer = baseUnitParser
                .parse(baseUnit)
                .map(baseUnitValueTransformers::get)
                .orElse(null);

        List<Measurement> resultingMeasurements = new ArrayList<>();
        String resultingBaseUnit = baseUnit;

        for (var measurement : originalDescriptor.getMeasurements()) {
            if (ACTUAL_VALUE_STATISTICS.contains(measurement.getStatistic())) {
                if (baseUnitValueTransformer != null) {
                    var transformedMetricValue = baseUnitValueTransformer.transform(measurement.getValue());
                    resultingMeasurements.add(new Measurement(transformedMetricValue.value()));
                    resultingBaseUnit = transformedMetricValue.baseUnit().getDisplayName();
                } else {
                    resultingMeasurements.add(new Measurement(measurement.getValue()));
                }
            }
        }

        return new TransformedMeasurements(resultingBaseUnit, resultingMeasurements);
    }

    record TransformedMeasurements(String baseUnit, List<Measurement> measurements) {}

    private List<Map<String, String>> getValidTagCombinations(String metricName) {
        Collection<Meter> meters = this.registry.find(metricName).meters();
        List<Map<String, String>> allCombinations = new ArrayList<>();

        for (Meter meter : meters) {

            Map<String, String> tagMap = new LinkedHashMap<>();

            for (Tag tag : meter.getId().getTags()) {
                tagMap.put(tag.getKey(), tag.getValue());
            }

            if (!tagMap.isEmpty()) {
                allCombinations.add(tagMap);
            }
        }

        return allCombinations;
    }
}
