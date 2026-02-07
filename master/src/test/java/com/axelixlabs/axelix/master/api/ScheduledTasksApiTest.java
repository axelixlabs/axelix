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
package com.axelixlabs.axelix.master.api;

import java.io.IOException;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskCronExpressionModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskExecuteRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskIntervalModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskToggleRequest;
import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.ScheduledTasksApi;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
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
                } else if (path.equals(
                        "/" + activeInstanceId + "/actuator/axelix-scheduled-tasks/modify/cron-expression")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks/modify/interval")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks/enable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks/disable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-scheduled-tasks/execute")) {
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
                .getForEntity("/api/external/scheduled-tasks/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_MASTER_RESPONSE);
    }

    @ParameterizedTest
    @MethodSource("managementScheduledTask")
    void shouldEnableOrDisableSingleScheduledTask(String scheduledTaskStatus) {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<String> body = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}" + scheduledTaskStatus,
                        requestBody,
                        String.class,
                        activeInstanceId);

        // then.
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldModifyCronExpressionScheduledTask() {

        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 0 0/3 1/1 * ?");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/cron-expression",
                        requestBody,
                        Void.class,
                        activeInstanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldModifyIntervalScheduledTask() {

        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", 555555L);

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/interval",
                        requestBody,
                        Void.class,
                        activeInstanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldExecuteScheduledTask() {

        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/execute",
                        requestBody,
                        Void.class,
                        activeInstanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorOnGetAllScheduledTasks() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/api/external/scheduled-tasks/{instanceId}", EndpointInvocationException.class, instanceId);

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
                        "/api/external/scheduled-tasks/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("managementScheduledTask")
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnEnableOrDisableSingleScheduledTask(String scheduledTaskStatus) {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}" + scheduledTaskStatus,
                        requestBody,
                        EndpointInvocationException.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ParameterizedTest
    @MethodSource("managementScheduledTask")
    void shouldReturnBadRequestForUnregisteredInstance_OnEnableOrDisableSingleScheduledTask(
            String scheduledTaskStatus) {
        String instanceId = UUID.randomUUID().toString();
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}" + scheduledTaskStatus,
                        requestBody,
                        EndpointInvocationException.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnModifyCronExpression() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 0 0/3 1/1 * ?");

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/cron-expression",
                        requestBody,
                        Void.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnModifyCronExpression() {
        String instanceId = UUID.randomUUID().toString();
        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 0 0/3 1/1 * ?");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/cron-expression",
                        requestBody,
                        Void.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnModifyInterval() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", 555555L);

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/interval",
                        requestBody,
                        Void.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnModifyInterval() {
        String instanceId = UUID.randomUUID().toString();
        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", 555555L);

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/interval",
                        requestBody,
                        Void.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnTaskExecute() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/interval",
                        requestBody,
                        Void.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance_OnExecuteTask() {
        String instanceId = UUID.randomUUID().toString();
        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/interval",
                        requestBody,
                        Void.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnGetAllScheduledTasks(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<?> response = scenario.getModifier()
                .apply(restTemplate)
                .getForEntity("/api/external/scheduled-tasks/{instanceId}", Void.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnEnableSingleScheduledTask(InvalidAuthScenario scenario) {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/enable", requestBody, Void.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnDisableSingleScheduledTask(InvalidAuthScenario scenario) {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/disable",
                        requestBody,
                        Void.class,
                        activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static Stream<Arguments> managementScheduledTask() {
        return Stream.of(Arguments.of("/enable"), Arguments.of("/disable"));
    }
}
