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

import com.axelixlabs.axelix.common.utils.Assert;

/**
 * The OTLP metrics export properties that are specific to Axelix Master.
 *
 * @param enabled whether OTLP metrics export is enabled
 * @param url complete URL of the OTLP HTTP/protobuf metrics endpoint
 * @param step interval between metric exports
 * @param headers headers to add to every export request
 * @param compressionMode compression mode to use for export requests
 * @author Aleksei Ermakov
 */
@ConfigurationProperties(prefix = OtlpProperties.AXELIX_MASTER_METRICS_OTLP_PREFIX)
public record OtlpProperties(
        boolean enabled, String url, Duration step, Map<String, String> headers, CompressionMode compressionMode) {

    public OtlpProperties {
        if (enabled) {
            Assert.state(
                    () -> url != null && !url.isBlank(),
                    "OTel-compatible backend url must be supplied when " + AXELIX_MASTER_METRICS_OTLP_PREFIX
                            + ".enabled is 'true'");
        }
    }

    public static final String AXELIX_MASTER_METRICS_OTLP_PREFIX = "axelix.master.metrics.otlp";
}
