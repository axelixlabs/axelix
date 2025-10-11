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
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksEndpointProber}.
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
    private ScheduledTasksEndpointProber scheduledTasksEndpointProber;

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
              "cron" : [ {
                "expression" : "0 0 0/3 1/1 * ?",
                "nextExecution" : {
                  "time" : "2025-09-18T17:59:59.999098218Z"
                },
                "runnable" : {
                  "target" : "com.example.Processor.processOrders"
                }
              }],
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
              }],
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

                if (path.equals("/" + activeInstanceId + "/scheduledtasks")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnServiceScheduledTasks() {
        // when.
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        ServiceScheduledTasks serviceScheduledTasks =
                scheduledTasksEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // Cron
        ServiceScheduledTasks.Cron cron = serviceScheduledTasks.cron().get(0);
        assertThat(cron.runnable().target()).isEqualTo("com.example.Processor.processOrders");
        assertThat(cron.expression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.nextExecution().time()).isEqualTo("2025-09-18T17:59:59.999098218Z");
        assertThat(cron.lastExecution()).isNull();

        // Custom
        ServiceScheduledTasks.Custom custom = serviceScheduledTasks.custom().get(0);
        assertThat(custom.trigger()).isEqualTo("com.example.Processor$CustomTrigger@56567e9b");
        assertThat(custom.runnable().target()).isEqualTo("com.example.Processor$CustomTriggeredRunnable@438fc55e");
        assertThat(custom.lastExecution().status()).isEqualTo("ERROR");
        assertThat(custom.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(custom.lastExecution().exception().message()).isEqualTo("Failed while running custom task");

        // FixedDelay
        ServiceScheduledTasks.FixedDelay fixedDelay =
                serviceScheduledTasks.fixedDelay().get(0);
        assertThat(fixedDelay.runnable().target()).isEqualTo("com.example.Processor.purge");
        assertThat(fixedDelay.interval()).isEqualTo(5000);
        assertThat(fixedDelay.initialDelay()).isEqualTo(0);
        assertThat(fixedDelay.nextExecution().time()).isEqualTo("2025-09-18T15:03:39.117492423Z");
        assertThat(fixedDelay.lastExecution().status()).isEqualTo("SUCCESS");
        assertThat(fixedDelay.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.113091965Z");
        assertThat(fixedDelay.lastExecution().exception()).isNull();

        // FixedRate
        ServiceScheduledTasks.FixedRate fixedRate =
                serviceScheduledTasks.fixedRate().get(0);
        assertThat(fixedRate.runnable().target()).isEqualTo("com.example.Processor.retrieveIssues");
        assertThat(fixedRate.interval()).isEqualTo(3000);
        assertThat(fixedRate.initialDelay()).isEqualTo(10000);
        assertThat(fixedRate.nextExecution().time()).isEqualTo("2025-09-18T15:03:44.102073608Z");
        assertThat(fixedRate.lastExecution()).isNull();
    }
}
