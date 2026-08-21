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

import java.time.Duration;
import java.util.Map;

import io.micrometer.registry.otlp.CompressionMode;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The OTLP metrics export properties that are specific to Axelix Master.
 *
 * @author Aleksei Ermakov
 */
@ConfigurationProperties(prefix = OtlpProperties.AXELIX_MASTER_METRICS_OTLP_PREFIX)
public record OtlpProperties(
        /** Whether OTLP metrics export is enabled. */
        @DefaultValue("false") boolean enabled,
        /** Complete URL of the OTLP HTTP/protobuf metrics endpoint. */
        @DefaultValue("http://localhost:4318/v1/metrics") String url,
        /** Interval between metric exports. */
        @DefaultValue("1m") Duration step,
        /** Headers to add to every export request. */
        @DefaultValue Map<String, String> headers,
        /** Compression mode to use for export requests. */
        @DefaultValue("none") CompressionMode compressionMode) {

    public static final String AXELIX_MASTER_METRICS_OTLP_PREFIX = "axelix.master.metrics.otlp";
}
