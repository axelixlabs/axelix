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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.OtlpProperties.AXELIX_MASTER_METRICS_OTLP_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that OTLP metrics export is disabled by default.
 *
 * @author Aleksei Ermakov
 */
@SpringBootTest
class OtlpMetricsExportDisabledTest {

    private static final MockWebServer OTLP_COLLECTOR = startCollector();

    @Autowired
    private ApplicationContext applicationContext;

    @DynamicPropertySource
    static void configureOtlpCollector(DynamicPropertyRegistry registry) {
        registry.add(
                AXELIX_MASTER_METRICS_OTLP_PREFIX + ".url",
                () -> OTLP_COLLECTOR.url("/v1/metrics").toString());
    }

    @AfterAll
    static void shutdownCollector() throws IOException {
        OTLP_COLLECTOR.shutdown();
    }

    @Test // GH-1496
    void shouldDisableOtlpMetricsExportByDefault() throws InterruptedException {
        // given.
        Class<OtlpMeterRegistry> registryType = OtlpMeterRegistry.class;

        // when.
        Map<String, OtlpMeterRegistry> registries = applicationContext.getBeansOfType(registryType);
        RecordedRequest request = OTLP_COLLECTOR.takeRequest(250, TimeUnit.MILLISECONDS);

        // then.
        assertThat(registries).isEmpty();
        assertThat(request).isNull();
    }

    private static MockWebServer startCollector() {
        MockWebServer collector = new MockWebServer();
        try {
            collector.start();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to start the mock OTLP collector", ex);
        }
        return collector;
    }
}
