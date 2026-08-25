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

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;

/**
 * Prometheus metrics related auto-configuration.
 *
 * @author Dmitry Mazurov
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@EnableConfigurationProperties(PrometheusProperties.class)
@ConditionalOnProperty(
        prefix = PrometheusProperties.PROMETHEUS_METRICS_PROPERTIES_PREFIX,
        name = "enabled",
        havingValue = "true")
public class PrometheusMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    PrometheusConfig prometheusConfig() {
        return new PrometheusConfig() {

            // For now, it is empty, meaning, PrometheusMeterRegistry will take deafults
            @Override
            public @Nullable String get(String key) {
                return null;
            }
        };
    }

    @Bean
    PrometheusRegistry prometheusRegistry() {
        return new PrometheusRegistry();
    }

    @Bean
    Clock mainClock() {
        return Clock.SYSTEM;
    }

    @Bean
    PrometheusScrapeEndpoint prometheusEndpoint(
            PrometheusRegistry prometheusRegistry, PrometheusConfig prometheusConfig) {
        return new PrometheusScrapeEndpoint(prometheusRegistry, prometheusConfig.prometheusProperties());
    }

    @Bean
    PrometheusMeterRegistry prometheusMeterRegistry(
            PrometheusConfig prometheusConfig,
            PrometheusRegistry prometheusRegistry,
            Clock clock,
            PrometheusProperties prometheusProperties) {

        PrometheusMeterRegistry prometheusMeterRegistry =
                new PrometheusMeterRegistry(prometheusConfig, prometheusRegistry, clock, null);
        addTagsIfNeeded(prometheusProperties, prometheusMeterRegistry);
        return prometheusMeterRegistry;
    }

    private static void addTagsIfNeeded(
            PrometheusProperties prometheusProperties, PrometheusMeterRegistry prometheusMeterRegistry) {
        Map<String, String> tags = prometheusProperties.getTags();

        if (!tags.isEmpty()) {
            Tags commonTags = Tags.of(tags.entrySet().stream()
                    .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                    .toList());

            prometheusMeterRegistry.config().commonTags(commonTags);
        }
    }
}
