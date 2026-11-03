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
package com.axelixlabs.axelix.master.api.external.endpoint.instance;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskLaunchType;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskCronExpressionModifyRequest;
import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskEnableRequest;
import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskExecuteRequest;
import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskIntervalModifyRequest;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.external.request.ScheduledTaskCronExpressionValidationRequest;
import com.axelixlabs.axelix.master.api.external.response.ScheduledTaskCronExpressionValidationResponse;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.repository.ScheduledTaskExecutionResultRepository;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
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
              "enabled": true,
              "successRate": null
            },
            {
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
              },
              "expression": "*/5 * * * * *",
              "enabled": true,
              "successRate": null
            },
            {
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
              },
              "expression": "*/2 * * * * *",
              "enabled": true,
              "successRate": null
            }
          ],
          "fixedDelay": [
            {
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask"
              },
              "interval": 2000,
              "initialDelay": 0,
              "enabled": true,
              "successRate": null
            }
          ],
          "fixedRate": [
            {
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask"
              },
              "interval": 2000,
              "initialDelay": 100,
              "enabled": false,
              "successRate": null
            }
          ],
          "custom": [
            {
              "runnable": {
                "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634"
              },
              "trigger": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0",
              "enabled": false,
              "successRate": null
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

    @Autowired
    private ScheduledTaskExecutionResultRepository executionResultRepository;

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
                    "enabled": true
                },
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
                    },
                    "expression": "*/5 * * * * *",
                    "enabled": true
                },
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask"
                    },
                    "expression": "*/2 * * * * *",
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
                    "enabled": false
                }
              ],
              "custom": [
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634"
                    },
                    "trigger": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0",
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
                TestInstanceFactory.create(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
        executionResultRepository.deleteAll();
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

    @Test
    void shouldReturnTheSummaryOfTheExecutionHistoryOfTheTaskOnAllTheInstancesOfTheApplication() {
        // given.
        String taskId = "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask";
        Instant startedAt = Instant.parse("2026-11-01T10:00:00Z");
        executionResultRepository.saveAll(List.of(
                execution(
                        "test-object-factory-app",
                        activeInstanceId,
                        taskId,
                        startedAt,
                        400,
                        "SQLTransientConnectionException",
                        "timeout"),
                execution(
                        "test-object-factory-app",
                        activeInstanceId,
                        taskId,
                        startedAt.plusSeconds(1),
                        200,
                        "IllegalStateException",
                        "boom 1"),
                new ScheduledTaskExecutionResult(
                        UUID.randomUUID().toString(),
                        "com.axelixlabs",
                        "test-object-factory-app",
                        "restarted-instance",
                        "restarted-instance-name",
                        taskId,
                        startedAt.plusSeconds(2),
                        100,
                        false,
                        ScheduledTaskLaunchType.MANUAL,
                        "IllegalStateException",
                        "boom 2"),
                execution(
                        "test-object-factory-app", activeInstanceId, taskId, startedAt.plusSeconds(3), 300, null, null),
                execution("other-app", activeInstanceId, taskId, startedAt.plusSeconds(4), 500, null, null)));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/history?taskId={taskId}",
                        String.class,
                        activeInstanceId,
                        taskId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // language=json
        assertThatJson(response.getBody()).when(IGNORING_EXTRA_FIELDS).isEqualTo("""
            {
              "summary": {
                "lastRunAt": "2026-11-01T10:00:03Z",
                "totalRuns": 4,
                "failedRuns": 3,
                "successRate": 25.0,
                "averageDurationMillis": 250,
                "p95DurationMillis": 400,
                "maxDurationMillis": 400
              },
              "failuresByErrorType": [
                {
                  "errorType": "IllegalStateException",
                  "count": 2,
                  "lastErrorMessage": "boom 2",
                  "lastOccurredAt": "2026-11-01T10:00:02Z"
                },
                {
                  "errorType": "SQLTransientConnectionException",
                  "count": 1,
                  "lastErrorMessage": "timeout",
                  "lastOccurredAt": "2026-11-01T10:00:00Z"
                }
              ]
            }
            """);
    }

    @Test
    void shouldReturnTheSuccessRateOfEachTaskOnAllTheInstancesOfTheApplication() {
        // given.
        String taskId = "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask";
        Instant startedAt = Instant.parse("2026-11-01T10:00:00Z");
        executionResultRepository.saveAll(List.of(
                execution(
                        "test-object-factory-app",
                        activeInstanceId,
                        taskId,
                        startedAt,
                        100,
                        "IllegalStateException",
                        "boom"),
                execution(
                        "test-object-factory-app", activeInstanceId, taskId, startedAt.plusSeconds(1), 100, null, null),
                execution(
                        "other-app",
                        activeInstanceId,
                        taskId,
                        startedAt.plusSeconds(2),
                        100,
                        "IllegalStateException",
                        "boom")));

        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity("/api/external/scheduled-tasks/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(response.getBody()).inPath("fixedRate[0].successRate").isEqualTo(50.0);
        assertThatJson(response.getBody()).inPath("fixedDelay[0].successRate").isNull();
    }

    @Test
    void shouldReturnThePageOfTheExecutionsOfTheTaskOnAllTheInstancesOfTheApplication() {
        // given.
        String taskId = "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask";
        Instant startedAt = Instant.parse("2026-11-01T10:00:00Z");
        executionResultRepository.saveAll(List.of(
                new ScheduledTaskExecutionResult(
                        UUID.randomUUID().toString(),
                        "com.axelixlabs",
                        "test-object-factory-app",
                        "restarted-instance",
                        "restarted-instance-name",
                        taskId,
                        startedAt,
                        100,
                        false,
                        ScheduledTaskLaunchType.MANUAL,
                        "IllegalStateException",
                        "boom"),
                execution(
                        "test-object-factory-app", activeInstanceId, taskId, startedAt.plusSeconds(1), 200, null, null),
                execution(
                        "test-object-factory-app", activeInstanceId, taskId, startedAt.plusSeconds(2), 300, null, null),
                execution(
                        "test-object-factory-app", activeInstanceId, taskId, startedAt.plusSeconds(3), 400, null, null),
                execution("other-app", activeInstanceId, taskId, startedAt.plusSeconds(4), 500, null, null)));

        // when.
        var viewer = restTemplate.asViewer();
        ResponseEntity<String> response = viewer.getForEntity(
                "/api/external/scheduled-tasks/{instanceId}/history?taskId={taskId}&page=1&size=2",
                String.class,
                activeInstanceId,
                taskId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // language=json
        assertThatJson(response.getBody()).isEqualTo("""
            {
              "summary": {
                "lastRunAt": "2026-11-01T10:00:03Z",
                "totalRuns": 4,
                "failedRuns": 1,
                "successRate": 75.0,
                "averageDurationMillis": 250,
                "p95DurationMillis": 400,
                "maxDurationMillis": 400
              },
              "failuresByErrorType": [
                {
                  "errorType": "IllegalStateException",
                  "count": 1,
                  "lastErrorMessage": "boom",
                  "lastOccurredAt": "2026-11-01T10:00:00Z"
                }
              ],
              "executions": [
                {
                  "startedAt": "2026-11-01T10:00:01Z",
                  "instanceId": "%1$s",
                  "instanceName": "test-object-factory-instance",
                  "durationMillis": 200,
                  "success": true,
                  "launchType": "SCHEDULED",
                  "errorType": null,
                  "errorMessage": null
                },
                {
                  "startedAt": "2026-11-01T10:00:00Z",
                  "instanceId": "restarted-instance",
                  "instanceName": "restarted-instance-name",
                  "durationMillis": 100,
                  "success": false,
                  "launchType": "MANUAL",
                  "errorType": "IllegalStateException",
                  "errorMessage": "boom"
                }
              ],
              "totalExecutions": 4,
              "page": 1,
              "size": 2
            }
            """.formatted(activeInstanceId));
        assertSuccessfulCallback(MasterWebEndpoints.SCHEDULED_TASK_EXECUTION_HISTORY_READ, viewer.getActor());
    }

    @ParameterizedTest
    @CsvSource({"-1, 30", "0, 0"})
    void shouldReturnBadRequestForOutOfBoundsPageOnGetExecutionHistory(int page, int size) {
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/history?taskId=task&page={page}&size={size}",
                        String.class,
                        activeInstanceId,
                        page,
                        size);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceOnGetExecutionHistory() {
        // when.
        ResponseEntity<String> response = restTemplate
                .asViewer()
                .getForEntity(
                        "/api/external/scheduled-tasks/{instanceId}/history?taskId=task",
                        String.class,
                        "unregistered-axelix-scheduled-tasks-instance");

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // the execution succeeded when it has no error type
    private static ScheduledTaskExecutionResult execution(
            String artifactId,
            String instanceId,
            String taskId,
            Instant startedAt,
            long durationMillis,
            @Nullable String errorType,
            @Nullable String errorMessage) {
        return new ScheduledTaskExecutionResult(
                UUID.randomUUID().toString(),
                "com.axelixlabs",
                artifactId,
                instanceId,
                "test-object-factory-instance",
                taskId,
                startedAt,
                durationMillis,
                errorType == null,
                ScheduledTaskLaunchType.SCHEDULED,
                errorType,
                errorMessage);
    }

    @ParameterizedTest
    @MethodSource("managementScheduledTask")
    void shouldEnableOrDisableSingleScheduledTask(String scheduledTaskStatus) {
        ScheduledTaskEnableRequest requestBody = new ScheduledTaskEnableRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

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
        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask")
                .cronExpression("*/5 * * * * *");

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
        var requestBody = new ScheduledTaskCronExpressionModifyRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask")
                .cronExpression(invalidCronExpression);

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

        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask")
                .interval(555555L);

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

        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

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
        registry.register(TestInstanceFactory.create(instanceId));

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

        ScheduledTaskEnableRequest requestBody = new ScheduledTaskEnableRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        registry.register(TestInstanceFactory.create(instanceId));
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
        ScheduledTaskEnableRequest requestBody = new ScheduledTaskEnableRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

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

        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask")
                .cronExpression("*/5 * * * * *");

        // when.
        registry.register(TestInstanceFactory.create(instanceId));
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
        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.cronTask")
                .cronExpression("*/5 * * * * *");

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

        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask")
                .interval(555555L);

        // when.
        registry.register(TestInstanceFactory.create(instanceId));
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
        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask")
                .interval(555555L);

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

        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        // when.
        registry.register(TestInstanceFactory.create(instanceId));
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
        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest()
                .trigger("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

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
                        MasterWebEndpoints.SCHEDULED_TASK_EXECUTION_HISTORY_READ,
                        "/api/external/scheduled-tasks/00000000-0000-0000-0000-000000000001/history?taskId=task"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.SCHEDULED_TASK_ENABLE,
                        "/api/external/scheduled-tasks/00000000-0000-0000-0000-000000000001/enable"),
                new TestableMasterWebEndpoint(
                        MasterWebEndpoints.SCHEDULED_TASK_DISABLE,
                        "/api/external/scheduled-tasks/00000000-0000-0000-0000-000000000001/disable"));
    }
}
