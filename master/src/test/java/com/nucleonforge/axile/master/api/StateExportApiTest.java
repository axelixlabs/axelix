package com.nucleonforge.axile.master.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

// TODO: Actualize test. Add tests for the 'components' request param
/**
 * Integration tests for {@link StateExportApi}.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StateExportApiTest {

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
              "defaultProfiles": ["default","development"],
              "propertySources": [
                {
                  "name": "systemProperties",
                  "properties": {
                    "java.vm.vendor": {
                      "value": "BellSoft",
                      "isPrimary": true,
                      "configPropsBeanName": "test.property.systemProperties"
                    }
                  }
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
      "cacheManagers" : {
        "cacheManager" : {
          "caches" : {
            "cities" : {
              "target" : "java.util.concurrent.ConcurrentHashMap"
            }
          }
        }
      }
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
              "delegate": {
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
                }
              },
              "enabled": true
            }
          ],
          "fixedDelay": [],
          "fixedRate": [],
          "custom": []
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/beans")) {
                    return new MockResponse()
                            .setBody(beansJsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axile-env")) {
                    return new MockResponse()
                            .setBody(envJsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/conditions")) {
                    return new MockResponse()
                            .setBody(jsonConditionsResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/caches")) {
                    return new MockResponse()
                            .setBody(jsonCacheResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axile-configprops")) {
                    return new MockResponse()
                            .setBody(jsonConfigpropsResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/scheduledtasks")) {
                    return new MockResponse()
                            .setBody(jsonSchedulesTasksResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnZipArchiveWithJsonFiles() throws IOException {
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<byte[]> response =
                restTemplate.getForEntity("/api/axile/export-state/{instanceId}", byte[].class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/zip"));
        assertThat(response.getHeaders().getContentDisposition().getFilename()).endsWith(".zip");

        byte[] zipData = response.getBody();
        assertThat(zipData).isNotEmpty();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            Set<String> zipEntriesNames = new HashSet<>();
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                zipEntriesNames.add(entry.getName());
                zis.closeEntry();
            }

            assertThat(zipEntriesNames)
                    .containsOnly(
                            "beans.json",
                            "caches.json",
                            "conditions.json",
                            "config_props.json",
                            "env.json",
                            "scheduled_tasks.json");
        }
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<?> response =
                restTemplate.getForEntity("/api/axile/export-state/{instanceId}", Void.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnNotFoundForUnregisteredInstance() {
        String unknownInstanceId = UUID.randomUUID().toString();

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/export-state/{instanceId}", String.class, unknownInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
