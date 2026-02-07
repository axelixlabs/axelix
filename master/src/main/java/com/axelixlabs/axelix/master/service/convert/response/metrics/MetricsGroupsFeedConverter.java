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
package com.axelixlabs.axelix.master.service.convert.response.metrics;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.master.api.external.response.metrics.MetricsGroupsFeedResponse;
import com.axelixlabs.axelix.master.api.external.response.metrics.MetricsGroupsFeedResponse.MetricsGroup;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * Converter from the {@link MetricsGroupsFeed} to the {@link MetricsGroupsFeedResponse}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class MetricsGroupsFeedConverter implements Converter<MetricsGroupsFeed, MetricsGroupsFeedResponse> {

    @Override
    public @NonNull MetricsGroupsFeedResponse convertInternal(@NonNull MetricsGroupsFeed source) {
        List<MetricsGroup> metricsGroups = source.getMetricsGroups().stream()
                .map(metricsGroup -> new MetricsGroup(
                        metricsGroup.getGroupName(), convertMetricDescription(metricsGroup.getMetrics())))
                .toList();

        return new MetricsGroupsFeedResponse(metricsGroups);
    }

    private List<MetricsGroupsFeedResponse.MetricsGroup.MetricDescription> convertMetricDescription(
            List<MetricsGroupsFeed.MetricsGroup.MetricDescription> metrics) {
        return metrics.stream()
                .map(metric -> new MetricsGroup.MetricDescription(metric.getMetricName(), metric.getDescription()))
                .toList();
    }
}
