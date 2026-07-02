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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ThreadDumpApi}.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThreadDumpApiTest {

    private static final String EXPECTED_THREAD_DUMP_JSON =
            // language=json
            """
      {
        "threadContentionMonitoringEnabled" : true,
        "threads" : [ {
          "threadName" : "Test worker",
          "threadId" : 1,
          "blockedTime" : -1,
          "blockedCount" : 37,
          "waitedTime" : -1,
          "waitedCount" : 109,
          "lockInfo" : null,
          "lockName" : null,
          "lockOwnerId" : -1,
          "lockOwnerName" : null,
          "daemon" : false,
          "inNative" : false,
          "suspended" : false,
          "threadState" : "RUNNABLE",
          "priority" : 5,
          "stackTrace" : [ {
            "classLoaderName" : null,
            "className" : "sun.management.ThreadImpl",
            "fileName" : "ThreadImpl.java",
            "lineNumber" : -2,
            "methodName" : "dumpThreads0",
            "moduleName" : "java.management",
            "moduleVersion" : "17.0.17",
            "nativeMethod" : true
          }, {
            "classLoaderName" : null,
            "className" : "sun.management.ThreadImpl",
            "fileName" : "ThreadImpl.java",
            "lineNumber" : 528,
            "methodName" : "dumpAllThreads",
            "moduleName" : "java.management",
            "moduleVersion" : "17.0.17",
            "nativeMethod" : false
          } ],
          "lockedMonitors" : [ ],
          "lockedSynchronizers" : [ ]
        }, {
          "threadName" : "Finalizer",
          "threadId" : 3,
          "blockedTime" : -1,
          "blockedCount" : 0,
          "waitedTime" : -1,
          "waitedCount" : 1,
          "lockName" : "java.lang.ref.ReferenceQueue$Lock@60ffdc9f",
          "lockOwnerId" : -1,
          "lockOwnerName" : null,
          "daemon" : true,
          "inNative" : false,
          "suspended" : false,
          "threadState" : "WAITING",
          "priority" : 8,
          "stackTrace" : [ {
            "classLoaderName" : null,
            "className" : "java.lang.Object",
            "fileName" : null,
            "lineNumber" : -2,
            "methodName" : "wait",
            "moduleName" : "java.base",
            "moduleVersion" : "17.0.17",
            "nativeMethod" : true
          }, {
            "classLoaderName" : null,
            "className" : "java.lang.ref.Finalizer$FinalizerThread",
            "fileName" : "Finalizer.java",
            "lineNumber" : 172,
            "methodName" : "run",
            "moduleName" : "java.base",
            "moduleVersion" : "17.0.17",
            "nativeMethod" : false
          } ],
          "lockedMonitors" : [ ],
          "lockedSynchronizers" : [ ],
          "lockInfo" : {
            "className" : "java.lang.ref.ReferenceQueue$Lock",
            "identityHashCode" : 1627380895
          }
        } ]
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
        // language=json
        String jsonResponse = """
    {
       "threadContentionMonitoringEnabled" : true,
       "threads" : [ {
         "threadName" : "Test worker",
         "threadId" : 1,
         "blockedTime" : -1,
         "blockedCount" : 37,
         "waitedTime" : -1,
         "waitedCount" : 109,
         "lockInfo" : null,
         "lockName" : null,
         "lockOwnerId" : -1,
         "lockOwnerName" : null,
         "daemon" : false,
         "inNative" : false,
         "suspended" : false,
         "threadState" : "RUNNABLE",
         "priority" : 5,
         "stackTrace" : [ {
           "classLoaderName" : null,
           "className" : "sun.management.ThreadImpl",
           "fileName" : "ThreadImpl.java",
           "lineNumber" : -2,
           "methodName" : "dumpThreads0",
           "moduleName" : "java.management",
           "moduleVersion" : "17.0.17",
           "nativeMethod" : true
         }, {
           "classLoaderName" : null,
           "className" : "sun.management.ThreadImpl",
           "fileName" : "ThreadImpl.java",
           "lineNumber" : 528,
           "methodName" : "dumpAllThreads",
           "moduleName" : "java.management",
           "moduleVersion" : "17.0.17",
           "nativeMethod" : false
         } ],
         "lockedMonitors" : [ ],
         "lockedSynchronizers" : [ ]
       }, {
         "threadName" : "Finalizer",
         "threadId" : 3,
         "blockedTime" : -1,
         "blockedCount" : 0,
         "waitedTime" : -1,
         "waitedCount" : 1,
         "lockName" : "java.lang.ref.ReferenceQueue$Lock@60ffdc9f",
         "lockOwnerId" : -1,
         "lockOwnerName" : null,
         "daemon" : true,
         "inNative" : false,
         "suspended" : false,
         "threadState" : "WAITING",
         "priority" : 8,
         "stackTrace" : [ {
           "classLoaderName" : null,
           "className" : "java.lang.Object",
           "fileName" : null,
           "lineNumber" : -2,
           "methodName" : "wait",
           "moduleName" : "java.base",
           "moduleVersion" : "17.0.17",
           "nativeMethod" : true
         }, {
           "classLoaderName" : null,
           "className" : "java.lang.ref.Finalizer$FinalizerThread",
           "fileName" : "Finalizer.java",
           "lineNumber" : 172,
           "methodName" : "run",
           "moduleName" : "java.base",
           "moduleVersion" : "17.0.17",
           "nativeMethod" : false
         } ],
         "lockedMonitors" : [ ],
         "lockedSynchronizers" : [ ],
         "lockInfo" : {
           "className" : "java.lang.ref.ReferenceQueue$Lock",
           "identityHashCode" : 1627380895
         }
       } ]
    }
    """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-thread-dump")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-thread-dump/enable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-thread-dump/disable")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.reload(TestObjectFactory.createTestInstance(
                activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONThreadDumpFeed() {
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/thread-dump/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_THREAD_DUMP_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();
        registry.reload(createInstance(instanceId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/thread-dump/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/thread-dump/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("managementCachesContentionMonitoring")
    void shouldEnableOrDisableContentionMonitoring(String contentionMonitoringStatus) throws InterruptedException {
        // when.
        ResponseEntity<Void> response = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/thread-dump/{instanceId}/thread-contention-monitoring"
                                + contentionMonitoringStatus,
                        null,
                        Void.class,
                        Map.of("instanceId", activeInstanceId));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("managementCachesContentionMonitoring")
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnEnableOrDisableContentionMonitoring(String contentionMonitoringStatus) {
        String instanceId = UUID.randomUUID().toString();
        registry.reload(createInstance(instanceId));
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/thread-dump/{instanceId}/thread-contention-monitoring"
                                + contentionMonitoringStatus,
                        null,
                        String.class,
                        Map.of("instanceId", instanceId));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @MethodSource("managementCachesContentionMonitoring")
    void shouldReturnBadRequestForUnregisteredInstance_OnEnableOrDisableContentionMonitoring(
            String contentionMonitoringStatus) {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .postForEntity(
                        "/api/external/thread-dump/{instanceId}/thread-contention-monitoring"
                                + contentionMonitoringStatus,
                        null,
                        String.class,
                        Map.of("instanceId", instanceId));

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ProtectedEndpointTests(
            method = HttpMethod.GET,
            path = "/api/external/thread-dump/00000000-0000-0000-0000-000000000001")
    void negativeAuthTestsOnThreadDump() {}

    @ProtectedEndpointTests(
            method = HttpMethod.POST,
            path = "/api/external/thread-dump/00000000-0000-0000-0000-000000000001/thread-contention-monitoring/enable")
    void negativeAuthTestsOnEnableContentionMonitoring() {}

    @ProtectedEndpointTests(
            method = HttpMethod.POST,
            path =
                    "/api/external/thread-dump/00000000-0000-0000-0000-000000000001/thread-contention-monitoring/disable")
    void negativeAuthTestsOnDisableContentionMonitoring() {}

    private static Stream<Arguments> managementCachesContentionMonitoring() {
        return Stream.of(Arguments.of("/enable"), Arguments.of("/disable"));
    }
}
