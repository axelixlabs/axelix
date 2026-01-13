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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.TestRestTemplateBuilder;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskMutationRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskToggleRequest;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axelix.master.utils.TestObjectFactory;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
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
              "nextExecution": {
                "time": "2025-10-14T06:33:50.086630700Z"
              },
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
    private static final String activeInstanceIdMutateTask = UUID.randomUUID().toString();

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
                },
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
                    },
                    "expression": "*/5 * * * * *",
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.999631800Z"
                    },
                    "enabled": true
                },
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
                    },
                    "expression": "*/2 * * * * *",
                    "lastExecution": {
                      "exception": null,
                      "time": "2025-10-14T06:33:48.014578100Z",
                      "status": "SUCCESS"
                    },
                    "enabled": true
                }
              ],
              "fixedDelay": [
                {
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
                    },
                    "enabled": true
                }
              ],
              "fixedRate": [
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask"
                    },
                    "initialDelay": 100,
                    "interval": 2000,
                    "nextExecution": {
                      "time": "2025-10-14T06:33:50.086630700Z"
                    },
                    "enabled": false
                }
              ],
              "custom": [
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634"
                    },
                    "trigger": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0",
                    "nextExecution": {
                       "time": "2025-10-14T06:33:50.086630700Z"
                    },
                    "lastExecution": {
                      "exception": {
                        "message": "Failed while running custom task",
                        "type": "java.lang.IllegalStateException"
                      },
                      "status": "ERROR",
                      "time": "2025-09-18T15:03:34.132500256Z"
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

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceIdMutateTask + "/actuator/axelix-scheduled-tasks")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks/enable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks/disable")) {
                    return new MockResponse();
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
    void shouldReturnJSONScheduledTasksResponse() {
        // when.

        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/axelix/scheduled-tasks/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_MASTER_RESPONSE);
    }

    @Test
    void shouldEnableSingleScheduledTask() {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<String> body = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}/enable", requestBody, String.class, activeInstanceId);

        // then
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableSingleScheduledTask() {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<String> body = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}/disable",
                        requestBody,
                        String.class,
                        activeInstanceId);

        // then
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldModifyConfigurationScheduledTask() {

        ScheduledTaskMutationRequest requestBody = new ScheduledTaskMutationRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 0 0/3 1/1 * ?");

        registry.register(TestObjectFactory.createInstance(
                activeInstanceIdMutateTask, mockWebServer.url(activeInstanceIdMutateTask) + "/actuator"));

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}",
                        requestBody,
                        Void.class,
                        activeInstanceIdMutateTask);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorOnGetAllScheduledTasks() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnGetAllScheduledTasks() {
        String instanceId = "unregistered-axelix-scheduled-tasks-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorOnEnableSingleScheduledTask() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}/enable",
                        requestBody,
                        EndpointInvocationException.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnEnableSingleScheduledTask() {
        String instanceId = "unregistered-axelix-scheduled-tasks-enable-instance";
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}/enable",
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
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}/disable",
                        requestBody,
                        EndpointInvocationException.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnDisableSingleScheduledTask() {
        String instanceId = "unregistered-axelix-scheduled-tasks-enable-instance";
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/scheduled-tasks/{instanceId}/disable",
                        requestBody,
                        EndpointInvocationException.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnModifyConfigurationScheduledTask() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskMutationRequest requestBody = new ScheduledTaskMutationRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 0 0/3 1/1 * ?");

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity("/api/axelix/scheduled-tasks/{instanceId}", requestBody, Void.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnModifyConfigurationScheduledTask() {
        String instanceId = "unregistered-axelix-scheduled-tasks-enable-instance";
        ScheduledTaskMutationRequest requestBody = new ScheduledTaskMutationRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 0 0/3 1/1 * ?");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity("/api/axelix/scheduled-tasks/{instanceId}", requestBody, Void.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
