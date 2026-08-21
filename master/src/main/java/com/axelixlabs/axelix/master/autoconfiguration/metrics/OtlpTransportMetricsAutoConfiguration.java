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

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.CompressionMode;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.OtlpProperties.AXELIX_MASTER_METRICS_OTLP_PREFIX;

/**
 * OTLP transport metrics export auto-configuration.
 *
 * <p>Configures a {@link OtlpMeterRegistry} that pushes metrics to an OTLP HTTP/protobuf endpoint
 * from the stable {@code axelix.master.metrics.otlp.*} contract, instead of relying on Spring Boot's
 * own OTLP metrics export auto-configuration.
 *
 * @author Aleksei Ermakov
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@EnableConfigurationProperties(OtlpProperties.class)
@ConditionalOnProperty(prefix = AXELIX_MASTER_METRICS_OTLP_PREFIX, name = "enabled", havingValue = "true")
public class OtlpTransportMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    OtlpConfig otlpConfig(OtlpProperties properties) {
        return new OtlpConfig() {
            @SuppressWarnings("NullAway") // Micrometer uses null to fall back to its default values.
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String url() {
                return properties.url();
            }

            @Override
            public Duration step() {
                return properties.step();
            }

            @Override
            public Map<String, String> headers() {
                return properties.headers();
            }

            @Override
            public CompressionMode compressionMode() {
                return properties.compressionMode();
            }
        };
    }

    @Bean
    OtlpMeterRegistry otlpMeterRegistry(OtlpConfig otlpConfig, Clock clock) {
        return OtlpMeterRegistry.builder(otlpConfig).clock(clock).build();
    }
}
