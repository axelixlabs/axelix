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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Integration tests for {@link TransactionMonitoringApi}.
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionMonitoringApiTest {

    private static final String ACTUAL_AND_EXPECTED_TRANSACTION_MONITORING_JSON =
            // language=json
            """
      {
        "entrypoints": [
            {
                "className": "org.springframework.samples.petclinic.monitoring.PropagationTestHelper",
                "methodName": "testCorrectSelfInvocation",
                "executions": [
                    {
                        "startTimestampMs": 1769442500006,
                        "endTimestampMs": 1769442500009,
                        "queries": []
                    },
                    {
                        "startTimestampMs": 1769442510007,
                        "endTimestampMs": 1769442510009,
                        "queries": []
                    }
                ],
                "executionStats": {
                    "averageDurationMs": 3,
                    "maxDurationMs": 3,
                    "medianDurationMs": 3
                }
            },
            {
                "className": "org.springframework.samples.petclinic.owner.OwnerRepository",
                "methodName": "findByLastName",
                "executions": [
                    {
                        "startTimestampMs": 1769442500006,
                        "endTimestampMs": 1769442500007,
                        "queries": [
                            {
                                "sql": "select o from Owner o where o.lastName = ?",
                                "startTimestampMs": 1769442500006,
                                "endTimestampMs": 1769442500007
                            }
                        ]
                    },
                    {
                        "startTimestampMs": 1769442510007,
                        "endTimestampMs": 1769442510010,
                        "queries": [
                            {
                                "sql": "select o from Owner o where o.lastName = ?",
                                "startTimestampMs": 1769442510007,
                                "endTimestampMs": 1769442510010
                            }
                        ]
                    }
                ],
                "executionStats": {
                    "averageDurationMs": 2,
                    "maxDurationMs": 3,
                    "medianDurationMs": 2
                }
            }
        ]
    }
    """;

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-transactions-monitoring")
                        && request.getMethod().equals("GET")) {
                    return new MockResponse()
                            .setBody(ACTUAL_AND_EXPECTED_TRANSACTION_MONITORING_JSON)
                            .addHeader("Content-Type", APPLICATION_JSON_VALUE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-transactions-monitoring")
                        && request.getMethod().equals("DELETE")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
        registry.register(
                TestObjectFactory.withUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONTransactionsMonitoringFeed() throws InterruptedException {
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/transaction-monitoring/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody())
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(ACTUAL_AND_EXPECTED_TRANSACTION_MONITORING_JSON);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + activeInstanceId + "/actuator/axelix-transactions-monitoring");
    }

    @Test
    void shouldClearTransactionsMonitoringStats() throws InterruptedException {
        // when.
        restTemplate
                .asViewer()
                .delete("/api/external/transaction-monitoring/{instanceId}", Map.of("instanceId", activeInstanceId));

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest(10l, TimeUnit.SECONDS);
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + activeInstanceId + "/actuator/axelix-transactions-monitoring");
    }

    @Test
    void shouldReturnInternalServerError_OnGetTransactionFeed() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/transaction-monitoring/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnInternalServerError_OnClearTransactionStats() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .exchange(
                        RequestEntity.delete(URI.create("/api/external/transaction-monitoring/" + instanceId))
                                .build(),
                        String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
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

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnClearTransactionStats() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .exchange(
                        RequestEntity.delete(URI.create("/api/external/transaction-monitoring/" + instanceId))
                                .build(),
                        String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ProtectedEndpointTests(
            method = HttpMethod.GET,
            path = "/api/external/transaction-monitoring/00000000-0000-0000-0000-000000000001")
    void negativeAuthTestsOnGetTransactionFeed() {}

    @ProtectedEndpointTests(
            method = HttpMethod.DELETE,
            path = "/api/external/transaction-monitoring/00000000-0000-0000-0000-000000000001")
    void negativeAuthTestsOnClearTransactionStats() {}
}
