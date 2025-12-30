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
package com.nucleonforge.axelix.common.api.metrics;

import java.util.List;

/**
 * The response returned by the custom metric groups list endpoint.
 *
 * @param metricsGroups the list of groups.
 *
 * @author Sergey Cherkasov
 */
public record MetricsGroupsFeed(List<MetricsGroup> metricsGroups) {

    /**
     * DTO that encapsulates information about a metrics group.
     *
     * @param groupName the name of the group to which the {@link #metrics} belong to.
     * @param metrics   the names and descriptions of the metrics inside the given group.
     */
    public record MetricsGroup(String groupName, List<MetricDescription> metrics) {

        /**
         * DTO that encapsulates information about a metric.
         *
         * @param metricName   the name of the metric.
         * @param description  the description of the metrics.
         */
        public record MetricDescription(String metricName, String description) {}
    }
}
