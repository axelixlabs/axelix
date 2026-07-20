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
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.ExecutionStats;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOrigin;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionalKey;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.Instance;
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
 * Integration tests for {@link TransactionMonitoringApi}.
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionMonitoringApiTest {

    private static final String EXPECTED_PERSISTENCE_INSIGHTS_JSON =
            // language=json
            """
            {
              "transactions": [
                {
                  "transactionOrigin": "APPLICATION_DECLARATIVE",
                  "transactionalKey": {
                    "className": "com.example.OwnerService",
                    "methodName": "saveOwner"
                  },
                  "transactionOverallStats": {
                    "minMs": 1,
                    "maxMs": 10,
                    "averageMs": 5
                  },
                  "lazyLoadingTargets": [],
                  "inMemoryPagination": {
                    "com.example.Pet": 2
                  },
                  "externalCalls": []
                }
              ]
            }
            """;

    private static final String EXPECTED_EMPTY_PERSISTENCE_INSIGHTS_JSON =
            // language=json
            """
            {
              "transactions": []
            }
            """;

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static final String groupId = "org.springframework.samples";

    private static final String artifactId = "petclinic";

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private DatabaseHistoricalApplicationSnapshotService historicalApplicationSnapshotService;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        jdbcAggregateTemplate.deleteAll(Instance.class);
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
    }

    @Test
    void shouldReturnPersistenceInsights() {
        // given.
        TransactionAggregatedProfile profile = new TransactionAggregatedProfile(
                TransactionOrigin.APPLICATION_DECLARATIVE,
                new TransactionalKey("com.example.OwnerService", "saveOwner"),
                new ExecutionStats(1, 10, 5),
                List.of(),
                Map.of("com.example.Pet", 2),
                List.of());
        BasicRegistrationMetadata metadata = TestMetadataFactory.withPersistenceInsights(
                groupId, artifactId, new PersistenceInsights(List.of(profile)));
        registry.reload(TestInstanceFactory.create(activeInstanceId, groupId, artifactId));
        historicalApplicationSnapshotService.reloadCurrentState(metadata);

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/transaction-monitoring/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_PERSISTENCE_INSIGHTS_JSON);
    }

    @Test
    void shouldReturnEmptyPersistenceInsights() {
        // given.
        registry.reload(TestInstanceFactory.create(activeInstanceId, groupId, artifactId));
        historicalApplicationSnapshotService.reloadCurrentState(TestMetadataFactory.create(groupId, artifactId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/transaction-monitoring/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).isEqualTo(EXPECTED_EMPTY_PERSISTENCE_INSIGHTS_JSON);
    }

    @Test
    void shouldReturnEmptyPersistenceInsightsWhenNoSnapshotExists() {
        // given.
        registry.reload(TestInstanceFactory.create(activeInstanceId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/transaction-monitoring/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnGetTransactionFeed() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/transaction-monitoring/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ProtectedEndpointTests(
            method = HttpMethod.GET,
            path = "/api/external/transaction-monitoring/00000000-0000-0000-0000-000000000001")
    void negativeAuthTestsOnGetTransactionFeed() {}
}
