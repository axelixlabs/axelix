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
package com.axelixlabs.axelix.common.api.metrics;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response returned by the custom metric groups list endpoint.
 *
 * @author Sergey Cherkasov
 */
public final class MetricsGroupsFeed {

    private final List<MetricsGroup> metricsGroups;

    /**
     * Creates a new MetricsGroupsFeed.
     *
     * @param metricsGroups the list of groups.
     */
    @JsonCreator
    public MetricsGroupsFeed(@JsonProperty("metricsGroups") List<MetricsGroup> metricsGroups) {
        this.metricsGroups = metricsGroups;
    }

    public List<MetricsGroup> getMetricsGroups() {
        return metricsGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetricsGroupsFeed that = (MetricsGroupsFeed) o;
        return Objects.equals(metricsGroups, that.metricsGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricsGroups);
    }

    @Override
    public String toString() {
        return "MetricsGroupsFeed{" + "metricsGroups=" + metricsGroups + '}';
    }

    /**
     * DTO that encapsulates information about a metrics group.
     */
    public static final class MetricsGroup {

        private final String groupName;
        private final List<MetricDescription> metrics;

        /**
         * Creates a new MetricsGroup.
         *
         * @param groupName the name of the group to which the {@link #getMetrics()} belong to.
         * @param metrics   the names and descriptions of the metrics inside the given group.
         */
        @JsonCreator
        public MetricsGroup(
                @JsonProperty("groupName") String groupName, @JsonProperty("metrics") List<MetricDescription> metrics) {
            this.groupName = groupName;
            this.metrics = metrics;
        }

        public String getGroupName() {
            return groupName;
        }

        public List<MetricDescription> getMetrics() {
            return metrics;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MetricsGroup that = (MetricsGroup) o;
            return Objects.equals(groupName, that.groupName) && Objects.equals(metrics, that.metrics);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupName, metrics);
        }

        @Override
        public String toString() {
            return "MetricsGroup{" + "groupName='" + groupName + '\'' + ", metrics=" + metrics + '}';
        }

        /**
         * DTO that encapsulates information about a metric.
         */
        public static final class MetricDescription {

            private final String metricName;
            private final String description;

            /**
             * Creates a new MetricDescription.
             *
             * @param metricName  the name of the metric.
             * @param description the description of the metrics.
             */
            @JsonCreator
            public MetricDescription(
                    @JsonProperty("metricName") String metricName, @JsonProperty("description") String description) {
                this.metricName = metricName;
                this.description = description;
            }

            public String getMetricName() {
                return metricName;
            }

            public String getDescription() {
                return description;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                MetricDescription that = (MetricDescription) o;
                return Objects.equals(metricName, that.metricName) && Objects.equals(description, that.description);
            }

            @Override
            public int hashCode() {
                return Objects.hash(metricName, description);
            }

            @Override
            public String toString() {
                return "MetricDescription{"
                        + "metricName='"
                        + metricName
                        + '\''
                        + ", description='"
                        + description
                        + '\''
                        + '}';
            }
        }
    }
}
