/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed.MetricsGroup;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;

/**
 * Default implementation of {@link ServiceMetricsGroupsAssembler}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultServiceMetricsGroupsAssembler implements ServiceMetricsGroupsAssembler {

    public static final String OTHER_GROUP_NAME = "Others";

    private final MeterRegistry registry;

    public DefaultServiceMetricsGroupsAssembler(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public MetricsGroupsFeed assemble() {
        Map<String, List<MetricDescription>> metricsByGroupName =
                listNames().stream().collect(Collectors.groupingBy(metric -> extractGroupName(metric.metricName())));

        List<MetricsGroup> metricsGroup = metricsByGroupName.entrySet().stream()
                .map(entry -> new MetricsGroup(entry.getKey(), entry.getValue()))
                .toList();

        return new MetricsGroupsFeed(metricsGroup);
    }

    private List<MetricDescription> listNames() {
        Map<String, String> metricsNameMapping = new HashMap<>();
        collectMetricNamesAndDescription(metricsNameMapping, this.registry);

        return metricsNameMapping.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new MetricDescription(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void collectMetricNamesAndDescription(Map<String, String> names, MeterRegistry registry) {
        if (registry instanceof CompositeMeterRegistry composite) {
            composite.getRegistries().forEach(member -> collectMetricNamesAndDescription(names, member));
        } else {
            registry.getMeters()
                    .forEach(meter -> names.putIfAbsent(
                            meter.getId().getName(), meter.getId().getDescription()));
        }
    }

    private static String extractGroupName(String it) {
        String[] parts = it.split("\\.");

        if (parts.length == 1) {
            return OTHER_GROUP_NAME;
        }

        return parts[0];
    }
}
