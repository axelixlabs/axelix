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
package com.nucleonforge.axelix.master.service.transport;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskCronExpressionModifyRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskExecuteRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskIntervalModifyRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskToggleRequest;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.scheduled.DisableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.EnableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.ExecuteScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.GetAllScheduledTasksEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.ModifyCronExpressionScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.ModifyIntervalScheduledTaskEndpointProber;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GetAllScheduledTasksEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class ScheduledTasksEndpointProberTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();

    private MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private GetAllScheduledTasksEndpointProber getAllScheduledTasksEndpointProber;

    @Autowired
    private EnableSingleScheduledTaskEndpointProber enableSingleScheduledTaskEndpointProber;

    @Autowired
    private DisableSingleScheduledTaskEndpointProber disableSingleScheduledTaskEndpointProber;

    @Autowired
    private ModifyCronExpressionScheduledTaskEndpointProber modifyCronExpressionScheduledTaskEndpointProber;

    @Autowired
    private ModifyIntervalScheduledTaskEndpointProber modifyIntervalScheduledTaskEndpointProber;

    @Autowired
    private ExecuteScheduledTaskEndpointProber executeScheduledTaskEndpointProber;

    @Autowired
    private JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    @BeforeEach
    void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
        // language=json
        String response =
                """
            {
              "cron": [
                {
                   "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive"
                   },
                    "expression": "0 0 0/3 1/1 * ?",
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.999631800Z"
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
                    "lastExecution": {
                      "exception": null,
                      "time": "2025-10-14T06:33:48.092631800Z",
                      "status": "ERROR"
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

                if (path.equals("/" + activeInstanceId + "/axelix-scheduled-tasks")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/axelix-scheduled-tasks/modify/cron-expression")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/axelix-scheduled-tasks/modify/interval")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/axelix-scheduled-tasks/enable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/axelix-scheduled-tasks/disable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/axelix-scheduled-tasks/execute")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
        registry.register(createInstance(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnServiceScheduledTasks() {
        // when.
        ServiceScheduledTasks serviceScheduledTasks =
                getAllScheduledTasksEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // CronTask
        ServiceScheduledTasks.CronTask cron = serviceScheduledTasks.cron().get(0);
        assertThat(cron.enabled()).isTrue();
        assertThat(cron.runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive");
        assertThat(cron.expression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.nextExecution().time()).isEqualTo("2025-10-14T06:33:49.999631800Z");
        assertThat(cron.lastExecution()).isNull();

        // FixedDelayTask
        ServiceScheduledTasks.FixedDelayTask fixedDelay =
                serviceScheduledTasks.fixedDelay().get(0);
        assertThat(fixedDelay.enabled()).isTrue();
        assertThat(fixedDelay.runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask");
        assertThat(fixedDelay.interval()).isEqualTo(2000);
        assertThat(fixedDelay.initialDelay()).isEqualTo(0);
        assertThat(fixedDelay.nextExecution().time()).isEqualTo("2025-10-14T06:33:49.063630700Z");
        assertThat(fixedDelay.lastExecution().status()).isEqualTo("SUCCESS");
        assertThat(fixedDelay.lastExecution().time()).isEqualTo("2025-10-14T06:33:47.001570800Z");
        assertThat(fixedDelay.lastExecution().exception()).isNull();

        // FixedRateTask
        ServiceScheduledTasks.FixedRateTask fixedRate =
                serviceScheduledTasks.fixedRate().get(0);
        assertThat(fixedRate.enabled()).isFalse();
        assertThat(fixedRate.runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");
        assertThat(fixedRate.interval()).isEqualTo(2000);
        assertThat(fixedRate.initialDelay()).isEqualTo(100);
        assertThat(fixedRate.nextExecution().time()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(fixedRate.lastExecution().time()).isEqualTo("2025-10-14T06:33:48.092631800Z");
        assertThat(fixedRate.lastExecution().status()).isEqualTo("ERROR");
        assertThat(fixedRate.lastExecution().exception()).isNull();

        // CustomTask
        ServiceScheduledTasks.CustomTask custom = serviceScheduledTasks.custom().get(0);
        assertThat(custom.enabled()).isFalse();
        assertThat(custom.trigger())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0");
        assertThat(custom.runnable().target())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634");
        assertThat(custom.nextExecution().time()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(custom.lastExecution().status()).isEqualTo("ERROR");
        assertThat(custom.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(custom.lastExecution().exception().message()).isEqualTo("Failed while running custom task");
    }

    @Test
    void shouldEnableSingleScheduledTask() throws InterruptedException {
        // language=json
        String jsonRequest =
                """
          {
             "taskId": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask"
          }
          """;
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(requestBody));

        // when.
        enableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/axelix-scheduled-tasks/enable");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(jsonRequest);
    }

    @Test
    void shouldDisableSingleScheduledTask() throws InterruptedException {
        // language=json
        String jsonRequest =
                """
            {
               "taskId": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask"
            }
            """;
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(requestBody));

        // when.
        disableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/axelix-scheduled-tasks/disable");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(jsonRequest);
    }

    @Test
    void shouldModifyCronExpressionScheduledTask() throws InterruptedException {
        // language=json
        String jsonRequest =
                """
        {
           "taskId": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive",
           "cronExpression" : "*/5 0 0/3 1/1 * ?"
        }
        """;
        ScheduledTaskCronExpressionModifyRequest requestBody = new ScheduledTaskCronExpressionModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive", "*/5 0 0/3 1/1 * ?");

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(requestBody));

        // when.
        modifyCronExpressionScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + activeInstanceId + "/axelix-scheduled-tasks/modify/cron-expression");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(jsonRequest);
    }

    @Test
    void shouldModifyIntervalScheduledTask() throws InterruptedException {
        // language=json
        String jsonRequest =
                """
        {
           "taskId": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask",
           "interval" : 22222
        }
        """;

        ScheduledTaskIntervalModifyRequest requestBody = new ScheduledTaskIntervalModifyRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask", 22222L);

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(requestBody));

        // when.
        modifyIntervalScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + activeInstanceId + "/axelix-scheduled-tasks/modify/interval");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(jsonRequest);
    }

    @Test
    void shouldExecuteScheduledTask() throws InterruptedException {
        // language=json
        String jsonRequest =
                """
            {
               "taskId": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask"
            }
            """;

        ScheduledTaskExecuteRequest requestBody = new ScheduledTaskExecuteRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask");

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(requestBody));

        // when.
        executeScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/axelix-scheduled-tasks/execute");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(jsonRequest);
    }
}
