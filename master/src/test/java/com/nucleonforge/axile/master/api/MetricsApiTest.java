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
 * Integration tests for {@link MetricsApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "axile.master.auth.type=none")
public class MetricsApiTest {

    @MockBean
    private UserApi userApi;

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private CookieService cookieService;

    // language=json
    private static final String EXPECTED_GROUPS_METRICS_JSON =
            """
        {
          "metricsGroups": [
            {
              "groupName": "jvm",
              "metrics": [
                {
                  "metricName": "jvm.gc.memory.allocated",
                  "description": "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"
                },
                {
                  "metricName": "jvm.memory.usage.after.gc",
                  "description": "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"
                },
                {
                  "metricName": "jvm.memory.used",
                  "description": "The amount of used memory"
                },
                {
                  "metricName": "jvm.threads.daemon",
                  "description": "The current number of live daemon threads"
                }
              ]
            },
            {
              "groupName": "process",
              "metrics": [
                {
                  "metricName": "process.cpu.time",
                  "description": "The \\"cpu time\\" used by the Java Virtual Machine process"
                },
                {
                  "metricName": "process.cpu.usage",
                  "description": "The \\"recent cpu usage\\" for the Java Virtual Machine process"
                }
              ]
            },
            {
              "groupName": "tomcat",
              "metrics": [
                {
                  "metricName": "tomcat.sessions.active.current",
                  "description": null
                },
                {
                  "metricName": "tomcat.sessions.active.max",
                  "description": null
                }
              ]
            }
          ]
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
              "metricsGroups": [
                {
                  "groupName": "jvm",
                  "metrics": [
                    {
                      "metricName": "jvm.gc.memory.allocated",
                      "description": "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"
                    },
                    {
                      "metricName": "jvm.memory.usage.after.gc",
                      "description": "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"
                    },
                    {
                      "metricName": "jvm.memory.used",
                      "description": "The amount of used memory"
                    },
                    {
                      "metricName": "jvm.threads.daemon",
                      "description": "The current number of live daemon threads"
                    }
                  ]
                },
                {
                  "groupName": "process",
                  "metrics": [
                    {
                      "metricName": "process.cpu.time",
                      "description": "The \\"cpu time\\" used by the Java Virtual Machine process"
                    },
                    {
                      "metricName": "process.cpu.usage",
                      "description": "The \\"recent cpu usage\\" for the Java Virtual Machine process"
                    }
                  ]
                },
                {
                  "groupName": "tomcat",
                  "metrics": [
                    {
                      "metricName": "tomcat.sessions.active.current",
                      "description": null
                    },
                    {
                      "metricName": "tomcat.sessions.active.max",
                      "description": null
                    }
                  ]
                }
              ]
            }
    """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axile-metrics")) {
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
    void shouldReturnJSONMetricsGroupResponse() {
        // when.
        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/metrics/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_GROUPS_METRICS_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/metrics/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/metrics/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
