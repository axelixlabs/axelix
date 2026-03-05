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
package com.axelixlabs.axelix.master.api;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.DashboardApi;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.MemoryUsageCache;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DashboardApi}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
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
    private MemoryUsageCache memoryUsageCache;

    @BeforeEach
    void prepare() {
        // Register instances with different versions and statuses
        registry.register(TestObjectFactory.createInstance(
                instance1Id,
                "http://example.com/1",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.2",
                "6.0.2",
                "BellSoft",
                null,
                java.util.List.of()));
        memoryUsageCache.putHeapSize(InstanceId.of(instance1Id), 1000.0);

        registry.register(TestObjectFactory.createInstance(
                instance2Id,
                "http://example.com/2",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.1",
                "6.0.1",
                "BellSoft",
                "1.9.0",
                java.util.List.of()));
        memoryUsageCache.putHeapSize(InstanceId.of(instance2Id), 1000.0);

        registry.register(TestObjectFactory.createInstance(
                instance3Id,
                "http://example.com/3",
                Instance.InstanceStatus.DOWN,
                "17",
                "2.7.0",
                "5.3.0",
                "BellSoft",
                null,
                java.util.List.of()));
        memoryUsageCache.putHeapSize(InstanceId.of(instance3Id), 1000.0);
    }

    @AfterEach
    void cleanup() {
        deRegisterAll();
        clearMemoryCache();
    }

    private void clearMemoryCache() {
        memoryUsageCache.clear(InstanceId.of(instance1Id));
        memoryUsageCache.clear(InstanceId.of(instance2Id));
        memoryUsageCache.clear(InstanceId.of(instance3Id));
    }

    @Test
    void shouldReturnJSONDashboardResponse() {
        // when.
        ResponseEntity<String> response =
                restTemplate.withoutAuthorities().getForEntity("/api/external/dashboard", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DASHBOARD_JSON_WITH_INSTANCES);
    }

    @Test
    void shouldReturnJSONDashboardResponseWithEmptyRegistry() {
        // given.
        deRegisterAll();
        clearMemoryCache();

        // when.
        ResponseEntity<String> response =
                restTemplate.withoutAuthorities().getForEntity("/api/external/dashboard", String.class);

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
        registry.register(TestObjectFactory.createInstance(unknownInstanceId, Instance.InstanceStatus.UNKNOWN));
        memoryUsageCache.putHeapSize(InstanceId.of(unknownInstanceId), 1000.0);

        try {
            // when.
            ResponseEntity<String> response =
                    restTemplate.withoutAuthorities().getForEntity("/api/external/dashboard", String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            assertThatJson(response.getBody())
                    .node("healthStatus.statuses.UNKNOWN")
                    .isPresent();
        } finally {
            registry.deRegister(InstanceId.of(unknownInstanceId));
            memoryUsageCache.clear(InstanceId.of(unknownInstanceId));
        }
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response =
                scenario.getModifier().apply(restTemplate).getForEntity("/api/external/dashboard", Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private void deRegisterAll() {
        registry.deRegisterQuietly(InstanceId.of(instance1Id));
        registry.deRegisterQuietly(InstanceId.of(instance2Id));
        registry.deRegisterQuietly(InstanceId.of(instance3Id));
    }
}
