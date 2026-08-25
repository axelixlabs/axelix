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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.api.infrastructure.InfrastructureApiPaths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the actuator prometheus endpoint on the master is available or absent depending on
 * {@code axelix.master.metrics.prometheus.enabled}, without requiring authentication.
 *
 * @author Dmitry Mazurov
 */
class ActuatorPrometheusEndpointTest {

    @Nested
    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {
                "axelix.master.metrics.prometheus.enabled=true",
                "axelix.master.metrics.prometheus.tags.region=eu-west-1"
            })
    @AutoConfigureTestRestTemplate
    class WhenEnabled {

        // The TestRestTemplateBuilder is intentionally not used here, since we do not require any auth to access
        // settings API.
        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void actuatorPrometheusReturnsScrapedMetricsWithoutAuth() {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(InfrastructureApiPaths.PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isNotNull();
            assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_PLAIN))
                    .isTrue();

            String body = response.getBody();
            assertThat(body).isNotBlank();
            assertThat(body).contains("# HELP").contains("# TYPE");
            assertThat(body).contains("jvm_memory_used_bytes");
            assertThat(body).contains("region=\"eu-west-1\"");
        }
    }

    @Nested
    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {"axelix.master.metrics.prometheus.enabled=false"})
    @AutoConfigureTestRestTemplate
    class WhenDisabled {

        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void actuatorPrometheusIsNotAvailableWithoutAuth() {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(InfrastructureApiPaths.PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureTestRestTemplate
    class WhenNotConfigured {

        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void actuatorPrometheusIsNotAvailableByDefault() {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(InfrastructureApiPaths.PROMETHEUS_METRICS_SCRAPE_PATH, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
