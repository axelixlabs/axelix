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

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;
import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigPropsByPrefixEndpointProber;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ConfigPropsByPrefixEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class ConfigPropsByPrefixEndpointProberTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private ConfigPropsByPrefixEndpointProber configPropsByPrefixEndpointProber;

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
                    "application1" : {
                      "beans" : {
                        "org.springframework.boot.actuate.autoconfigure.endpoint.web.Bean" : {
                          "prefix" : "management.endpoints.web.cors",
                          "properties": [
                            { "key": "allowedOrigins", "value": null },
                            { "key": "maxAge", "value": "PT30M" },
                            { "key": "exposedHeaders", "value": null },
                            { "key": "allowedOriginPatterns", "value": null },
                            { "key": "allowedHeaders", "value": null },
                            { "key": "allowedMethods", "value": null }
                          ],
                          "inputs": [
                            { "key": "allowedOrigins", "value": null },
                            { "key": "maxAge", "value": null },
                            { "key": "exposedHeaders", "value": null },
                            { "key": "allowedOriginPatterns", "value": null },
                            { "key": "allowedHeaders", "value": null },
                            { "key": "allowedMethods", "value": null }
                          ]
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

                if (path.equals("/" + activeInstanceId + "/actuator/axile-configprops/management.endpoints.web.cors")) {
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
    void shouldReturnAxileConfigPropsFeed() {
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        // when.
        String prefix = "management.endpoints.web.cors";
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));

        AxileConfigPropsFeed configPropsFeed =
                configPropsByPrefixEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        // then.
        Map<String, AxileConfigPropsFeed.Context> context = configPropsFeed.contexts();

        // bean
        AxileConfigPropsFeed.Bean bean1 =
                getBeanByName(context, "org.springframework.boot.actuate.autoconfigure.endpoint.web.Bean");

        // bean -> prefix
        assertThat(bean1.prefix()).isEqualTo("management.endpoints.web.cors");

        // bean -> properties
        assertThat(bean1.properties())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", "PT30M"),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));

        // bean -> inputs
        assertThat(bean1.inputs())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", null),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        // when.
        String prefix = "management.endpoints.web.cors";
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));

        // then.
        assertThatThrownBy(() -> configPropsByPrefixEndpointProber.invoke(InstanceId.of(instanceId), payload))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        // when.
        String prefix = "management.endpoints.web.cors";
        String instanceId = "unregistered-instance";
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));

        // then.
        assertThatThrownBy(() -> configPropsByPrefixEndpointProber.invoke(InstanceId.of(instanceId), payload))
                .isInstanceOf(InstanceNotFoundException.class);
    }

    private static AxileConfigPropsFeed.Bean getBeanByName(
            Map<String, AxileConfigPropsFeed.Context> context, String beanName) {
        return context.values().stream()
                .map(AxileConfigPropsFeed.Context::beans)
                .findFirst()
                .map(beansMap -> beansMap.get(beanName))
                .get();
    }
}
