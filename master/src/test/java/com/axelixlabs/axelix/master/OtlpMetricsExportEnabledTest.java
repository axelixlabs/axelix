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
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.axelixlabs.axelix.master.autoconfiguration.metrics.AxelixOtlpMetricsEnvironmentPostProcessor.AXELIX_MASTER_METRICS_OTLP_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that Master can export metrics to an OTLP HTTP collector.
 *
 * @author Aleksei Ermakov
 */
@SpringBootTest(
        properties = {
            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".enabled=true",
            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".step=100ms",
            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".compression-mode=none",
            AXELIX_MASTER_METRICS_OTLP_PREFIX + ".headers.X-Axelix-Test=expected"
        })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OtlpMetricsExportEnabledTest {

    private static final String TEST_METRIC_NAME = "axelix.otlp.export.test";
    private static final MockWebServer OTLP_COLLECTOR = startCollector();

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private OtlpMeterRegistry otlpMeterRegistry;

    @DynamicPropertySource
    static void configureOtlpCollector(DynamicPropertyRegistry registry) {
        registry.add(
                AXELIX_MASTER_METRICS_OTLP_PREFIX + ".url",
                () -> OTLP_COLLECTOR.url("/v1/metrics").toString());
    }

    @AfterAll
    void shutdownCollector() throws IOException {
        otlpMeterRegistry.close();
        OTLP_COLLECTOR.shutdown();
    }

    @Test // GH-1496
    void shouldExportMetricsToOtlpCollector() throws IOException, InterruptedException {
        // given.
        meterRegistry.counter(TEST_METRIC_NAME).increment();

        // when.
        RecordedRequest request = awaitRequestContainingMetric();

        // then.
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v1/metrics");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/x-protobuf");
        assertThat(request.getHeader("X-Axelix-Test")).isEqualTo("expected");
        assertThat(request.getBodySize()).isPositive();
    }

    private RecordedRequest awaitRequestContainingMetric() throws IOException, InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        ByteString metricName = ByteString.encodeUtf8(TEST_METRIC_NAME);

        while (System.nanoTime() < deadline) {
            long remainingNanos = deadline - System.nanoTime();
            RecordedRequest request = OTLP_COLLECTOR.takeRequest(remainingNanos, TimeUnit.NANOSECONDS);
            if (request == null || request.getBody().indexOf(metricName) >= 0) {
                return request;
            }
        }
        return null;
    }

    private static MockWebServer startCollector() {
        MockWebServer collector = new MockWebServer();
        collector.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                return new MockResponse().setResponseCode(200);
            }
        });
        try {
            collector.start();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to start the mock OTLP collector", ex);
        }
        return collector;
    }
}
