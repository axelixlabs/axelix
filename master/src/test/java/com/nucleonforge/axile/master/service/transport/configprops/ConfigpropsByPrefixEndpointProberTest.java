package com.nucleonforge.axile.master.service.transport.configprops;

import java.io.IOException;
import java.util.Map;
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

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigpropsByPrefixEndpointProber;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ConfigpropsByPrefixEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class ConfigpropsByPrefixEndpointProberTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private ConfigpropsByPrefixEndpointProber configpropsByPrefixEndpointProber;

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
              "contexts" : {
                "application" : {
                  "beans" : {
                    "spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties" : {
                      "prefix" : "spring.jackson",
                      "properties" : {
                        "serialization" : {
                          "INDENT_OUTPUT" : true
                        },
                        "defaultPropertyInclusion" : "NON_NULL",
                        "visibility" : { },
                        "parser" : { },
                        "deserialization" : { },
                        "generator" : { },
                        "mapper" : { }
                      },
                      "inputs" : {
                        "serialization" : {
                          "INDENT_OUTPUT" : {
                            "value" : "true",
                            "origin" : "\\"spring.jackson.serialization.indent_output\\" from property source \\"Inlined Test Properties\\""
                          }
                        },
                        "defaultPropertyInclusion" : {
                          "value" : "non_null",
                          "origin" : "\\"spring.jackson.default-property-inclusion\\" from property source \\"Inlined Test Properties\\""
                        },
                        "visibility" : { },
                        "parser" : { },
                        "deserialization" : { },
                        "generator" : { },
                        "mapper" : { }
                      }
                    }
                  }
                }
              }
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/configprops/spring.jackson")) {
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
    @SuppressWarnings("unchecked")
    void shouldReturnConfigpropsSingleBean() {
        // when.
        String prefix = "spring.jackson";
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));

        // then.
        ConfigpropsFeed feed = configpropsByPrefixEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        // context
        assertThat(feed).isNotNull();
        assertThat(feed.contexts()).containsKey("application");

        // beans
        ConfigpropsFeed.Context context = feed.contexts().get("application");
        Map<String, ConfigpropsFeed.Bean> bean = context.beans();
        assertThat(bean).hasSize(1);

        // spring.jackson-org
        ConfigpropsFeed.Bean springJacksonOrg =
                bean.get("spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties");

        // prefix
        assertThat(springJacksonOrg.prefix()).isEqualTo("spring.jackson");

        // properties
        assertThat(springJacksonOrg.properties().get("serialization")).isEqualTo(Map.of("INDENT_OUTPUT", true));
        assertThat(springJacksonOrg.properties().get("defaultPropertyInclusion"))
                .isEqualTo("NON_NULL");
        assertThat(springJacksonOrg.properties().get("visibility")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.properties().get("parser")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.properties().get("deserialization")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.properties().get("generator")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.properties().get("mapper")).isEqualTo(Map.of());

        // inputs
        assertThat(springJacksonOrg.inputs().get("visibility")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.inputs().get("parser")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.inputs().get("deserialization")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.inputs().get("generator")).isEqualTo(Map.of());
        assertThat(springJacksonOrg.inputs().get("mapper")).isEqualTo(Map.of());

        // inputs -> "serialization"
        Map<String, Object> inputsSerialization =
                (Map<String, Object>) springJacksonOrg.inputs().get("serialization");
        assertThat(inputsSerialization.get("INDENT_OUTPUT"))
                .isEqualTo(
                        Map.of(
                                "value",
                                "true",
                                "origin",
                                "\"spring.jackson.serialization.indent_output\" from property source \"Inlined Test Properties\""));

        // inputs -> "defaultPropertyInclusion"
        Map<String, Object> inputsDefaultPropertyInclusion =
                (Map<String, Object>) springJacksonOrg.inputs().get("defaultPropertyInclusion");
        assertThat(inputsDefaultPropertyInclusion).containsEntry("value", "non_null");
        assertThat(inputsDefaultPropertyInclusion)
                .containsEntry(
                        "origin",
                        "\"spring.jackson.default-property-inclusion\" from property source \"Inlined Test Properties\"");
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        // when.
        String prefix = "spring.jackson";
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));

        // then.
        assertThatThrownBy(() -> configpropsByPrefixEndpointProber.invoke(InstanceId.of(instanceId), payload))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        // when.
        String prefix = "spring.jackson";
        String instanceId = "unregistered-instance";
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));

        // then.
        assertThatThrownBy(() -> configpropsByPrefixEndpointProber.invoke(InstanceId.of(instanceId), payload))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
