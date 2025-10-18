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

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link EnvironmentPropertyEndpointProber}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class EnvironmentPropertyEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private EnvironmentPropertyEndpointProber environmentPropertyEndpointProber;

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
              "property": {
              "source": "servletConfigInitParams",
              "value": "Amazon.com Inc."
              },
              "activeProfiles": ["production"],
              "defaultProfiles": ["default", "test"],
              "propertySources": [
              {
                "name": "server.ports"
              },
              {
                "name": "servletConfigInitParams",
                "property": {
                  "value": "Amazon.com Inc."
                }
              },
              {
                "name": "servletContextInitParams"
              },
              {
                "name": "systemProperties",
                "property": {
                  "value": "Amazon"
                }
              }
             ]
            }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/env/java.vendor")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
        ;
    }

    @Test
    void shouldReturnEnvironmentProperty() {
        String propertyName = "java.vendor";

        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        HttpPayload payload = new DefaultHttpPayload(Map.of("property.name", propertyName));

        EnvironmentProperty environmentProperty =
                environmentPropertyEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        assertThat(environmentProperty).isNotNull();
        assertThat(environmentProperty.activeProfiles()).containsOnly("production");
        assertThat(environmentProperty.defaultProfiles()).containsOnly("default", "test");
        assertThat(environmentProperty.property().source()).isEqualTo("servletConfigInitParams");
        assertThat(environmentProperty.property().value()).isEqualTo("Amazon.com Inc.");
        assertThat(environmentProperty.propertySources()).hasSize(4);

        EnvironmentProperty.SourceEntry serverPortsEntry =
                environmentProperty.propertySources().get(0);
        assertThat(serverPortsEntry.sourceName()).isEqualTo("server.ports");
        assertThat(serverPortsEntry.property()).isNull();

        EnvironmentProperty.SourceEntry servletConfigInitEntry =
                environmentProperty.propertySources().get(1);
        assertThat(servletConfigInitEntry.sourceName()).isEqualTo("servletConfigInitParams");
        assertThat(servletConfigInitEntry.property()).isNotNull();
        assertThat(servletConfigInitEntry.property().value()).isNotNull().isEqualTo("Amazon.com Inc.");

        EnvironmentProperty.SourceEntry servletContextInitEntry =
                environmentProperty.propertySources().get(2);
        assertThat(servletContextInitEntry.sourceName()).isEqualTo("servletContextInitParams");
        assertThat(servletContextInitEntry.property()).isNull();

        EnvironmentProperty.SourceEntry systemPropertiesEntry =
                environmentProperty.propertySources().get(3);
        assertThat(systemPropertiesEntry.sourceName()).isEqualTo("systemProperties");
        assertThat(systemPropertiesEntry.property()).isNotNull();
        assertThat(systemPropertiesEntry.property().value()).isNotNull().isEqualTo("Amazon");
    }
}
