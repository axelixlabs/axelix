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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Metrics related auto-configuration.
 *
 * @author Dmitry Mazurov
 */
@AutoConfiguration
@EnableConfigurationProperties(PrometheusProperties.class)
public class MetricsAutoConfiguration {

    public static final String PROMETHEUS_METRICS_PROPERTIES_PREFIX = "axelix.master.metrics.prometheus";

    @Bean
    @ConditionalOnProperty(prefix = PROMETHEUS_METRICS_PROPERTIES_PREFIX, name = "enabled", havingValue = "true")
    public MeterRegistryCustomizer<MeterRegistry> prometheusCommonTagsCustomizer(
            PrometheusProperties prometheusProperties) {
        Map<String, String> tags = prometheusProperties.getTags();
        if (tags.isEmpty()) {
            return _ -> {};
        }

        Tags commonTags = Tags.of(tags.entrySet().stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .toList());

        return registry -> registry.config().commonTags(commonTags);
    }
}
