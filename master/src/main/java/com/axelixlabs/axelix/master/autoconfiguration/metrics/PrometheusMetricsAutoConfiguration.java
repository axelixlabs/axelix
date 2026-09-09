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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.context.annotation.Bean;

import static com.axelixlabs.axelix.master.api.infrastructure.InfrastructureApiPaths.PROMETHEUS_METRICS_SCRAPE_PATH;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.ConditionalOnPrometheusPort.Mode;

/**
 * Prometheus metrics related auto-configuration.
 *
 * @author Dmitry Mazurov
 * @author Mikhail Polivakha
 */
@AutoConfiguration(
        before = {CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class},
        after = MetricsAutoConfiguration.class)
@EnableConfigurationProperties(PrometheusProperties.class)
@ConditionalOnProperty(
        prefix = PrometheusProperties.PROMETHEUS_METRICS_PROPERTIES_PREFIX,
        name = "enabled",
        havingValue = "true")
public class PrometheusMetricsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PrometheusMetricsAutoConfiguration.class);

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
    @ConditionalOnPrometheusPort(Mode.MATCHES_APPLICATION_PORT)
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

    @Bean
    @ConditionalOnPrometheusPort(Mode.CUSTOM_PORT_CONFIGURED)
    HTTPServer prometheusHttpServer(
            PrometheusMeterRegistry prometheusMeterRegistry, PrometheusProperties prometheusProperties)
            throws IOException {
        int port = Objects.requireNonNull(prometheusProperties.port());
        HTTPServer server = HTTPServer.builder()
                .port(port)
                .registry(prometheusMeterRegistry.getPrometheusRegistry())
                .metricsHandlerPath(PROMETHEUS_METRICS_SCRAPE_PATH)
                .registerHealthHandler(false)
                .defaultHandler(new WhitelabelNotFoundHandler())
                .buildAndStart();

        log.info(
                "Prometheus HTTP server started on port {} with context path '{}'",
                server.getPort(),
                PROMETHEUS_METRICS_SCRAPE_PATH);

        return server;
    }

    private static void addTagsIfNeeded(
            PrometheusProperties prometheusProperties, PrometheusMeterRegistry prometheusMeterRegistry) {
        Map<String, String> tags = prometheusProperties.tags();

        if (!tags.isEmpty()) {
            Tags commonTags = Tags.of(tags.entrySet().stream()
                    .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                    .toList());

            prometheusMeterRegistry.config().commonTags(commonTags);
        }
    }
}
