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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.TestSocketUtils;

import static com.axelixlabs.axelix.master.api.infrastructure.InfrastructureApiPaths.PROMETHEUS_METRICS_SCRAPE_PATH;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.PROMETHEUS_PORT_PROPERTY;
import static com.axelixlabs.axelix.master.autoconfiguration.metrics.PrometheusProperties.SERVER_PORT_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that Prometheus metrics are exposed differently depending on
 * {@code axelix.master.metrics.prometheus.port}: through the actuator when it matches
 * {@code server.port} or is not set at all, or through a dedicated HTTP server on its own port
 * otherwise - and not exposed at all when {@code axelix.master.metrics.prometheus.enabled} is
 * {@code false}.
 *
 * @author Dmitry Mazurov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ActuatorPrometheusEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Nested
    @TestPropertySource(
            properties = {
                "axelix.master.metrics.prometheus.enabled=true",
                "axelix.master.metrics.prometheus.tags.region=eu-west-1"
            })
    class WhenPortDiffersFromServerPort {

        @DynamicPropertySource
        static void configurePrometheusPort(DynamicPropertyRegistry registry) {
            registry.add(PROMETHEUS_PORT_PROPERTY, TestSocketUtils::findAvailableTcpPort);
        }

        @Autowired
        private HTTPServer prometheusHttpServer;

        @Test // GH-1520
        void prometheusIsNotExposedThroughActuator() {
            // when.
            ResponseEntity<String> response = restTemplate.getForEntity(PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test // GH-1520
        void prometheusMetricsAreServedOnDedicatedHttpServer() {
            // when.
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "http://localhost:" + prometheusHttpServer.getPort() + PROMETHEUS_METRICS_SCRAPE_PATH,
                    String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            String body = response.getBody();
            assertThat(body).isNotBlank();
            assertThat(body).contains("# HELP").contains("# TYPE");
            assertThat(body).contains("jvm_memory_used_bytes");
            assertThat(body).contains("region=\"eu-west-1\"");
        }

        @Test // GH-1520
        void unmatchedPathsReturnNotFoundInsteadOfLandingPage() {
            // when.
            ResponseEntity<String> healthy = restTemplate.getForEntity(
                    "http://localhost:" + prometheusHttpServer.getPort() + "/-/healthy", String.class);
            ResponseEntity<String> root =
                    restTemplate.getForEntity("http://localhost:" + prometheusHttpServer.getPort() + "/", String.class);

            // then.
            assertThat(healthy.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(root.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @TestPropertySource(
            properties = {
                "axelix.master.metrics.prometheus.enabled=true",
                "axelix.master.metrics.prometheus.tags.region=eu-west-1"
            })
    class WhenPortNotConfigured {

        @Autowired(required = false)
        private HTTPServer prometheusHttpServer;

        @Test // GH-1520
        void dedicatedHttpServerIsNotStarted() {
            // then.
            assertThat(prometheusHttpServer).isNull();
        }

        @Test // GH-1520
        void prometheusIsServedThroughActuator() {
            // when.
            ResponseEntity<String> response = restTemplate.getForEntity(PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            String body = response.getBody();
            assertThat(body).isNotBlank();
            assertThat(body).contains("# HELP").contains("# TYPE");
            assertThat(body).contains("jvm_memory_used_bytes");
            assertThat(body).contains("region=\"eu-west-1\"");
        }

        @Test // GH-1520
        void doesNotCreateFallbackSimpleMeterRegistry() {
            // then.
            assertThat(applicationContext.getBeansOfType(SimpleMeterRegistry.class))
                    .isEmpty();
        }
    }

    @Nested
    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
            properties = {
                "axelix.master.metrics.prometheus.enabled=true",
                "axelix.master.metrics.prometheus.tags.region=eu-west-1"
            })
    class WhenPortMatchesServerPort {

        @DynamicPropertySource
        static void configurePorts(DynamicPropertyRegistry registry) {
            int port = TestSocketUtils.findAvailableTcpPort();
            registry.add(SERVER_PORT_PROPERTY, () -> port);
            registry.add(PROMETHEUS_PORT_PROPERTY, () -> port);
        }

        @Autowired(required = false)
        private HTTPServer prometheusHttpServer;

        @Test // GH-1520
        void dedicatedHttpServerIsNotStarted() {
            // then.
            assertThat(prometheusHttpServer).isNull();
        }

        @Test // GH-1520
        void prometheusIsServedThroughActuator() {
            // when.
            ResponseEntity<String> response = restTemplate.getForEntity(PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            String body = response.getBody();
            assertThat(body).isNotBlank();
            assertThat(body).contains("# HELP").contains("# TYPE");
            assertThat(body).contains("jvm_memory_used_bytes");
            assertThat(body).contains("region=\"eu-west-1\"");
        }
    }

    @Nested
    @TestPropertySource(properties = {"axelix.master.metrics.prometheus.enabled=false"})
    class WhenDisabled {

        @Autowired(required = false)
        private HTTPServer prometheusHttpServer;

        @Test // GH-1520
        void prometheusHttpServerIsNotStarted() {
            // then.
            assertThat(prometheusHttpServer).isNull();
        }

        @Test // GH-1520
        void prometheusIsNotAvailableThroughActuator() {
            // when.
            ResponseEntity<String> response = restTemplate.getForEntity(PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class WhenNotConfigured {

        @Test
        void prometheusIsNotAvailableByDefault() {
            // when.
            ResponseEntity<String> response = restTemplate.getForEntity(PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
