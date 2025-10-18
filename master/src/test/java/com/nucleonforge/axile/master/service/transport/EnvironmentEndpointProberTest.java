package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
import java.util.List;
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

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.PropertyValue;
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
 * Integration tests for {@link EnvironmentEndpointProber}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class EnvironmentEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private EnvironmentEndpointProber environmentEndpointProber;

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
              "activeProfiles": ["production"],
              "defaultProfiles": ["default", "test"],
              "propertySources": [
                {
                  "name": "servletContextInitParams",
                  "properties": {}
                },
                {
                  "name": "systemProperties",
                  "properties": {
                    "java.specification.version": {
                      "value": "17"
                    },
                    "java.vm.vendor": {
                      "value": "BellSoft"
                    }
                  }
                },
                {
                  "name": "systemEnvironment",
                  "properties": {
                    "JAVA_HOME": {
                      "value": "Java_Liberica_jdk/17.0.16-12/x64",
                      "origin": "System Environment Property \\"JAVA_HOME\\""
                    }
                  }
                },
                {
                  "name": "Config resource classpath:actuate/env/",
                  "properties": {
                    "com.example.cache.max-size": {
                      "value": "1000",
                      "origin": "class path resource [application.properties]"
                    }
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

                if (path.equals("/" + activeInstanceId + "/env")) {
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
    void shouldReturnEnvironmentFeed() {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        EnvironmentFeed feed =
                environmentEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        assertThat(feed).isNotNull();

        List<String> activeProfiles = feed.activeProfiles();
        assertThat(activeProfiles).containsOnly("production");

        List<String> defaultProfiles = feed.defaultProfiles();
        assertThat(defaultProfiles).containsOnly("test", "default");

        EnvironmentFeed.PropertySource servletParams = feed.propertySources().stream()
                .filter(name -> name.sourceName().equals("servletContextInitParams"))
                .findFirst()
                .orElseThrow();
        assertThat(servletParams.sourceName()).isEqualTo("servletContextInitParams");
        assertThat(servletParams.properties()).isEmpty();

        EnvironmentFeed.PropertySource systemProperties = feed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemProperties"))
                .findFirst()
                .orElseThrow();
        assertThat(systemProperties.sourceName()).isEqualTo("systemProperties");
        assertThat(systemProperties.properties())
                .hasSize(2)
                .containsKeys("java.specification.version", "java.vm.vendor");

        PropertyValue javaSpecVersion = systemProperties.properties().get("java.specification.version");
        assertThat(javaSpecVersion.value()).isEqualTo("17");
        assertThat(javaSpecVersion.origin()).isNull();

        PropertyValue javaVmVendor = systemProperties.properties().get("java.vm.vendor");
        assertThat(javaVmVendor.value()).isEqualTo("BellSoft");
        assertThat(javaVmVendor.origin()).isNull();

        EnvironmentFeed.PropertySource systemEnvironment = feed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemEnvironment"))
                .findFirst()
                .orElseThrow();
        assertThat(systemEnvironment.properties()).hasSize(1).containsKey("JAVA_HOME");

        PropertyValue javaHome = systemEnvironment.properties().get("JAVA_HOME");
        assertThat(javaHome.value()).isEqualTo("Java_Liberica_jdk/17.0.16-12/x64");
        assertThat(javaHome.origin()).isEqualTo("System Environment Property \"JAVA_HOME\"");

        EnvironmentFeed.PropertySource configResource = feed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("Config resource classpath:actuate/env/"))
                .findFirst()
                .orElseThrow();
        assertThat(configResource.properties()).hasSize(1).containsKey("com.example.cache.max-size");

        PropertyValue cacheMaxSize = configResource.properties().get("com.example.cache.max-size");
        assertThat(cacheMaxSize.value()).isEqualTo("1000");
        assertThat(cacheMaxSize.origin()).isEqualTo("class path resource [application.properties]");
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsUnreachable() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        assertThatThrownBy(() -> environmentEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionForUnregisteredInstance() {
        String instanceId = "unregistered-instance";

        assertThatThrownBy(() -> environmentEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
