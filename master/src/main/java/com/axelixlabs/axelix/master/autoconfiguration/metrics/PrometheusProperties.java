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

import com.axelixlabs.axelix.common.utils.Assert;

/**
 * The Prometheus related properties that are specific to Axelix Master.
 *
 * @param tags common tags to attach to every metric exposed via the Prometheus endpoint
 * @param port port of the dedicated HTTP server that exposes the Prometheus scrape endpoint. If
 *     not set, defaults to {@code server.port}, meaning Prometheus is served through the actuator
 *     instead of a dedicated server. Must be between {@code 1} and {@code 65535} if set -
 *     {@code 0} is rejected rather than treated as "bind to a random free port".
 * @author Dmitry Mazurov
 */
@ConfigurationProperties(prefix = PrometheusProperties.PROMETHEUS_METRICS_PROPERTIES_PREFIX)
public record PrometheusProperties(
        Map<String, String> tags, @Nullable Integer port) {

    public static final String PROMETHEUS_METRICS_PROPERTIES_PREFIX = "axelix.master.metrics.prometheus";

    public static final String SERVER_PORT_PROPERTY = "server.port";

    public static final String PROMETHEUS_PORT_PROPERTY = PROMETHEUS_METRICS_PROPERTIES_PREFIX + ".port";

    static final int PORT_RANGE_MAX = 65535;

    public PrometheusProperties {
        Assert.isTrue(
                port == null || (port >= 1 && port <= PORT_RANGE_MAX),
                "Prometheus port must be between 1 and 65535 if set. Set " + PROMETHEUS_PORT_PROPERTY
                        + " to a value in that range, or leave it unset to use server.port.");

        if (tags == null) {
            tags = Map.of();
        }
    }
}
