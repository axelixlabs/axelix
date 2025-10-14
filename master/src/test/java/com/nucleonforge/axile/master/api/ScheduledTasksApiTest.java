package com.nucleonforge.axile.master.api;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.request.ScheduledTaskToggleRequest;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints.DISABLE_SCHEDULED_TASK;
import static com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints.ENABLE_SCHEDULED_TASK;
import static com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints.SCHEDULED_TASKS;
import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksApi}.
 *
 * @author Sergey Cherkasov
 * @since 28.08.2025
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduledTasksApiTest {

    // language=json
    private static final String EXPECTED_MASTER_RESPONSE =
            """
         {
          "cron": [
            {
              "enabled": true,
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive"
              },
              "expression": "*/2 * * * * *",
              "nextExecution": {
                "time": "2025-10-14T06:33:49.999631800Z"
              },
              "lastExecution": {
                "status": "STARTED",
                "time": "2025-10-14T06:33:48.014578100Z"
              }
            },
            {
              "enabled": true,
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
              },
              "expression": "*/5 * * * * *",
              "nextExecution": {
                "time": "2025-10-14T06:33:49.999631800Z"
              }
            },
            {
              "enabled": true,
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
              },
              "expression": "*/2 * * * * *",
              "lastExecution": {
                "status": "SUCCESS",
                "time": "2025-10-14T06:33:48.014578100Z"
              }
            }
          ],
          "fixedDelay": [
            {
              "enabled": true,
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask"
              },
              "interval": 2000,
              "initialDelay": 0,
              "nextExecution": {
                "time": "2025-10-14T06:33:49.063630700Z"
              },
              "lastExecution": {
                "status": "SUCCESS",
                "time": "2025-10-14T06:33:47.001570800Z"
              }
            }
          ],
          "fixedRate": [
            {
              "enabled": false,
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask"
              },
              "interval": 2000,
              "initialDelay": 100,
              "nextExecution": {
                "time": "2025-10-14T06:33:50.086630700Z"
              }
            }
          ],
          "custom": [
            {
              "enabled": false,
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634"
              },
              "trigger": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0",
              "lastExecution": {
                "status": "ERROR",
                "time": "2025-09-18T15:03:34.132500256Z",
                "exception": {
                  "type": "java.lang.IllegalStateException",
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
                },
                {
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
                    },
                    "expression": "*/5 * * * * *",
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.999631800Z"
                    }
                  },
                  "enabled": true
                },
                {
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
                    },
                    "expression": "*/2 * * * * *",
                    "lastExecution": {
                      "exception": null,
                      "time": "2025-10-14T06:33:48.014578100Z",
                      "status": "SUCCESS"
                    }
                  },
                  "enabled": true
                }
              ],
              "fixedDelay": [
                {
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask"
                    },
                    "initialDelay": 0,
                    "interval": 2000,
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.063630700Z"
                    },
                    "lastExecution": {
                      "exception": null,
                      "time": "2025-10-14T06:33:47.001570800Z",
                      "status": "SUCCESS"
                    }
                  },
                  "enabled": true
                }
              ],
              "fixedRate": [
                {
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask"
                    },
                    "initialDelay": 100,
                    "interval": 2000,
                    "nextExecution": {
                      "time": "2025-10-14T06:33:50.086630700Z"
                    }
                  },
                  "enabled": false
                }
              ],
              "custom": [
                {
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634"
                    },
                    "trigger": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0",
                    "lastExecution": {
                      "exception": {
                        "message": "Failed while running custom task",
                        "type": "java.lang.IllegalStateException"
                      },
                      "status": "ERROR",
                      "time": "2025-09-18T15:03:34.132500256Z"
                    }
                  },
                  "enabled": false
                }
              ]
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator"
                        + SCHEDULED_TASKS.path().getOriginalUrl())) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator"
                        + ENABLE_SCHEDULED_TASK.path().getOriginalUrl())) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator"
                        + DISABLE_SCHEDULED_TASK.path().getOriginalUrl())) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONScheduledTasksResponse() {
        // when.

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/scheduled-tasks/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_MASTER_RESPONSE);
    }

    @Test
    void shouldEnableSingleScheduledTask() {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        // when.
        ResponseEntity<String> body = restTemplate.postForEntity(
                "/api/axile/scheduled-tasks/{instanceId}/enable", requestBody, String.class, activeInstanceId);

        // then
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableSingleScheduledTask() {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        // when.
        ResponseEntity<String> body = restTemplate.postForEntity(
                "/api/axile/scheduled-tasks/{instanceId}/disable", requestBody, String.class, activeInstanceId);

        // then
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorOnGetAllScheduledTasksResponse() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/scheduled-tasks/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnGetAllScheduledTasksResponse() {
        String instanceId = "unregistered-scheduled-tasks-management-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/scheduled-tasks/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorOnEnableSingleScheduledTask() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/scheduled-tasks/{instanceId}/enable",
                requestBody,
                EndpointInvocationException.class,
                instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnEnableSingleScheduledTask() {
        String instanceId = "unregistered-scheduled-tasks-management-enable--instance";
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/scheduled-tasks/{instanceId}/enable",
                requestBody,
                EndpointInvocationException.class,
                instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorOnDisableSingleScheduledTask() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/scheduled-tasks/{instanceId}/disable",
                requestBody,
                EndpointInvocationException.class,
                instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnDisableSingleScheduledTask() {
        String instanceId = "unregistered-scheduled-tasks-management-enable--instance";
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/scheduled-tasks/{instanceId}/disable",
                requestBody,
                EndpointInvocationException.class,
                instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
