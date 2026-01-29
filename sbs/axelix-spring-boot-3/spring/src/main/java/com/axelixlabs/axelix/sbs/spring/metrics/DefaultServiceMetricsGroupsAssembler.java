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
package com.axelixlabs.axelix.sbs.spring.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed.MetricsGroup;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;

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
