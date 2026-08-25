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
package com.axelixlabs.axelix.master.autoconfiguration.metrics;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The Prometheus related properties that are specific to Axelix Master.
 *
 * @author Dmitry Mazurov
 */
@ConfigurationProperties(prefix = PrometheusProperties.PROMETHEUS_METRICS_PROPERTIES_PREFIX)
public class PrometheusProperties {

    public static final String PROMETHEUS_METRICS_PROPERTIES_PREFIX = "axelix.master.metrics.prometheus";

    /**
     * Common tags to attach to every metric exposed via the Prometheus actuator endpoint.
     */
    private Map<String, String> tags = Map.of();

    public Map<String, String> getTags() {
        return tags;
    }

    public PrometheusProperties setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
