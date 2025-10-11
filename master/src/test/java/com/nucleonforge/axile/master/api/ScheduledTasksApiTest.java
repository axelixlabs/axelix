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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksApi}.
 *
 * @since 28.08.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduledTasksApiTest {

    // language=json
    private static final String EXPECTED_SCHEDULEDTASKS_JSON =
            """
        {
      "cron": [
        {
          "runnable": {
            "target": "com.example.Processor.processOrders"
          },
          "expression": "0 0 0/3 1/1 * ?",
          "nextExecution": {
            "time": "2025-09-18T17:59:59.999098218Z"
          }
        },
        {
          "runnable": {
            "target": "com.example.Processor.processOrdersTest"
          },
          "expression": "0 0 0/3 1/1 * ?",
          "nextExecution": {
            "time": "2025-09-18T17:59:59.999098218Z"
          }
        }
      ],
      "fixedDelay": [
        {
          "runnable": {
            "target": "com.example.Processor.purge"
          },
          "interval": 5000,
          "initialDelay": 0,
          "nextExecution": {
            "time": "2025-09-18T15:03:39.117492423Z"
          },
          "lastExecution": {
            "status": "SUCCESS",
            "time": "2025-09-18T15:03:34.113091965Z"
          }
        }
      ],
      "fixedRate": [
        {
          "runnable": {
            "target": "com.example.Processor.retrieveIssues"
          },
          "interval": 3000,
          "initialDelay": 10000,
          "nextExecution": {
            "time": "2025-09-18T15:03:44.102073608Z"
          }
        }
      ],
      "custom": [
        {
          "runnable": {
            "target": "com.example.Processor$CustomTriggeredRunnable@438fc55e"
          },
          "trigger": "com.example.Processor$CustomTrigger@56567e9b",
          "lastExecution": {
            "status": "ERROR",
            "time": "2025-09-18T15:03:34.132500256Z",
            "exception": {
              "type": "java.lang.IllegalStateException",
              "message": "Failed while running custom task"
            }
          }
        },
        {
          "runnable": {
            "target": "com.example.Processor$CustomTriggeredRunnable@438fc55eTest"
          },
          "trigger": "com.example.Processor$CustomTrigger@56567e9b",
          "lastExecution": {
            "status": "ERROR",
            "time": "2025-09-18T15:03:34.132500256Z",
            "exception": {
              "message": "Failed while running custom task"
            }
          }
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
          "cron" : [ {
            "expression" : "0 0 0/3 1/1 * ?",
            "nextExecution" : {
              "time" : "2025-09-18T17:59:59.999098218Z"
            },
            "runnable" : {
              "target" : "com.example.Processor.processOrders"
            }
          }, {
            "expression" : "0 0 0/3 1/1 * ?",
            "nextExecution" : {
              "time" : "2025-09-18T17:59:59.999098218Z"
            },
            "runnable" : {
              "target" : "com.example.Processor.processOrdersTest"
            }
          } ],
          "custom" : [ {
            "lastExecution" : {
              "exception" : {
                "message" : "Failed while running custom task",
                "type" : "java.lang.IllegalStateException"
              },
              "status" : "ERROR",
              "time" : "2025-09-18T15:03:34.132500256Z"
            },
            "runnable" : {
              "target" : "com.example.Processor$CustomTriggeredRunnable@438fc55e"
            },
            "trigger" : "com.example.Processor$CustomTrigger@56567e9b"
          },
          {
            "lastExecution" : {
              "exception" : {
                "message" : "Failed while running custom task"
              },
              "status" : "ERROR",
              "time" : "2025-09-18T15:03:34.132500256Z"
            },
            "runnable" : {
              "target" : "com.example.Processor$CustomTriggeredRunnable@438fc55eTest"
            },
            "trigger" : "com.example.Processor$CustomTrigger@56567e9b"
          } ],
          "fixedDelay" : [ {
            "initialDelay" : 0,
            "interval" : 5000,
            "lastExecution" : {
              "status" : "SUCCESS",
              "time" : "2025-09-18T15:03:34.113091965Z"
            },
            "nextExecution" : {
              "time" : "2025-09-18T15:03:39.117492423Z"
            },
            "runnable" : {
              "target" : "com.example.Processor.purge"
            }
          } ],
          "fixedRate" : [ {
            "initialDelay" : 10000,
            "interval" : 3000,
            "nextExecution" : {
              "time" : "2025-09-18T15:03:44.102073608Z"
            },
            "runnable" : {
              "target" : "com.example.Processor.retrieveIssues"
            }
          } ]
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/scheduledtasks")) {
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
    void shouldReturnJSONScheduledTasksResponse() {
        // when.
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/scheduledtasks/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_SCHEDULEDTASKS_JSON);
    }

    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/scheduledtasks/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = "unregistered-info-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/scheduledtasks/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
