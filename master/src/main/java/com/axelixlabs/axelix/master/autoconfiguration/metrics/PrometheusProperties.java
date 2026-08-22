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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The Prometheus related properties that are specific to Axelix Master.
 *
 * @author Dmitry Mazurov
 */
@ConfigurationProperties(prefix = PrometheusProperties.PROMETHEUS_METRICS_PROPERTIES_PREFIX)
public class PrometheusProperties {

    public static final String PROMETHEUS_METRICS_PROPERTIES_PREFIX = "axelix.master.metrics.prometheus";

    public static final String SERVER_PORT_PROPERTY = "server.port";

    public static final String PROMETHEUS_PORT_PROPERTY = PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".port";

    /**
     * Common tags to attach to every metric exposed via the Prometheus endpoint.
     */
    private Map<String, String> tags = Map.of();

    /**
     * Port of the dedicated HTTP server that exposes the Prometheus scrape endpoint. If not set,
     * defaults to {@code server.port}, meaning Prometheus is served through the actuator instead
     * of a dedicated server.
     */
    @Nullable
    private Integer port;

    public Map<String, String> getTags() {
        return tags;
    }

    public PrometheusProperties setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public @Nullable Integer getPort() {
        return port;
    }

    public PrometheusProperties setPort(@Nullable Integer port) {
        this.port = port;
        return this;
    }
}
