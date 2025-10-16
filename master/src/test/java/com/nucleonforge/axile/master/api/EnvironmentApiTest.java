package com.nucleonforge.axile.master.api;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link EnvironmentApi}.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EnvironmentApiTest {

    private static final String EXPECTED_ENV_JSON =
            // language=json
            """
        {
          "activeProfiles": ["production"],
          "defaultProfiles": ["default", "development"],
          "propertySources": [
            {
              "name": "systemProperties",
              "properties": [
                {
                  "key": "java.vm.vendor",
                  "value": "BellSoft"
                },
                {
                  "key": "java.specification.version",
                  "value": "17"
                }
              ]
            },
            {
              "name": "systemEnvironment",
              "properties": [
                {
                  "key": "JAVA_HOME",
                  "value": "/opt/hostedtoolcache/Java_Liberica_jdk/17.0.16-12/x64"
                }
              ]
            }
          ]
        }
        """;

    private static final String EXPECTED_ENV_PROPERTY_JSON =
            // language=json
            """
        {
          "source": "systemProperties",
          "value": "HotSpot 64-Bit Tiered Compilers",
          "propertySources": [
                {
                  "name": "systemProperties",
                  "property": {
                    "value": "HotSpot 64-Bit Tiered Compilers"
                  }
                },
                {
                  "name": "systemEnvironment",
                  "property": {
                    "value": "HotSpot 64-Bit"
                  }
                }
          ]
        }
        """;

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InstanceRegistry registry;

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
        String jsonEnvResponse =
                """
            {
              "activeProfiles": ["production"],
              "defaultProfiles": ["default","development"],
              "propertySources": [
                {
                  "name": "systemProperties",
                  "properties": {
                    "java.vm.vendor": {
                      "value": "BellSoft"
                    },
                    "java.specification.version": {
                      "value": "17"
                    }
                  }
                },
                {
                  "name": "systemEnvironment",
                  "properties": {
                    "JAVA_HOME": {
                      "value": "/opt/hostedtoolcache/Java_Liberica_jdk/17.0.16-12/x64",
                      "origin": "System Environment Property \\"JAVA_HOME\\""
                    }
                  }
                }
              ]
            }
        """;

        // language=json
        String jsonEnvPropertyResponse =
                """
            {
              "property": {
                "source": "systemProperties",
                "value": "HotSpot 64-Bit Tiered Compilers"
              },
              "activeProfiles": ["production"],
              "defaultProfiles": ["default", "development"],
              "propertySources": [
                {
                  "name": "server.ports"
                },
                {
                  "name": "servletConfigInitParams"
                },
                {
                  "name": "servletContextInitParams"
                },
                {
                  "name": "systemProperties",
                  "property": {
                    "value": "HotSpot 64-Bit Tiered Compilers"
                  }
                },
                {
                  "name": "systemEnvironment",
                  "property": {
                    "value": "HotSpot 64-Bit"
                  }
                },
                {
                  "name": "springCloudClientHostInfo"
                },
                {
                  "name": "Management Server"
                }
              ]
            }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/env")) {
                    return new MockResponse()
                            .setBody(jsonEnvResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/env/sun.management.compiler")) {
                    return new MockResponse()
                            .setBody(jsonEnvPropertyResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
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
    void shouldReturnJSONEnvironmentFeed() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/env/feed/{instanceId}", String.class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();

        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_ENV_JSON);
    }

    @Test
    void shouldReturnJSONEnvironmentProperty() {
        String propertyName = "sun.management.compiler";

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/axile/env/{instanceId}/property/{propertyName}", String.class, activeInstanceId, propertyName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();

        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_ENV_PROPERTY_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when fetching EnvironmentFeed")
    void shouldReturnInternalServerErrorOnEnvFeed() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/env/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when fetching EnvironmentProperty")
    void shouldReturnInternalServerErrorOnEnvProperty() {
        String propertyName = "sun.management.compiler";
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/axile/env/{instanceId}/property/{propertyName}", String.class, instanceId, propertyName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = "unregistered-env-instance";

        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/env/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
