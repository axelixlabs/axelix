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
package com.axelixlabs.axelix.master.api.caches;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.caches.CachesManagementApi;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesManagementApi}
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CachesManagementApiTest {

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
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/vets/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/vets/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/enable-all-cache")) {
                    return new MockResponse().setResponseCode(200);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldEnableOrDisableSpecificCache(String cacheStatus) {
        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/caches/{instanceId}/{cacheManagerName}/{cacheName}/" + cacheStatus,
                        null,
                        Void.class,
                        Map.of(
                                "instanceId",
                                activeInstanceId,
                                "cacheManagerName",
                                "cacheManager",
                                "cacheName",
                                "vets"));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldEnableOrDisableCacheManager(String cacheStatus) {
        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/caches/{instanceId}/{cacheManagerName}/" + cacheStatus,
                        null,
                        Void.class,
                        Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager"));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnEnableOrDisableCacheName(String cacheStatus) {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/caches/{instanceId}/{cacheManagerName}/{cacheName}/" + cacheStatus,
                        null,
                        EndpointInvocationException.class,
                        Map.of("instanceId", instanceId, "cacheManagerName", "unknown", "cacheName", "unknown"));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldReturnBadRequestForUnregisteredInstance_OnEnableOrDisableCacheName(String cacheStatus) {
        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/caches/{instanceId}/{cacheManagerName}/{cacheName}/" + cacheStatus,
                        null,
                        EndpointInvocationException.class,
                        Map.of(
                                "instanceId",
                                UUID.randomUUID().toString(),
                                "cacheManagerName",
                                "cacheManager",
                                "cacheName",
                                "vets"));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("cacheOperationsAndScenarios")
    void shouldReturnUnauthorized_OnAnyInvalidOperationForCache(String operation, InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/api/external/caches/{instanceId}/{cacheManagerName}/{cacheName}/" + operation,
                        null,
                        Void.class,
                        Map.of(
                                "instanceId",
                                activeInstanceId,
                                "cacheManagerName",
                                "cacheManager",
                                "cacheName",
                                "vets"));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static Stream<Arguments> cacheOperations() {
        return Stream.of("enable", "disable").map(Arguments::of);
    }

    private static Stream<Arguments> cacheOperationsAndScenarios() {
        return Stream.of("enable", "disable")
                .flatMap(s -> Arrays.stream(InvalidAuthScenario.values()).map(it -> Arguments.of(s, it)));
    }
}
