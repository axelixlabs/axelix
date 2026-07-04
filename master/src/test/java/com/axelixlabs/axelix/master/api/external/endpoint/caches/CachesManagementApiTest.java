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
package com.axelixlabs.axelix.master.api.external.endpoint.caches;

import java.io.IOException;
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

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesManagementApi}
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

        registry.reload(
                TestInstanceFactory.create(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
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
                .asEditor()
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
                .asEditor()
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
        registry.reload(TestInstanceFactory.create(instanceId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity(
                        "/api/external/caches/{instanceId}/{cacheManagerName}/{cacheName}/" + cacheStatus,
                        null,
                        String.class,
                        Map.of("instanceId", instanceId, "cacheManagerName", "unknown", "cacheName", "unknown"));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldReturnBadRequestForUnregisteredInstance_OnEnableOrDisableCacheName(String cacheStatus) {
        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity(
                        "/api/external/caches/{instanceId}/{cacheManagerName}/{cacheName}/" + cacheStatus,
                        null,
                        String.class,
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

    @ProtectedEndpointTests(
            method = HttpMethod.POST,
            path = "/api/external/caches/00000000-0000-0000-0000-000000000001/cacheManager/vets/enable",
            requiredAuthority = DefaultAuthority.CACHES_TOGGLE)
    void negativeAuthTestsOnEnableCache() {}

    @ProtectedEndpointTests(
            method = HttpMethod.POST,
            path = "/api/external/caches/00000000-0000-0000-0000-000000000001/cacheManager/vets/disable",
            requiredAuthority = DefaultAuthority.CACHES_TOGGLE)
    void negativeAuthTestsOnDisableCache() {}

    private static Stream<Arguments> cacheOperations() {
        return Stream.of("enable", "disable").map(Arguments::of);
    }
}
