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

/**
 * How {@code axelix.master.metrics.prometheus.port} relates to {@code server.port}, used by
 * {@link ConditionalOnPrometheusPort} to pick which Prometheus exposure bean to activate.
 *
 * @author Dmitry Mazurov
 */
public enum PrometheusPortMode {

    /**
     * {@code axelix.master.metrics.prometheus.port} is unset or equals {@code server.port}:
     * Prometheus is exposed through the actuator.
     */
    MATCHES_APPLICATION_PORT,

    /**
     * {@code axelix.master.metrics.prometheus.port} is explicitly set to a different port:
     * Prometheus is exposed through a dedicated HTTP server.
     */
    CUSTOM_PORT_CONFIGURED
}
