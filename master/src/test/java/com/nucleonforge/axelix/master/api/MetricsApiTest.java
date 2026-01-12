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

import java.io.IOException;
import java.util.UUID;

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
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.TestRestTemplateBuilder;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axelix.master.utils.InvalidAuthScenario;
import com.nucleonforge.axelix.master.utils.TestObjectFactory;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MetricsApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MetricsApiTest {

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

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-metrics")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONMetricsGroupResponse() {
        // when.
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/axelix/metrics/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_GROUPS_METRICS_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/axelix/metrics/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/axelix/metrics/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response = scenario.modifier
                .apply(restTemplate)
                .getForEntity("/api/axelix/metrics/{instanceId}", Void.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
