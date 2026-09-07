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
package com.axelixlabs.axelix.master;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.otlp.OtlpMetricsExportAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the auto-configurations excluded on {@link Main} are absent from the application context.
 *
 * @author Dmitry Mazurov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MainAutoConfigurationExclusionsTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test // GH-1550
    void excludedAutoConfigurationsAreAbsentFromContext() {
        // given.
        List<Class<?>> excludedAutoConfigurations = List.of(
                CompositeDiscoveryClientAutoConfiguration.class,
                SimpleDiscoveryClientAutoConfiguration.class,
                OtlpMetricsExportAutoConfiguration.class,
                PrometheusMetricsExportAutoConfiguration.class);

        // when.
        List<String> beanNames = excludedAutoConfigurations.stream()
                .map(applicationContext::getBeanNamesForType)
                .flatMap(Arrays::stream)
                .toList();

        // then.
        assertThat(beanNames).isEmpty();
    }
}
