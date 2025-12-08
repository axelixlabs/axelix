/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.convert.response.metrics;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsFeedResponse;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsFeedResponse.MetricsGroup;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * Converter from the {@link MetricsGroupsFeed} to the {@link MetricsGroupsFeedResponse}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class MetricsGroupsFeedConverter implements Converter<MetricsGroupsFeed, MetricsGroupsFeedResponse> {

    @Override
    public @NonNull MetricsGroupsFeedResponse convertInternal(@NonNull MetricsGroupsFeed source) {
        List<MetricsGroup> metricsGroups = source.metricsGroups().stream()
                .map(metricsGroup ->
                        new MetricsGroup(metricsGroup.groupName(), convertMetricDescription(metricsGroup.metrics())))
                .toList();

        return new MetricsGroupsFeedResponse(metricsGroups);
    }

    private List<MetricsGroupsFeedResponse.MetricsGroup.MetricDescription> convertMetricDescription(
            List<MetricsGroupsFeed.MetricsGroup.MetricDescription> metrics) {
        return metrics.stream()
                .map(metric -> new MetricsGroup.MetricDescription(metric.metricName(), metric.description()))
                .toList();
    }
}
