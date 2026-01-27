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
package com.nucleonforge.axelix.master.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.service.export.HeapDumpAnonymizer;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.utils.InvalidAuthScenario;
import com.nucleonforge.axelix.master.utils.TestObjectFactory;
import com.nucleonforge.axelix.master.utils.TestRestTemplateBuilder;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link StateExportApi}.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StateExportApiTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @MockBean
    private HeapDumpAnonymizer heapDumpAnonymizer;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private InstanceRegistry registry;

    // language=json
    private static final String HTTP_REQUEST_BODY =
            """
            {
                "components" : [
                    {
                        "component" : "BEANS"
                    },
                    {
                        "component" : "CACHES"
                    },
                    {
                        "component" : "CONDITIONS"
                    },
                    {
                        "component" : "CONFIG_PROPS"
                    },
                    {
                        "component" : "ENV"
                    },
                    {
                        "component" : "SCHEDULED_TASKS"
                    },
                    {
                        "component" : "THREAD_DUMP"
                    },
                    {
                        "component" : "LOG_FILE"
                    },
                    {
                      "component" : "GC_LOG_FILE"
                    },
                    {
                        "component" : "HEAP_DUMP",
                        "sanitized" : true
                    }
                ]
            }
            """;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    @BeforeEach
    void prepare() {
        // language=json
        String beansJsonResponse =
                """
        {
          "contexts": {
            "application": {
              "parentId": null,
              "beans": {
                "jmxEndpointProperties": {
                  "scope": "singleton",
                  "type": "JmxEndpointProperties",
                  "proxyType" : "CGLIB",
                  "aliases": [],
                  "dependencies": [],
                  "isLazyInit": false,
                  "isPrimary": false,
                  "qualifiers": [],
                  "beanSource": {
                     "origin": "COMPONENT_ANNOTATION"
                  }
                }
              }
            }
          }
        }
        """;

        // language=json
        String envJsonResponse =
                """
        {
          "activeProfiles": ["production"],
          "defaultProfiles": ["default", "development"],
          "propertySources": [
            {
              "name": "systemProperties",
              "properties": [
                {
                  "name": "java.vm.vendor",
                  "value": "BellSoft",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null
                }
              ]
            }
          ]
        }
        """;

        // language=json
        String jsonConditionsResponse =
                """
    {
      "positiveConditions": [
        {
          "target": "EndpointAutoConfiguration#propertiesEndpointAccessResolver",
          "matches": [
            {
              "condition": "OnBeanCondition",
              "message": "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.EndpointAccessResolver; SearchStrategy: all) did not find any beans"
            }
          ]
        }
      ],
      "negativeConditions": [
        {
          "target": "WebFluxEndpointManagementContextConfiguration",
          "notMatched": [
            {
              "condition": "OnWebApplicationCondition",
              "message": "not a reactive web application"
            }
          ]
        }
      ]
    }
    """;

        String jsonCacheResponse =
                // language=json
                """
    {
      "cacheManagers" : [
        {
          "name": "anotherCacheManager",
          "caches": [
            {
              "name": "countries",
              "target" : "java.util.concurrent.ConcurrentHashMap",
              "enabled": true,
              "hitsCount" : 12,
              "missesCount" : 4,
              "estimatedEntrySize" : 1
            }
          ]
        }
      ]
    }
    """;

        // language=json
        String jsonConfigpropsResponse =
                """
            {
          "contexts" : {
            "application1" : {
              "beans" : {
                "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties" : {
                  "prefix" : "management.endpoints.web.cors",
                  "properties": [
                        { "key": "allowedOrigins", "value": null },
                        { "key": "maxAge", "value": "PT30M" }
                      ],
                      "inputs": [
                        { "key": "allowedOrigins", "value": null },
                        { "key": "maxAge", "value": null }
                      ]
                }
              }
            }
          }
        }
    """;

        // language=json
        String jsonSchedulesTasksResponse =
                """
        {
          "cron": [
            {
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive"
              },
              "expression": "*/2 * * * * *",
              "nextExecution": {
                "time": "2025-10-14T06:33:49.999631800Z"
              },
              "lastExecution": {
                "exception": null,
                "time": "2025-10-14T06:33:48.014578100Z",
                "status": "STARTED"
              },
              "enabled": true
            }
          ],
          "fixedDelay": [],
          "fixedRate": [],
          "custom": []
        }
        """;

        // language=json
        String jsonThreadDumpResponse =
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
       } ]
    }
    """;

        String mockLogFileResponse =
                """
             2025-11-12T14:05:13.795+05:00  INFO 1868 --- [main] o.s.s.petclinic.PetClinicApplication: Starting PetClinicApplication using Java 17.0.16 with PID 1868 (C:\\Project\\spring-petclinic\\target\\classes started in C:\\Project\\spring-petclinic)
             2025-11-12T14:05:13.795+05:00  INFO 1868 --- [main] o.s.s.petclinic.PetClinicApplication     : No active profile set, falling back to 1 default profile: "default"
             2025-11-12T14:05:14.382+05:00  INFO 1868 --- [main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
             2025-11-12T14:05:14.429+05:00  INFO 1868 --- [main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 30 ms. Found 3 JPA repository interfaces.
             2025-11-12T14:05:14.531+05:00  INFO 1868 --- [main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=4c03ca02-57eb-3d79-a155-785dae504167
             """;

        String mockGcLogFileResponse =
                """
         [2026-01-11T23:20:50.868+0500][info][gc] GC(348) Concurrent Mark Cycle
         [2026-01-11T23:20:50.878+0500][info][gc] GC(350) Pause Young (Normal) (G1 Evacuation Pause) 32M->31M(42M) 0.532ms
         [2026-01-11T23:20:50.883+0500][info][gc] GC(348) Pause Remark 33M->33M(42M) 2.256ms
         [2026-01-11T23:20:50.884+0500][info][gc] GC(351) Pause Young (Normal) (G1 Evacuation Pause) 33M->31M(42M) 0.380ms
         [2026-01-11T23:20:50.888+0500][info][gc] GC(352) Pause Young (Normal) (G1 Evacuation Pause) 33M->31M(42M) 0.342ms
         """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-beans")) {
                    return new MockResponse()
                            .setBody(beansJsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-env")) {
                    return new MockResponse()
                            .setBody(envJsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-conditions")) {
                    return new MockResponse()
                            .setBody(jsonConditionsResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches")) {
                    return new MockResponse()
                            .setBody(jsonCacheResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-configprops")) {
                    return new MockResponse()
                            .setBody(jsonConfigpropsResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks")) {
                    return new MockResponse()
                            .setBody(jsonSchedulesTasksResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-thread-dump")) {
                    return new MockResponse()
                            .setBody(jsonThreadDumpResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/heapdump")) {
                    return new MockResponse()
                            .setBody("Mock HPROF binary data")
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/logfile")) {
                    return new MockResponse()
                            .setBody(mockLogFileResponse)
                            .addHeader("Content-Type", "text/plain;charset=UTF-8");
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-gc/log/file")) {
                    return new MockResponse()
                            .setBody(mockGcLogFileResponse)
                            .addHeader("Content-Type", "text/plain;charset=UTF-8");
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<?> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/export-state/{instanceId}",
                        new HttpEntity<>(HTTP_REQUEST_BODY, headers),
                        Void.class,
                        instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnNotFoundForUnregisteredInstance() {
        String unknownInstanceId = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/export-state/{instanceId}",
                        new HttpEntity<>(HTTP_REQUEST_BODY, headers),
                        String.class,
                        unknownInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/api/axelix/export-state/{instanceId}",
                        new HttpEntity<>(HTTP_REQUEST_BODY, headers),
                        Void.class,
                        activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnZipArchiveWithJsonFiles() throws IOException {
        when(heapDumpAnonymizer.anonymize(any(Resource.class)))
                .thenReturn(new ByteArrayResource("sanitized".getBytes()));

        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<byte[]> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/export-state/{instanceId}",
                        new HttpEntity<>(HTTP_REQUEST_BODY, headers),
                        byte[].class,
                        activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/zip"));
        assertThat(response.getHeaders().getContentDisposition().getFilename()).endsWith(".zip");

        byte[] zipData = response.getBody();
        assertThat(zipData).isNotEmpty();

        assertZipArchiveContent(zipData);
    }

    private static void assertZipArchiveContent(byte[] zipData) throws IOException {
        String hprofData = null;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            Set<String> zipEntriesNames = new HashSet<>();
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                zipEntriesNames.add(entry.getName());

                if (entry.getName().equals("heap_dump.hprof")) {
                    byte[] buffer = zis.readAllBytes();
                    hprofData = new String(buffer, StandardCharsets.UTF_8);
                }

                zis.closeEntry();
            }

            assertThat(hprofData).isEqualTo("sanitized");
            assertThat(zipEntriesNames)
                    .containsOnly(
                            "beans.json",
                            "caches.json",
                            "conditions.json",
                            "config_props.json",
                            "env.json",
                            "scheduled_tasks.json",
                            "thread_dump.json",
                            "heap_dump.hprof",
                            "log_file.log",
                            "gc_log_file.log");
        }
    }
}
