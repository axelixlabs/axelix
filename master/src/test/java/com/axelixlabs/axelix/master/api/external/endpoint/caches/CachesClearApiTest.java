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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.api.external.request.caches.CacheBulkClearRequest;
import com.axelixlabs.axelix.master.api.external.request.caches.CachesBulkClearRequest;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesClearApi}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
class CachesClearApiTest extends AbstractProtectedEndpointTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String siblingInstanceId = UUID.randomUUID().toString();
    private static final String failingInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    private List<String> invokedPaths;
    private AtomicInteger failingCacheOperationStatusCode;

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
        invokedPaths = new CopyOnWriteArrayList<>();
        failingCacheOperationStatusCode = new AtomicInteger(500);
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;
                invokedPaths.add(path);

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/clear")
                        || path.equals("/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/clear")) {
                    return new MockResponse();
                }
                if (path.equals("/" + siblingInstanceId + "/actuator/axelix-caches/clear")
                        || path.equals("/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/clear")) {
                    return new MockResponse();
                }
                if (path.equals("/" + failingInstanceId + "/actuator/axelix-caches/clear")
                        || path.equals("/" + failingInstanceId + "/actuator/axelix-caches/manager/orders/clear")) {
                    return new MockResponse().setResponseCode(failingCacheOperationStatusCode.get());
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        registry.register(
                TestInstanceFactory.create(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
        registry.register(
                TestInstanceFactory.create(siblingInstanceId, mockWebServer.url(siblingInstanceId) + "/actuator"));
        registry.register(
                TestInstanceFactory.create(failingInstanceId, mockWebServer.url(failingInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
        registry.deRegister(InstanceId.of(siblingInstanceId));
        registry.deRegister(InstanceId.of(failingInstanceId));
    }

    @Nested
    class ClearAllCaches {

        @Test
        void shouldClearAcrossInstances() {
            // given.
            CachesBulkClearRequest requestBody =
                    new CachesBulkClearRequest(List.of(activeInstanceId, siblingInstanceId));
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<String> response = editor.exchange(
                    "/api/external/caches/clear", HttpMethod.DELETE, new HttpEntity<>(requestBody), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + activeInstanceId + "/actuator/axelix-caches/clear",
                            "/" + siblingInstanceId + "/actuator/axelix-caches/clear");
            assertSuccessfulCallback(MasterWebEndpoints.CACHES_CLEAR_ALL, editor.getActor());
        }

        @Test
        void shouldClearOnSingleInstance() {
            // given.
            CachesBulkClearRequest requestBody = new CachesBulkClearRequest(List.of(activeInstanceId));
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<String> response = editor.exchange(
                    "/api/external/caches/clear", HttpMethod.DELETE, new HttpEntity<>(requestBody), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder("/" + activeInstanceId + "/actuator/axelix-caches/clear");
            assertSuccessfulCallback(MasterWebEndpoints.CACHES_CLEAR_ALL, editor.getActor());
        }

        @Test
        void shouldClearWithBlankAndDuplicatedInstanceIds() {
            // given.
            CachesBulkClearRequest requestBody =
                    new CachesBulkClearRequest(List.of("", " ", activeInstanceId, activeInstanceId, siblingInstanceId));
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<String> response = editor.exchange(
                    "/api/external/caches/clear", HttpMethod.DELETE, new HttpEntity<>(requestBody), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + activeInstanceId + "/actuator/axelix-caches/clear",
                            "/" + siblingInstanceId + "/actuator/axelix-caches/clear");
            assertSuccessfulCallback(MasterWebEndpoints.CACHES_CLEAR_ALL, editor.getActor());
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 500})
        void shouldReturnBadRequestAndInvokeRemainingInstances_WhenOneInstanceFails(int failureStatusCode) {
            // given.
            failingCacheOperationStatusCode.set(failureStatusCode);
            CachesBulkClearRequest requestBody =
                    new CachesBulkClearRequest(List.of(failingInstanceId, siblingInstanceId));

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + failingInstanceId + "/actuator/axelix-caches/clear",
                            "/" + siblingInstanceId + "/actuator/axelix-caches/clear");
        }

        @Test
        void shouldReturnBadRequestAndInvokeRemainingInstances_WhenInstanceIsUnknown() {
            // given.
            CachesBulkClearRequest requestBody =
                    new CachesBulkClearRequest(List.of("unknown-cache-instance", siblingInstanceId));

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder("/" + siblingInstanceId + "/actuator/axelix-caches/clear");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRejectMissingInstanceIds(List<String> instanceIds) {
            // given.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", instanceIds);

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }
    }

    @Nested
    class ClearSpecificCache {

        @Test
        void shouldClearAcrossInstances() {
            // given.
            CacheBulkClearRequest requestBody =
                    new CacheBulkClearRequest(List.of(activeInstanceId, siblingInstanceId), "manager", "orders");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<String> response = editor.exchange(
                    "/api/external/caches/cache/clear", HttpMethod.DELETE, new HttpEntity<>(requestBody), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/clear",
                            "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/clear");
            assertSuccessfulCallback(MasterWebEndpoints.CACHE_CLEAR_ONE, editor.getActor());
        }

        @Test
        void shouldClearOnSingleInstance() {
            // given.
            CacheBulkClearRequest requestBody =
                    new CacheBulkClearRequest(List.of(activeInstanceId), "manager", "orders");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<String> response = editor.exchange(
                    "/api/external/caches/cache/clear", HttpMethod.DELETE, new HttpEntity<>(requestBody), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder("/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/clear");
            assertSuccessfulCallback(MasterWebEndpoints.CACHE_CLEAR_ONE, editor.getActor());
        }

        @Test
        void shouldClearWithBlankAndDuplicatedInstanceIds() {
            // given.
            CacheBulkClearRequest requestBody = new CacheBulkClearRequest(
                    List.of("", " ", activeInstanceId, activeInstanceId, siblingInstanceId), "manager", "orders");
            IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

            // when.
            ResponseEntity<String> response = editor.exchange(
                    "/api/external/caches/cache/clear", HttpMethod.DELETE, new HttpEntity<>(requestBody), String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/clear",
                            "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/clear");
            assertSuccessfulCallback(MasterWebEndpoints.CACHE_CLEAR_ONE, editor.getActor());
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 500})
        void shouldReturnBadRequestAndInvokeRemainingInstances_WhenOneInstanceFails(int failureStatusCode) {
            // given.
            failingCacheOperationStatusCode.set(failureStatusCode);
            CacheBulkClearRequest requestBody =
                    new CacheBulkClearRequest(List.of(failingInstanceId, siblingInstanceId), "manager", "orders");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/cache/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + failingInstanceId + "/actuator/axelix-caches/manager/orders/clear",
                            "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/clear");
        }

        @Test
        void shouldReturnBadRequestAndInvokeRemainingInstances_WhenInstanceIsUnknown() {
            // given.
            CacheBulkClearRequest requestBody = new CacheBulkClearRequest(
                    List.of("unknown-cache-instance", siblingInstanceId), "manager", "orders");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/cache/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactlyInAnyOrder(
                            "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/clear");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRejectMissingInstanceIds(List<String> instanceIds) {
            // given.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", instanceIds);
            requestBody.put("cacheManagerName", "manager");
            requestBody.put("cacheName", "orders");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/cache/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = " ")
        void shouldRejectMissingCacheManagerName(String value) {
            // given.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", List.of(activeInstanceId));
            requestBody.put("cacheManagerName", value);
            requestBody.put("cacheName", "orders");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/cache/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = " ")
        void shouldRejectMissingCacheName(String value) {
            // given.
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("instanceIds", List.of(activeInstanceId));
            requestBody.put("cacheManagerName", "manager");
            requestBody.put("cacheName", value);

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/cache/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(invokedPaths).isEmpty();
        }

        @Test
        void shouldReturnBadRequest_WhenCacheManagerDoesNotExist() {
            // given.
            CacheBulkClearRequest requestBody =
                    new CacheBulkClearRequest(List.of(activeInstanceId), "unknownManager", "orders");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/cache/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactly("/" + activeInstanceId + "/actuator/axelix-caches/unknownManager/orders/clear");
        }

        @Test
        void shouldReturnBadRequest_WhenCacheDoesNotExist() {
            // given.
            CacheBulkClearRequest requestBody =
                    new CacheBulkClearRequest(List.of(activeInstanceId), "manager", "unknownCache");

            // when.
            ResponseEntity<String> response = restTemplate
                    .asEditor()
                    .exchange(
                            "/api/external/caches/cache/clear",
                            HttpMethod.DELETE,
                            new HttpEntity<>(requestBody),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
            assertThat(invokedPaths)
                    .containsExactly("/" + activeInstanceId + "/actuator/axelix-caches/manager/unknownCache/clear");
        }
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(
                new TestableMasterWebEndpoint(MasterWebEndpoints.CACHES_CLEAR_ALL, "/api/external/caches/clear"),
                new TestableMasterWebEndpoint(MasterWebEndpoints.CACHE_CLEAR_ONE, "/api/external/caches/cache/clear"));
    }
}
