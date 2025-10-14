package com.nucleonforge.axile.master.service.transport;

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

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.request.ScheduledTaskToggleRequest;
import com.nucleonforge.axile.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.scheduled.DisableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axile.master.service.transport.scheduled.EnableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axile.master.service.transport.scheduled.GetAllScheduledTasksEndpointProber;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GetAllScheduledTasksEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class ScheduledTasksEndpointProberTest {
    private final String activeInstanceId = UUID.randomUUID().toString();

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
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive"
                    },
                    "expression": "0 0 0/3 1/1 * ?",
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.999631800Z"
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
                    },
                    "lastExecution": {
                      "exception": null,
                      "time": "2025-10-14T06:33:48.092631800Z",
                      "status": "ERROR"
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

                if (path.equals("/" + activeInstanceId + "/scheduledtasks")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/scheduled-tasks-management/enable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/scheduled-tasks-management/disable")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
        registry.register(createInstanceWithUrl(
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
        assertThat(cron.delegate().runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive");
        assertThat(cron.delegate().expression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.delegate().nextExecution().time()).isEqualTo("2025-10-14T06:33:49.999631800Z");
        assertThat(cron.delegate().lastExecution()).isNull();

        // FixedDelayTask
        ServiceScheduledTasks.FixedDelayTask fixedDelay =
                serviceScheduledTasks.fixedDelay().get(0);
        assertThat(fixedDelay.enabled()).isTrue();
        assertThat(fixedDelay.delegate().runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask");
        assertThat(fixedDelay.delegate().interval()).isEqualTo(2000);
        assertThat(fixedDelay.delegate().initialDelay()).isEqualTo(0);
        assertThat(fixedDelay.delegate().nextExecution().time()).isEqualTo("2025-10-14T06:33:49.063630700Z");
        assertThat(fixedDelay.delegate().lastExecution().status()).isEqualTo("SUCCESS");
        assertThat(fixedDelay.delegate().lastExecution().time()).isEqualTo("2025-10-14T06:33:47.001570800Z");
        assertThat(fixedDelay.delegate().lastExecution().exception()).isNull();

        // FixedRateTask
        ServiceScheduledTasks.FixedRateTask fixedRate =
                serviceScheduledTasks.fixedRate().get(0);
        assertThat(fixedRate.enabled()).isFalse();
        assertThat(fixedRate.delegate().runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");
        assertThat(fixedRate.delegate().interval()).isEqualTo(2000);
        assertThat(fixedRate.delegate().initialDelay()).isEqualTo(100);
        assertThat(fixedRate.delegate().nextExecution().time()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(fixedRate.delegate().lastExecution().time()).isEqualTo("2025-10-14T06:33:48.092631800Z");
        assertThat(fixedRate.delegate().lastExecution().status()).isEqualTo("ERROR");
        assertThat(fixedRate.delegate().lastExecution().exception()).isNull();

        // CustomTask
        ServiceScheduledTasks.CustomTask custom = serviceScheduledTasks.custom().get(0);
        assertThat(custom.enabled()).isFalse();
        assertThat(custom.delegate().trigger())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0");
        assertThat(custom.delegate().runnable().target())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634");
        assertThat(custom.delegate().lastExecution().status()).isEqualTo("ERROR");
        assertThat(custom.delegate().lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.delegate().lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(custom.delegate().lastExecution().exception().message())
                .isEqualTo("Failed while running custom task");
    }

    @Test
    void shouldEnableSingleScheduledTask() throws InterruptedException {
        // language=json
        String jsonRequest =
                """
          {
             "targetScheduledTask": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask",
             "force": true
          }
          """;
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(requestBody));

        // when.
        enableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/scheduledtasksmanagement/enable");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(jsonRequest);
    }

    @Test
    void shouldDisableSingleScheduledTask() throws InterruptedException {
        // language=json
        String jsonRequest =
                """
            {
               "targetScheduledTask": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask",
               "force": true
            }
            """;
        ScheduledTaskToggleRequest requestBody = new ScheduledTaskToggleRequest(
                "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask", true);

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(requestBody));

        // when.
        disableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), payload);

        // then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/scheduledtasksmanagement/disable");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(jsonRequest);
    }
}
