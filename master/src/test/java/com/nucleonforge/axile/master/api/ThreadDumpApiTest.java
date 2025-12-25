/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.api;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.auth.CookieService;
import com.nucleonforge.axile.master.service.auth.UserLoginService;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.utils.TestObjectFactory;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ThreadDumpApi}.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "axile.master.auth.type=none")
class ThreadDumpApiTest {

    @MockBean
    private UserApi userApi;

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private CookieService cookieService;

    private static final String EXPECTED_THREAD_DUMP_JSON =
            // language=json
            """
      {
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
          "lockInfo" : {
            "className" : "java.lang.ref.ReferenceQueue$Lock",
            "identityHashCode" : 1627380895
          },
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
          "lockedSynchronizers" : [ ]
        } ]
    }
    """;

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplate restTemplate;

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
        String jsonResponse =
                """
    {
       "threads" : [ {
         "threadName" : "Test worker",
         "threadId" : 1,
         "blockedTime" : -1,
         "blockedCount" : 37,
         "waitedTime" : -1,
         "waitedCount" : 109,
         "lockOwnerId" : -1,
         "daemon" : false,
         "inNative" : false,
         "suspended" : false,
         "threadState" : "RUNNABLE",
         "priority" : 5,
         "stackTrace" : [ {
           "moduleName" : "java.management",
           "moduleVersion" : "17.0.17",
           "methodName" : "dumpThreads0",
           "fileName" : "ThreadImpl.java",
           "lineNumber" : -2,
           "nativeMethod" : true,
           "className" : "sun.management.ThreadImpl"
         }, {
           "moduleName" : "java.management",
           "moduleVersion" : "17.0.17",
           "methodName" : "dumpAllThreads",
           "fileName" : "ThreadImpl.java",
           "lineNumber" : 528,
           "nativeMethod" : false,
           "className" : "sun.management.ThreadImpl"
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
         "daemon" : true,
         "inNative" : false,
         "suspended" : false,
         "threadState" : "WAITING",
         "priority" : 8,
         "stackTrace" : [ {
           "moduleName" : "java.base",
           "moduleVersion" : "17.0.17",
           "methodName" : "wait",
           "lineNumber" : -2,
           "nativeMethod" : true,
           "className" : "java.lang.Object"
         }, {
           "moduleName" : "java.base",
           "moduleVersion" : "17.0.17",
           "methodName" : "run",
           "fileName" : "Finalizer.java",
           "lineNumber" : 172,
           "nativeMethod" : false,
           "className" : "java.lang.ref.Finalizer$FinalizerThread"
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

                if (path.equals("/" + activeInstanceId + "/actuator/threaddump")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnJSONThreadDumpFeed() {
        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/thread-dump/{instanceId}", String.class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();

        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_THREAD_DUMP_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<?> response =
                restTemplate.getForEntity("/api/axile/thread-dump/{instanceId}", Void.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/thread-dump/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
