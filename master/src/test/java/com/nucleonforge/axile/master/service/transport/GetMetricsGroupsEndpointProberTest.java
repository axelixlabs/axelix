package com.nucleonforge.axile.master.service.transport;

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
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.metrics.GetMetricsGroupsEndpointProber;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link GetMetricsGroupsEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class GetMetricsGroupsEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private GetMetricsGroupsEndpointProber getMetricsGroupsEndpointProber;

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
                      "key": "jvm.gc.memory.allocated",
                      "value": "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"
                    },
                    {
                      "key": "jvm.memory.usage.after.gc",
                      "value": "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"
                    },
                    {
                      "key": "jvm.memory.used",
                      "value": "The amount of used memory"
                    }
                  ]
                },
                {
                  "groupName": "process",
                  "metrics": [
                    {
                      "key": "process.cpu.time",
                      "value": "The \\"cpu time\\" used by the Java Virtual Machine process"
                    },
                    {
                      "key": "process.cpu.usage",
                      "value": "The \\"recent cpu usage\\" for the Java Virtual Machine process"
                    }
                  ]
                },
                {
                  "groupName": "tomcat",
                  "metrics": [
                    {
                      "key": "tomcat.sessions.active.current",
                      "value": null
                    },
                    {
                      "key": "tomcat.sessions.active.max",
                      "value": null
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

                if (path.equals("/" + activeInstanceId + "/actuator/axile-metrics")) {
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
    void shouldReturnAxileMetricsGroups() {
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        // when.
        AxileMetricsGroups metricsGroups =
                getMetricsGroupsEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // then.
        assertThat(metricsGroups.metricsGroups()).isNotEmpty().hasSize(3);

        // jvm
        AxileMetricsGroups.MetricsGroup jvmGroup = getMetricsGroup(metricsGroups, "jvm");
        assertThat(jvmGroup.groupName()).isEqualTo("jvm");
        assertThat(jvmGroup.metrics())
                .containsOnly(
                        new KeyValue(
                                "jvm.gc.memory.allocated",
                                "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"),
                        new KeyValue(
                                "jvm.memory.usage.after.gc",
                                "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"),
                        new KeyValue("jvm.memory.used", "The amount of used memory"));

        // process
        AxileMetricsGroups.MetricsGroup processGroup = getMetricsGroup(metricsGroups, "process");
        assertThat(processGroup.groupName()).isEqualTo("process");
        assertThat(processGroup.metrics())
                .containsOnly(
                        new KeyValue("process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                        new KeyValue(
                                "process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        // tomcat
        AxileMetricsGroups.MetricsGroup tomcatGroup = getMetricsGroup(metricsGroups, "tomcat");
        assertThat(tomcatGroup.groupName()).isEqualTo("tomcat");
        assertThat(tomcatGroup.metrics())
                .containsOnly(
                        new KeyValue("tomcat.sessions.active.current", null),
                        new KeyValue("tomcat.sessions.active.max", null));
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        // when.
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));
        assertThatThrownBy(
                        () -> getMetricsGroupsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                // then.
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        // when.
        String instanceId = "unregistered-instance";
        assertThatThrownBy(
                        () -> getMetricsGroupsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                // then.
                .isInstanceOf(InstanceNotFoundException.class);
    }

    private AxileMetricsGroups.MetricsGroup getMetricsGroup(AxileMetricsGroups response, String groupName) {
        return response.metricsGroups().stream()
                .filter(group -> group.groupName().equals(groupName))
                .findFirst()
                .get();
    }
}
