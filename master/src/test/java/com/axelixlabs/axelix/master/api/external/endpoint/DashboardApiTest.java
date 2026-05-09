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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DashboardApi}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashboardApiTest {

    // language=json
    private static final String EXPECTED_DASHBOARD_JSON_WITH_INSTANCES = """
        {
          "distributions": [
            {
              "softwareComponentName": "SpringBoot",
              "versions": {
                "3.5": 2,
                "2.7": 1
              }
            },
            {
              "softwareComponentName": "SpringFramework",
              "versions": {
                "6.0": 2,
                "5.3": 1
              }
            },
            {
              "softwareComponentName": "Java",
              "versions": {
                "25": 2,
                "17": 1
              }
            },
            {
              "softwareComponentName": "Kotlin",
              "versions": {
                "1.9": 1
              }
            }
          ],
          "healthStatus": {
            "statuses": {
              "UP": 2,
              "DOWN": 1
            }
          },
          "memoryUsage": {
            "averageHeapSize": {
              "unit": "bytes",
              "value": 1000.0
            },
            "totalHeapSize": {
              "unit": "KB",
              "value": 2.93
            }
          }
        }
        """;

    // language=json
    private static final String EXPECTED_DASHBOARD_JSON_EMPTY = """
        {
          "distributions": [
            {
              "softwareComponentName": "SpringBoot",
              "versions": {}
            },
            {
              "softwareComponentName": "SpringFramework",
              "versions": {}
            },
            {
              "softwareComponentName": "Java",
              "versions": {}
            },
            {
              "softwareComponentName": "Kotlin",
              "versions": {}
            }
          ],
          "healthStatus": {
            "statuses": {}
          },
          "memoryUsage": {
            "averageHeapSize": {
              "unit": "bytes",
              "value": -1.0
            },
            "totalHeapSize": {
              "unit": "bytes",
              "value": 0.0
            }
          }
        }
        """;

    private static final String instance1Id = UUID.randomUUID().toString();
    private static final String instance2Id = UUID.randomUUID().toString();
    private static final String instance3Id = UUID.randomUUID().toString();

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepare() {
        // clear instanceRegistry before test
        deRegisterAll();

        // Register instances with different versions and statuses
        registry.register(TestObjectFactory.createInstance(
                instance1Id,
                "http://example.com/1",
                "test-name",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.2",
                "6.0.2",
                "BellSoft",
                null,
                Instance.VmFeatures.empty()));

        registry.register(TestObjectFactory.createInstance(
                instance2Id,
                "http://example.com/2",
                "test-name",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.1",
                "6.0.1",
                "BellSoft",
                "1.9.0",
                Instance.VmFeatures.empty()));

        registry.register(TestObjectFactory.createInstance(
                instance3Id,
                "http://example.com/3",
                "test-name",
                Instance.InstanceStatus.DOWN,
                "17",
                "2.7.0",
                "5.3.0",
                "BellSoft",
                null,
                Instance.VmFeatures.empty()));
    }

    @AfterEach
    void cleanup() {
        deRegisterAll();
    }

    @Test
    void shouldReturnJSONDashboardResponse() {
        // when.
        ResponseEntity<String> response = restTemplate.asViewer().getForEntity("/api/external/dashboard", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DASHBOARD_JSON_WITH_INSTANCES);
    }

    @Test
    void shouldReturnJSONDashboardResponseWithEmptyRegistry() {
        // given.
        deRegisterAll();

        // when.
        ResponseEntity<String> response = restTemplate.asViewer().getForEntity("/api/external/dashboard", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DASHBOARD_JSON_EMPTY);
    }

    @Test
    @DisplayName("Should return dashboard with UNKNOWN status instances")
    void shouldReturnDashboardWithUnknownStatusInstances() {
        // given.
        String unknownInstanceId = UUID.randomUUID().toString();
        registry.register(TestObjectFactory.withStatus(unknownInstanceId, Instance.InstanceStatus.UNKNOWN));

        try {
            // when.
            ResponseEntity<String> response =
                    restTemplate.asViewer().getForEntity("/api/external/dashboard", String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThatJson(response.getBody())
                    .node("healthStatus.statuses.UNKNOWN")
                    .isPresent();
        } finally {
            registry.deRegister(InstanceId.of(unknownInstanceId));
        }
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/api/external/dashboard")
    void negativeAuthTests() {}

    private void deRegisterAll() {
        jdbcTemplate.execute("DELETE FROM instances");
    }
}
