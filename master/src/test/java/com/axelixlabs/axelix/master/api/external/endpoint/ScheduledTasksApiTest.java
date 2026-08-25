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
import java.util.Set;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskCronExpressionModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskExecuteRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskIntervalModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskToggleRequest;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.external.request.ScheduledTaskCronExpressionValidationRequest;
import com.axelixlabs.axelix.master.api.external.response.ScheduledTaskCronExpressionValidationResponse;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksApi}.
 *
 * @author Sergey Cherkasov
 * @since 28.08.2025
 */
public class ScheduledTasksApiTest extends AbstractProtectedEndpointTest {

    // language=json
    private static final String EXPECTED_MASTER_RESPONSE = """
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
              "interval": 2000,
              "initialDelay": 0,
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
              "interval": 2000,
              "initialDelay": 100,
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
                  "type": "java.lang.IllegalStateException",
                  "message": "Failed while running custom task"
                },
                "status": "ERROR",
                "time": "2025-09-18T15:03:34.132500256Z"
              },
              "enabled": false
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
        String jsonResponse = """
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

        registry.reload(
                TestInstanceFactory.create(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONScheduledTasksResponse() {
        // when.
        var viewer = restTemplate.asViewer();
        ResponseEntity<String> response =
                viewer.getForEntity("/api/external/scheduled-tasks/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_MASTER_RESPONSE);
        assertSuccessfulCallback(MasterWebEndpoints.SCHEDULED_TASKS_READ, viewer.getActor());
    }

    @ParameterizedTest
    @MethodSource("managementScheduledTask")
    void shouldEnableOrDisableSingleScheduledTask(String scheduledTaskStatus) {
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        var editor = restTemplate.asEditor();
        ResponseEntity<String> body = editor.postForEntity(
                "/api/external/scheduled-tasks/{instanceId}" + scheduledTaskStatus,
                requestBody,
                String.class,
                activeInstanceId);

        // then.
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertSuccessfulCallback(
                scheduledTaskStatus.equals("/enable")
                        ? MasterWebEndpoints.SCHEDULED_TASK_ENABLE
                        : MasterWebEndpoints.SCHEDULED_TASK_DISABLE,
                editor.getActor());
    }

    @Test
    void shouldModifyCronExpressionScheduledTask() {
        // given.
        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 * * * * *");

        // when.
        var editor = restTemplate.asEditor();
        ResponseEntity<Void> response = editor.postForEntity(
                "/api/external/scheduled-tasks/{instanceId}/modify/cron-expression",
                requestBody,
                Void.class,
                activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertSuccessfulCallback(MasterWebEndpoints.SCHEDULED_TASK_MODIFY_CRON, editor.getActor());
    }

    @Test
    void shouldReturnValidResponse_OnValidateCronExpressionWithValidExpression() {
        // given.
        var requestBody = new ScheduledTaskCronExpressionValidationRequest("*/5 * * * * *");

        // when.
        var editor = restTemplate.asEditor();
        ResponseEntity<ScheduledTaskCronExpressionValidationResponse> response = editor.postForEntity(
                "/api/external/scheduled-tasks/validate-cron-expression",
                requestBody,
                ScheduledTaskCronExpressionValidationResponse.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().valid()).isTrue();
        assertSuccessfulCallback(MasterWebEndpoints.SCHEDULED_TASK_VALIDATE_CRON, editor.getActor());
    }

    @ParameterizedTest
    @MethodSource("invalidCronExpressions")
    void shouldReturnInvalidResponse_OnValidateCronExpressionWithInvalidExpression(String invalidCronExpression) {
        // given.
        var requestBody = new ScheduledTaskCronExpressionValidationRequest(invalidCronExpression);

        // when.
        var editor = restTemplate.asEditor();
        ResponseEntity<ScheduledTaskCronExpressionValidationResponse> response = editor.postForEntity(
                "/api/external/scheduled-tasks/validate-cron-expression",
                requestBody,
                ScheduledTaskCronExpressionValidationResponse.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().valid()).isFalse();
        assertSuccessfulCallback(MasterWebEndpoints.SCHEDULED_TASK_VALIDATE_CRON, editor.getActor());
    }

    @ParameterizedTest
    @MethodSource("invalidCronExpressions")
    @DisplayName("Should return 400 Bad Request for invalid cron expression")
    void shouldReturnBadRequest_OnModifyCronExpressionWithInvalidExpression(String invalidCronExpression) {
        // given.
        var requestBody = new ScheduledTaskCronExpressionModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", invalidCronExpression);

        // and.
        String expectedResponse = """
            {
                "errorCode" : "%s",
                "attributes" : {}
            }
            """.formatted(ApiErrorCodes.INVALID_CRON_EXPRESSION.getErrorCode());

        // when.
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/cron-expression",
                        requestBody,
                        String.class,
                        activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThatJson(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void shouldModifyIntervalScheduledTask() {

        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", 555555L);

        // when.
        var editor = restTemplate.asEditor();
        ResponseEntity<Void> response = editor.postForEntity(
                "/api/external/scheduled-tasks/{instanceId}/modify/interval",
                requestBody,
                Void.class,
                activeInstanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertSuccessfulCallback(MasterWebEndpoints.SCHEDULED_TASK_MODIFY_INTERVAL, editor.getActor());
    }

    @Test
    void shouldExecuteScheduledTask() {

        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        var editor = restTemplate.asEditor();
        ResponseEntity<Void> response = editor.postForEntity(
                "/api/external/scheduled-tasks/{instanceId}/execute", requestBody, Void.class, activeInstanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertSuccessfulCallback(MasterWebEndpoints.SCHEDULED_TASK_EXECUTE, editor.getActor());
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorOnGetAllScheduledTasks() {
        String instanceId = UUID.randomUUID().toString();
        registry.reload(TestInstanceFactory.create(instanceId));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/scheduled-tasks/{instanceId}", String.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnGetAllScheduledTasks() {
        String instanceId = "unregistered-axelix-scheduled-tasks-instance";

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/scheduled-tasks/{instanceId}", String.class, instanceId);

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
        registry.reload(TestInstanceFactory.create(instanceId));
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}" + scheduledTaskStatus,
                        requestBody,
                        String.class,
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
        ResponseEntity<String> response = restTemplate
                .asEditor()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}" + scheduledTaskStatus,
                        requestBody,
                        String.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError_OnModifyCronExpression() {
        String instanceId = UUID.randomUUID().toString();

        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 * * * * *");

        // when.
        registry.reload(TestInstanceFactory.create(instanceId));
        ResponseEntity<Void> response = restTemplate
                .asEditor()
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
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask", "*/5 * * * * *");

        // when.
        ResponseEntity<Void> response = restTemplate
                .asEditor()
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
        registry.reload(TestInstanceFactory.create(instanceId));
        ResponseEntity<Void> response = restTemplate
                .asEditor()
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
                .asEditor()
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
        registry.reload(TestInstanceFactory.create(instanceId));
        ResponseEntity<Void> response = restTemplate
                .asEditor()
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
                .asEditor()
                .postForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/modify/interval",
                        requestBody,
                        Void.class,
                        instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Stream<Arguments> managementScheduledTask() {
        return Stream.of(Arguments.of("/enable"), Arguments.of("/disable"));
    }

    private static Stream<Arguments> invalidCronExpressions() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("invalid"),
                Arguments.of("* * *"),
                Arguments.of("*/5 * * * * * *"),
                Arguments.of("60 * * * * *"),
                Arguments.of("* 60 * * * *"));
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.SCHEDULED_TASKS_READ,
                        "/api/external/scheduled-tasks/00000000-0000-0000-0000-000000000001"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.SCHEDULED_TASK_ENABLE,
                        "/api/external/scheduled-tasks/00000000-0000-0000-0000-000000000001/enable"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.SCHEDULED_TASK_DISABLE,
                        "/api/external/scheduled-tasks/00000000-0000-0000-0000-000000000001/disable"));
    }
}
