package com.nucleonforge.axile.master.service.transport;

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

import com.nucleonforge.axile.common.api.ConfigPropsFeed;
import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ConfigPropsEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class ConfigPropsEndpointProberTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private ConfigPropsEndpointProber configPropsEndpointProber;

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
                        "org.springframework.boot.actuate.autoconfigure.endpoint.web.Bean1" : {
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
                        },
                        "org.springframework.boot.autoconfigure.web.Bean2" : {
                          "prefix" : "spring.web",
                          "properties": [
                            { "key": "localeResolver", "value": "ACCEPT_HEADER" },
                            { "key": "resources.staticLocations[0]", "value": "classpath:/META-INF/resources/" },
                            { "key": "resources.staticLocations[1]", "value": "classpath:/resources/" },
                            { "key": "resources.staticLocations[2]", "value": "classpath:/static/" },
                            { "key": "resources.staticLocations[3]", "value": "classpath:/public/" },
                            { "key": "resources.addMappings", "value": "true" },
                            { "key": "resources.chain.cache", "value": "true" },
                            { "key": "resources.chain.compressed", "value": "false" },
                            { "key": "resources.chain.strategy.fixed.enabled", "value": "false" },
                            { "key": "resources.chain.strategy.fixed.paths[0]", "value": "/**" },
                            { "key": "resources.chain.strategy.content.enabled", "value": "false" },
                            { "key": "resources.chain.strategy.content.paths[0]", "value": "/**" },
                            { "key": "resources.cache.cachecontrol", "value": null },
                            { "key": "resources.cache.useLastModified", "value": "true" }
                          ],
                          "inputs": [
                            { "key": "localeResolver", "value": null },
                            { "key": "resources.staticLocations[0]", "value": null },
                            { "key": "resources.staticLocations[1]", "value": null },
                            { "key": "resources.staticLocations[2]", "value": null },
                            { "key": "resources.staticLocations[3]", "value": null },
                            { "key": "resources.addMappings", "value": null },
                            { "key": "resources.chain.cache", "value": null },
                            { "key": "resources.chain.compressed", "value": null },
                            { "key": "resources.chain.strategy.fixed.enabled", "value": null },
                            { "key": "resources.chain.strategy.fixed.paths[0]", "value": null },
                            { "key": "resources.chain.strategy.content.enabled", "value": null },
                            { "key": "resources.chain.strategy.content.paths[0]", "value": null },
                            { "key": "resources.cache.cachecontrol", "value": null },
                            { "key": "resources.cache.useLastModified", "value": null }
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

                if (path.equals("/" + activeInstanceId + "/actuator/axile-configprops")) {
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
        ConfigPropsFeed configPropsFeed =
                configPropsEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // then.
        Map<String, ConfigPropsFeed.Context> context = configPropsFeed.contexts();

        // bean1
        ConfigPropsFeed.Bean bean1 =
                getBeanByName(context, "org.springframework.boot.actuate.autoconfigure.endpoint.web.Bean1");

        // bean1 -> prefix
        assertThat(bean1.prefix()).isEqualTo("management.endpoints.web.cors");

        // bean1 -> properties
        assertThat(bean1.properties())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", "PT30M"),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));

        // bean1 -> inputs
        assertThat(bean1.inputs())
                .containsOnly(
                        new KeyValue("allowedOrigins", null),
                        new KeyValue("maxAge", null),
                        new KeyValue("exposedHeaders", null),
                        new KeyValue("allowedOriginPatterns", null),
                        new KeyValue("allowedHeaders", null),
                        new KeyValue("allowedMethods", null));

        // bean2
        ConfigPropsFeed.Bean bean2 = getBeanByName(context, "org.springframework.boot.autoconfigure.web.Bean2");

        // bean2 -> prefix
        assertThat(bean2.prefix()).isEqualTo("spring.web");

        // bean2 -> properties
        assertThat(bean2.properties())
                .containsOnly(
                        new KeyValue("localeResolver", "ACCEPT_HEADER"),
                        new KeyValue("resources.staticLocations[0]", "classpath:/META-INF/resources/"),
                        new KeyValue("resources.staticLocations[1]", "classpath:/resources/"),
                        new KeyValue("resources.staticLocations[2]", "classpath:/static/"),
                        new KeyValue("resources.staticLocations[3]", "classpath:/public/"),
                        new KeyValue("resources.addMappings", "true"),
                        new KeyValue("resources.chain.cache", "true"),
                        new KeyValue("resources.chain.compressed", "false"),
                        new KeyValue("resources.chain.strategy.fixed.enabled", "false"),
                        new KeyValue("resources.chain.strategy.fixed.paths[0]", "/**"),
                        new KeyValue("resources.chain.strategy.content.enabled", "false"),
                        new KeyValue("resources.chain.strategy.content.paths[0]", "/**"),
                        new KeyValue("resources.cache.cachecontrol", null),
                        new KeyValue("resources.cache.useLastModified", "true"));

        // bean2 -> inputs
        assertThat(bean2.inputs())
                .containsOnly(
                        new KeyValue("localeResolver", null),
                        new KeyValue("resources.staticLocations[0]", null),
                        new KeyValue("resources.staticLocations[1]", null),
                        new KeyValue("resources.staticLocations[2]", null),
                        new KeyValue("resources.staticLocations[3]", null),
                        new KeyValue("resources.addMappings", null),
                        new KeyValue("resources.chain.cache", null),
                        new KeyValue("resources.chain.compressed", null),
                        new KeyValue("resources.chain.strategy.fixed.enabled", null),
                        new KeyValue("resources.chain.strategy.fixed.paths[0]", null),
                        new KeyValue("resources.chain.strategy.content.enabled", null),
                        new KeyValue("resources.chain.strategy.content.paths[0]", null),
                        new KeyValue("resources.cache.cachecontrol", null),
                        new KeyValue("resources.cache.useLastModified", null));
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        // when.
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));
        assertThatThrownBy(() -> configPropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                // then.
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        // when.
        String instanceId = "unregistered-instance";
        assertThatThrownBy(() -> configPropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                // then.
                .isInstanceOf(InstanceNotFoundException.class);
    }

    private static ConfigPropsFeed.Bean getBeanByName(Map<String, ConfigPropsFeed.Context> context, String beanName) {
        return context.values().stream()
                .map(ConfigPropsFeed.Context::beans)
                .findFirst()
                .map(beansMap -> beansMap.get(beanName))
                .get();
    }
}
