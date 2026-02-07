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
package com.axelixlabs.axelix.master.api.external.response.metrics;

import java.util.List;

import com.axelixlabs.axelix.master.api.external.ApiPaths;

/**
 * Response to the {@link ApiPaths.MetricsApi#MAIN}.
 *
 * @param metricsGroups the list of groups.
 *
 * @author Mikhail Polivakha
 */
public record MetricsGroupsFeedResponse(List<MetricsGroup> metricsGroups) {

    /**
     * Information about the metrics group.
     *
     * @param groupName the name of the group to which the {@link #metrics} belong to.
     * @param metrics the names of the metrics inside teh given group.
     */
    public record MetricsGroup(String groupName, List<MetricDescription> metrics) {

        /**
         * Information about the metric.
         *
         * @param metricName   the name of the metric.
         * @param description  the description of the metrics.
         */
        public record MetricDescription(String metricName, String description) {}
    }
}
