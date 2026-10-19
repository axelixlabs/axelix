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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.api.external.request.caches.CacheBulkStatusChangeRequest;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesManagementApi}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
class CachesManagementApiTest extends AbstractProtectedEndpointTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String siblingInstanceId = UUID.randomUUID().toString();
    private static final String failingInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    private final List<String> invokedPaths = new CopyOnWriteArrayList<>();
    private final AtomicInteger failingCacheOperationStatusCode = new AtomicInteger(500);

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
                invokedPaths.add(path);

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/enable")
                        || path.equals("/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/disable")) {
                    return new MockResponse();
                }
                if (path.equals("/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/enable")
                        || path.equals("/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/disable")) {
                    return new MockResponse();
                }
                if (path.equals("/" + failingInstanceId + "/actuator/axelix-caches/manager/orders/enable")
                        || path.equals("/" + failingInstanceId + "/actuator/axelix-caches/manager/orders/disable")) {
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
        invokedPaths.clear();
        failingCacheOperationStatusCode.set(500);
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldChangeCacheStatusAcrossInstances(String operation) {
        // given.
        CacheBulkStatusChangeRequest requestBody =
                new CacheBulkStatusChangeRequest(List.of(activeInstanceId, siblingInstanceId), "manager", "orders");
        IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

        // when.
        ResponseEntity<String> response =
                editor.postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(invokedPaths)
                .containsExactlyInAnyOrder(
                        "/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/" + operation,
                        "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/" + operation);
        assertSuccessfulCallback(
                operation.equals("enable") ? MasterWebEndpoints.CACHE_ENABLE : MasterWebEndpoints.CACHE_DISABLE,
                editor.getActor());
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldChangeCacheStatusOnSingleInstance(String operation) {
        // given.
        CacheBulkStatusChangeRequest requestBody =
                new CacheBulkStatusChangeRequest(List.of(activeInstanceId), "manager", "orders");
        IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

        // when.
        ResponseEntity<String> response =
                editor.postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(invokedPaths)
                .containsExactlyInAnyOrder(
                        "/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/" + operation);
        assertSuccessfulCallback(
                operation.equals("enable") ? MasterWebEndpoints.CACHE_ENABLE : MasterWebEndpoints.CACHE_DISABLE,
                editor.getActor());
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldChangeCacheStatusWithBlankAndDuplicatedInstanceIds(String operation) {
        // given.
        CacheBulkStatusChangeRequest requestBody = new CacheBulkStatusChangeRequest(
                List.of("", " ", activeInstanceId, activeInstanceId, siblingInstanceId), "manager", "orders");
        IdentityAwareTestRestTemplate editor = restTemplate.asEditor();

        // when.
        ResponseEntity<String> response =
                editor.postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(invokedPaths)
                .containsExactlyInAnyOrder(
                        "/" + activeInstanceId + "/actuator/axelix-caches/manager/orders/" + operation,
                        "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/" + operation);
        assertSuccessfulCallback(
                operation.equals("enable") ? MasterWebEndpoints.CACHE_ENABLE : MasterWebEndpoints.CACHE_DISABLE,
                editor.getActor());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 500})
    void shouldReturnBadRequestAndInvokeRemainingInstances_WhenOneInstanceFails_enable(int failureStatusCode) {
        // given.
        failingCacheOperationStatusCode.set(failureStatusCode);
        CacheBulkStatusChangeRequest requestBody =
                new CacheBulkStatusChangeRequest(List.of(failingInstanceId, siblingInstanceId), "manager", "orders");

        // when.
        ResponseEntity<String> response =
                restTemplate.asEditor().postForEntity("/api/external/caches/cache/enable", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
        assertThat(invokedPaths)
                .containsExactlyInAnyOrder(
                        "/" + failingInstanceId + "/actuator/axelix-caches/manager/orders/enable",
                        "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/enable");
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 500})
    void shouldReturnBadRequestAndInvokeRemainingInstances_WhenOneInstanceFails_disable(int failureStatusCode) {
        // given.
        failingCacheOperationStatusCode.set(failureStatusCode);
        CacheBulkStatusChangeRequest requestBody =
                new CacheBulkStatusChangeRequest(List.of(failingInstanceId, siblingInstanceId), "manager", "orders");

        // when.
        ResponseEntity<String> response =
                restTemplate.asEditor().postForEntity("/api/external/caches/cache/disable", requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
        assertThat(invokedPaths)
                .containsExactlyInAnyOrder(
                        "/" + failingInstanceId + "/actuator/axelix-caches/manager/orders/disable",
                        "/" + siblingInstanceId + "/actuator/axelix-caches/manager/orders/disable");
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldReturnBadRequestWithoutInvokingAnyInstances_WhenInstanceIsUnknown(String operation) {
        // given.
        CacheBulkStatusChangeRequest requestBody = new CacheBulkStatusChangeRequest(
                List.of("unknown-cache-instance", siblingInstanceId, "another-missing-instance"), "manager", "orders");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("INSTANCE_NOT_FOUND");
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectNullInstanceIds(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", null);
        requestBody.put("cacheManagerName", "manager");
        requestBody.put("cacheName", "orders");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectEmptyInstanceIds(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", List.of());
        requestBody.put("cacheManagerName", "manager");
        requestBody.put("cacheName", "orders");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectNullCacheManagerName(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", List.of(activeInstanceId));
        requestBody.put("cacheManagerName", null);
        requestBody.put("cacheName", "orders");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectEmptyCacheManagerName(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", List.of(activeInstanceId));
        requestBody.put("cacheManagerName", "");
        requestBody.put("cacheName", "orders");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectBlankCacheManagerName(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", List.of(activeInstanceId));
        requestBody.put("cacheManagerName", " ");
        requestBody.put("cacheName", "orders");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectNullCacheName(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", List.of(activeInstanceId));
        requestBody.put("cacheManagerName", "manager");
        requestBody.put("cacheName", null);

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectEmptyCacheName(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", List.of(activeInstanceId));
        requestBody.put("cacheManagerName", "manager");
        requestBody.put("cacheName", "");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldRejectBlankCacheName(String operation) {
        // given.
        // Use a map to send invalid input: the request constructor would reject it before the HTTP call.
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("instanceIds", List.of(activeInstanceId));
        requestBody.put("cacheManagerName", "manager");
        requestBody.put("cacheName", " ");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invokedPaths).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldReturnBadRequest_WhenCacheManagerDoesNotExist(String operation) {
        // given.
        CacheBulkStatusChangeRequest requestBody =
                new CacheBulkStatusChangeRequest(List.of(activeInstanceId), "unknownManager", "orders");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
        assertThat(invokedPaths)
                .containsExactly("/" + activeInstanceId + "/actuator/axelix-caches/unknownManager/orders/" + operation);
    }

    @ParameterizedTest
    @MethodSource("cacheOperations")
    void shouldReturnBadRequest_WhenCacheDoesNotExist(String operation) {
        // given.
        CacheBulkStatusChangeRequest requestBody =
                new CacheBulkStatusChangeRequest(List.of(activeInstanceId), "manager", "unknownCache");

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity("/api/external/caches/cache/" + operation, requestBody, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("PARTIALLY_UPDATED");
        assertThat(invokedPaths)
                .containsExactly("/" + activeInstanceId + "/actuator/axelix-caches/manager/unknownCache/" + operation);
    }

    private static Stream<String> cacheOperations() {
        return Stream.of("enable", "disable");
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(
                new TestableMasterWebEndpoint(MasterWebEndpoints.CACHE_ENABLE, "/api/external/caches/cache/enable"),
                new TestableMasterWebEndpoint(MasterWebEndpoints.CACHE_DISABLE, "/api/external/caches/cache/disable"));
    }
}
