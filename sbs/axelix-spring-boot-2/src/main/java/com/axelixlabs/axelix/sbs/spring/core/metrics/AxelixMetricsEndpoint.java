/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.boot.actuate.metrics.MetricsEndpoint.MetricResponse;
import org.springframework.lang.Nullable;

import com.axelixlabs.axelix.common.api.metrics.MetricProfile;
import com.axelixlabs.axelix.common.api.metrics.MetricProfile.Measurement;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.units.BaseUnit;

/**
 * Custom Spring Boot Actuator endpoint providing an extended view of the application's environment.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Endpoint(id = "axelix-metrics")
public class AxelixMetricsEndpoint {

    private final MetricsEndpoint delegate;
    private final MeterRegistry registry;
    private final ServiceMetricsGroupsAssembler defaultMetricsGroupsAssembler;
    private final Map<BaseUnit, BaseUnitValueTransformer> baseUnitValueTransformers;
    private final BaseUnitParser baseUnitParser;

    private final Set<Statistic> ACTUAL_VALUE_STATISTICS = Set.of(Statistic.VALUE, Statistic.TOTAL, Statistic.COUNT);

    public AxelixMetricsEndpoint(
            MeterRegistry registry,
            BaseUnitParser baseUnitParser,
            ServiceMetricsGroupsAssembler defaultMetricsGroupsAssembler,
            Set<BaseUnitValueTransformer> baseUnitValueTransformers) {
        this.delegate = new MetricsEndpoint(registry);
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
        MetricResponse originalDescriptor = delegate.metric(requiredMetricName, tag);

        TransformedMeasurements measurements = getMeasurements(originalDescriptor.getBaseUnit(), originalDescriptor);

        return new MetricProfile(
                originalDescriptor.getName(),
                originalDescriptor.getDescription(),
                measurements.baseUnit(),
                measurements.measurements(),
                getValidTagCombinations(requiredMetricName));
    }

    private TransformedMeasurements getMeasurements(String baseUnit, MetricResponse originalDescriptor) {

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

    public static final class TransformedMeasurements {
        private final String baseUnit;
        private final List<Measurement> measurements;

        public TransformedMeasurements(String baseUnit, List<Measurement> measurements) {
            this.baseUnit = baseUnit;
            this.measurements = measurements;
        }

        public String baseUnit() {
            return baseUnit;
        }

        public List<Measurement> measurements() {
            return measurements;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TransformedMeasurements) obj;
            return Objects.equals(this.baseUnit, that.baseUnit) && Objects.equals(this.measurements, that.measurements);
        }

        @Override
        public int hashCode() {
            return Objects.hash(baseUnit, measurements);
        }

        @Override
        public String toString() {
            return "TransformedMeasurements[" + "baseUnit=" + baseUnit + ", " + "measurements=" + measurements + ']';
        }
    }

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
