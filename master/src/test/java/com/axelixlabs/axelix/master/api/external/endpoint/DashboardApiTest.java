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

import java.util.List;
import java.util.Map;
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

import com.axelixlabs.axelix.common.api.LazyLoadingTarget;
import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.CountedLazyLoadingTarget;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOrigin;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOverallStats;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionalKey;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashboardApiTest {

    // language=json
    private static final String EXPECTED_DASHBOARD_JSON_WITH_INSTANCES = """
        {
          "distributions": [
            {
              "softwareComponentName": "SpringBoot",
              "versions": {
                "3.5": 67,
                "2.7": 33
              }
            },
            {
              "softwareComponentName": "SpringFramework",
              "versions": {
                "6.0": 67,
                "5.3": 33
              }
            },
            {
              "softwareComponentName": "Java",
              "versions": {
                "25": 67,
                "17": 33
              }
            },
            {
              "softwareComponentName": "Kotlin",
              "versions": {
                "1.9": 100
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
    private DatabaseHistoricalApplicationSnapshotService historicalApplicationSnapshotService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepare() {
        // clear instanceRegistry before test
        deRegisterAll();

        // Register instances with different versions and statuses
        registry.reload(TestInstanceFactory.create(
                instance1Id,
                "http://example.com/1",
                "test-name",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.2",
                "6.0.2",
                "BellSoft",
                null));

        registry.reload(TestInstanceFactory.create(
                instance2Id,
                "http://example.com/2",
                "test-name",
                Instance.InstanceStatus.UP,
                "25",
                "3.5.1",
                "6.0.1",
                "BellSoft",
                "1.9.0"));

        registry.reload(TestInstanceFactory.create(
                instance3Id,
                "http://example.com/3",
                "test-name",
                Instance.InstanceStatus.DOWN,
                "17",
                "2.7.0",
                "5.3.0",
                "BellSoft",
                null));
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
        registry.reload(TestInstanceFactory.withStatus(unknownInstanceId, Instance.InstanceStatus.UNKNOWN));

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

    @Test
    void shouldReturnJavaDashboardWithGarbageCollectorDistribution() {
        // given.
        historicalApplicationSnapshotService.reloadCurrentStateBulk(List.of(
                metadata("com.example", "service-a", GarbageCollector.G1),
                metadata("com.example", "service-b", GarbageCollector.ZGC)));

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/api/external/dashboard/java", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody())
                .node("garbageCollectorDistribution.G1")
                .isEqualTo(50.0);
        assertThatJson(response.getBody())
                .node("garbageCollectorDistribution.ZGC")
                .isEqualTo(50.0);
    }

    @Test
    void shouldReturnPersistenceDashboardAggregatedAcrossServices() {
        // given one service with an N + 1 problem (plus a single lazy load that does not qualify) and another
        // with in-memory pagination.
        historicalApplicationSnapshotService.reloadCurrentStateBulk(List.of(
                persistenceMetadata(
                        "com.example", "service-a", List.of(nPlusOne("pets", 4), nPlusOne("category", 1)), Map.of()),
                persistenceMetadata("com.example", "service-b", List.of(), Map.of("owner", 3))));

        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/api/external/dashboard/persistence", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody())
                .when(IGNORING_ARRAY_ORDER)
                .node("nPlusOne")
                .isEqualTo("[{\"appName\":\"service-a\",\"size\":1}]");
        assertThatJson(response.getBody())
                .when(IGNORING_ARRAY_ORDER)
                .node("inMemoryPagination")
                .isEqualTo("[{\"appName\":\"service-b\",\"size\":1}]");
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/api/external/dashboard")
    void negativeAuthTests() {}

    private void deRegisterAll() {
        jdbcTemplate.execute("DELETE FROM instances");
        jdbcTemplate.execute("DELETE FROM historical_application_snapshots");
    }

    private static BasicRegistrationMetadata metadata(
            String groupId, String artifactId, GarbageCollector garbageCollector) {
        return TestMetadataFactory.withFeatures(
                groupId, artifactId, false, false, false, false, false, garbageCollector);
    }

    private static BasicRegistrationMetadata persistenceMetadata(
            String groupId,
            String artifactId,
            List<CountedLazyLoadingTarget> lazyLoadingTargets,
            Map<String, Integer> inMemoryPagination) {
        TransactionAggregatedProfile profile = new TransactionAggregatedProfile(
                TransactionOrigin.APPLICATION_DECLARATIVE,
                new TransactionalKey("com.example.OwnerService", "loadOwners"),
                new TransactionOverallStats(1, 10, 5),
                lazyLoadingTargets,
                inMemoryPagination);
        return TestMetadataFactory.withPersistenceInsights(
                groupId, artifactId, new PersistenceInsights(List.of(profile)));
    }

    private static CountedLazyLoadingTarget nPlusOne(String associationPropertyName, int count) {
        return new CountedLazyLoadingTarget(
                new LazyLoadingTarget(String.class.getName(), associationPropertyName), count);
    }
}
