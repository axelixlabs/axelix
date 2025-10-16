package com.nucleonforge.axile.master.service.transport.configprops;

import java.io.IOException;
import java.util.List;
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
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigpropsEndpointProber;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ConfigpropsEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class ConfigpropsEndpointProberTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private ConfigpropsEndpointProber configpropsEndpointProber;

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
                            "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties" : {
                              "prefix" : "management.endpoints.web.cors",
                              "properties" : {
                                "allowedOrigins" : [ ],
                                "maxAge" : "PT30M",
                                "exposedHeaders" : [ ],
                                "allowedOriginPatterns" : [ ],
                                "allowedHeaders" : [ ],
                                "allowedMethods" : [ ]
                              },
                              "inputs" : {
                                "allowedOrigins" : [ ],
                                "maxAge" : { },
                                "exposedHeaders" : [ ],
                                "allowedOriginPatterns" : [ ],
                                "allowedHeaders" : [ ],
                                "allowedMethods" : [ ]
                              }
                            },
                            "management.endpoints.web-org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties" : {
                              "prefix" : "management.endpoints.web",
                              "properties" : {
                                "pathMapping" : { },
                                "exposure" : {
                                  "include" : [ "*" ],
                                  "exclude" : [ ]
                                },
                                "basePath" : "/actuator",
                                "discovery" : {
                                  "enabled" : true
                                }
                              },
                              "inputs" : {
                                "pathMapping" : { },
                                "exposure" : {
                                  "include" : [ {
                                    "value" : "*",
                                    "origin" : "\\"management.endpoints.web.exposure.include\\" from property source \\"Inlined Test Properties\\""
                                  } ],
                                  "exclude" : [ ]
                                },
                                "basePath" : { },
                                "discovery" : {
                                  "enabled" : { }
                                }
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

                if (path.equals("/" + activeInstanceId + "/configprops")) {
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
    void shouldReturnConfigpropsFeed() {
        // when.
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // then
        ConfigpropsFeed feed =
                configpropsEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);
        // context
        assertThat(feed).isNotNull();
        assertThat(feed.contexts()).containsKey("application");

        // beans
        ConfigpropsFeed.Context contaxt = feed.contexts().get("application");
        Map<String, ConfigpropsFeed.Bean> beans = contaxt.beans();
        assertThat(beans).hasSize(2);

        // managementEndpointsWebCors
        ConfigpropsFeed.Bean managementEndpointsWebCors = beans.get(
                "management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties");

        // managementEndpointsWebCors -> prefix
        assertThat(managementEndpointsWebCors.prefix()).isEqualTo("management.endpoints.web.cors");

        // managementEndpointsWebCors ->  properties
        assertThat(managementEndpointsWebCors.properties().get("allowedOrigins"))
                .isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.properties().get("maxAge")).isEqualTo("PT30M");
        assertThat(managementEndpointsWebCors.properties().get("exposedHeaders"))
                .isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.properties().get("allowedOriginPatterns"))
                .isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.properties().get("allowedHeaders"))
                .isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.properties().get("allowedMethods"))
                .isEqualTo(List.of());

        // managementEndpointsWebCors -> inputs
        assertThat(managementEndpointsWebCors.inputs().get("allowedOrigins")).isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.inputs().get("maxAge")).isEqualTo(Map.of());
        assertThat(managementEndpointsWebCors.inputs().get("exposedHeaders")).isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.inputs().get("allowedOriginPatterns"))
                .isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.inputs().get("allowedHeaders")).isEqualTo(List.of());
        assertThat(managementEndpointsWebCors.inputs().get("allowedMethods")).isEqualTo(List.of());

        // managementEndpointsWeb
        ConfigpropsFeed.Bean managementEndpointsWeb = beans.get(
                "management.endpoints.web-org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties");

        // managementEndpointsWeb -> prefix
        assertThat(managementEndpointsWeb.prefix()).isEqualTo("management.endpoints.web");

        // managementEndpointsWeb -> properties
        assertThat(managementEndpointsWeb.properties().get("pathMapping")).isEqualTo(Map.of());
        assertThat(managementEndpointsWeb.properties().get("basePath")).isEqualTo("/actuator");
        assertThat(managementEndpointsWeb.properties().get("discovery")).isEqualTo(Map.of("enabled", true));

        // managementEndpointsWeb -> properties -> "exposure"
        Map<String, Object> exposureProperties =
                (Map<String, Object>) managementEndpointsWeb.properties().get("exposure");
        assertThat(exposureProperties).containsEntry("include", List.of("*"));
        assertThat(exposureProperties).containsEntry("exclude", List.of());

        // managementEndpointsWeb -> inputs
        assertThat(managementEndpointsWeb.inputs().get("pathMapping")).isEqualTo(Map.of());
        assertThat(managementEndpointsWeb.inputs().get("basePath")).isEqualTo(Map.of());
        assertThat(managementEndpointsWeb.inputs().get("discovery")).isEqualTo(Map.of("enabled", Map.of()));

        // managementEndpointsWeb -> inputs -> "exposure"
        Map<String, Object> exposureInputs =
                (Map<String, Object>) managementEndpointsWeb.inputs().get("exposure");
        assertThat(exposureInputs).containsEntry("exclude", List.of());
        assertThat(exposureInputs)
                .containsEntry(
                        "include",
                        List.of(
                                Map.of(
                                        "value",
                                        "*",
                                        "origin",
                                        "\"management.endpoints.web.exposure.include\" from property source \"Inlined Test Properties\"")));
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        // when.
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // then.
        assertThatThrownBy(() -> configpropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        // when.
        String instanceId = "unregistered-instance";

        // then.
        assertThatThrownBy(() -> configpropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
