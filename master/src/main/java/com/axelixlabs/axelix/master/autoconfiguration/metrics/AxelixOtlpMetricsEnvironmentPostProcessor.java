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

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Adapts the stable Axelix Master OTLP metrics configuration contract to Spring Boot's internal
 * OTLP metrics export properties.
 *
 * @author Aleksei Ermakov
 */
public class AxelixOtlpMetricsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String AXELIX_MASTER_METRICS_OTLP_PREFIX = "axelix.master.metrics.otlp";

    static final String MANAGEMENT_OTLP_METRICS_EXPORT_PREFIX = "management.otlp.metrics.export";

    private static final String PROPERTY_SOURCE_NAME = "axelixOtlpMetrics";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = environment.getProperty(propertyName("enabled"), Boolean.class, false);
        Map<String, Object> property = Map.of(managementPropertyName("enabled"), enabled);
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, property));
    }

    private static String propertyName(String suffix) {
        return AXELIX_MASTER_METRICS_OTLP_PREFIX + "." + suffix;
    }

    private static String managementPropertyName(String suffix) {
        return MANAGEMENT_OTLP_METRICS_EXPORT_PREFIX + "." + suffix;
    }
}
