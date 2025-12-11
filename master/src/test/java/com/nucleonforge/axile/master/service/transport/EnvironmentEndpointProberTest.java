/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.Property;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;
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
 * @author Sergey Cherkasov
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
              "sourceName": "servletContextInitParams",
              "sourceDescription": "Contains the initialization parameters of the 'ServletContext', defined in 'web.xml' or set via 'ServletContext.setInitParameter()', and has higher priority than properties in 'jndiProperties' and 'StandardEnvironment'",
              "properties": []
            },
            {
              "sourceName": "systemProperties",
              "sourceDescription": "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'",
              "properties": [
                {
                  "propertyName": "java.specification.version",
                  "value": "17",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null
                },
                {
                  "propertyName": "java.vm.vendor",
                  "value": "BellSoft",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null
                }
              ]
            },
            {
              "sourceName": "systemEnvironment",
              "sourceDescription": "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application.*'",
              "properties": [
                {
                  "propertyName": "JAVA_HOME",
                  "value": "Java_Liberica_jdk/17.0.16-12/x64",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": "System Environment Property \\"JAVA_HOME\\""
                },
                {
                  "propertyName": "logging.path",
                  "value": "pattern",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": "Location of the log file. For instance, `/var/log`.",
                  "deprecation": {
                    "reason": null,
                    "replacement": "logging.file.path"
                  }
                }
              ]
            },
            {
              "sourceName": "Config resource classpath:actuate/env/",
              "sourceDescription": "Contains properties from the 'application.*' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources.",
              "properties": [
                {
                  "propertyName": "com.example.cache.max-size",
                  "value": "1000",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": null
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

                if (path.equals("/" + activeInstanceId + "/actuator/axile-env")) {
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
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        EnvironmentFeed feed =
                environmentEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        assertThat(feed).isNotNull();

        assertThat(feed.activeProfiles()).containsOnly("production");
        assertThat(feed.defaultProfiles()).containsOnly("test", "default");

        PropertySource servletParams = feed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("servletContextInitParams"))
                .findFirst()
                .orElseThrow();
        assertThat(servletParams.properties()).isEmpty();
        assertThat(servletParams.sourceDescription())
                .isEqualTo(
                        "Contains the initialization parameters of the 'ServletContext', defined in 'web.xml' or set via 'ServletContext.setInitParameter()', and has higher priority than properties in 'jndiProperties' and 'StandardEnvironment'");

        PropertySource systemProperties = feed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemProperties"))
                .findFirst()
                .orElseThrow();
        assertThat(systemProperties.properties()).hasSize(2);
        assertThat(systemProperties.sourceDescription())
                .isEqualTo(
                        "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'");

        Property javaSpecVersion = systemProperties.properties().stream()
                .filter(pv -> pv.propertyName().equals("java.specification.version"))
                .findFirst()
                .orElseThrow();
        assertThat(javaSpecVersion.value()).isEqualTo("17");
        assertThat(javaSpecVersion.isPrimary()).isTrue();
        assertThat(javaSpecVersion.configPropsBeanName())
                .isEqualTo("org.springframework.boot.test.property.SystemProperties");
        assertThat(javaSpecVersion.description()).isNull();
        assertThat(javaSpecVersion.deprecation()).isNull();

        Property javaVmVendor = systemProperties.properties().stream()
                .filter(pv -> pv.propertyName().equals("java.vm.vendor"))
                .findFirst()
                .orElseThrow();
        assertThat(javaVmVendor.value()).isEqualTo("BellSoft");
        assertThat(javaVmVendor.isPrimary()).isTrue();
        assertThat(javaVmVendor.configPropsBeanName())
                .isEqualTo("org.springframework.boot.test.property.SystemProperties");
        assertThat(javaVmVendor.description()).isNull();
        assertThat(javaVmVendor.deprecation()).isNull();

        PropertySource systemEnvironment = feed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("systemEnvironment"))
                .findFirst()
                .orElseThrow();
        assertThat(systemEnvironment.sourceDescription())
                .isEqualTo(
                        "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application.*'");

        Property javaHome = systemEnvironment.properties().stream()
                .filter(pv -> pv.propertyName().equals("JAVA_HOME"))
                .findFirst()
                .orElseThrow();
        assertThat(javaHome.value()).isEqualTo("Java_Liberica_jdk/17.0.16-12/x64");
        assertThat(javaHome.isPrimary()).isTrue();
        assertThat(javaHome.configPropsBeanName()).isNull();
        assertThat(javaHome.description()).isEqualTo("System Environment Property \"JAVA_HOME\"");
        assertThat(javaHome.deprecation()).isNull();

        Property loggingPath = systemEnvironment.properties().stream()
                .filter(pv -> pv.propertyName().equals("logging.path"))
                .findFirst()
                .orElseThrow();
        assertThat(loggingPath.value()).isEqualTo("pattern");
        assertThat(loggingPath.isPrimary()).isTrue();
        assertThat(loggingPath.configPropsBeanName()).isNull();
        assertThat(loggingPath.description()).isEqualTo("Location of the log file. For instance, `/var/log`.");
        assertThat(loggingPath.deprecation()).isNotNull();
        assertThat(loggingPath.deprecation().reason()).isNull();
        assertThat(loggingPath.deprecation().replacement()).isEqualTo("logging.file.path");

        PropertySource configResource = feed.propertySources().stream()
                .filter(ps -> ps.sourceName().equals("Config resource classpath:actuate/env/"))
                .findFirst()
                .orElseThrow();
        assertThat(configResource.sourceDescription())
                .isEqualTo(
                        "Contains properties from the 'application.*' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources.");

        Property cacheMaxSize = configResource.properties().stream()
                .filter(pv -> pv.propertyName().equals("com.example.cache.max-size"))
                .findFirst()
                .orElseThrow();
        assertThat(cacheMaxSize.value()).isEqualTo("1000");
        assertThat(cacheMaxSize.isPrimary()).isTrue();
        assertThat(cacheMaxSize.configPropsBeanName()).isNull();
        assertThat(cacheMaxSize.description()).isNull();
        assertThat(cacheMaxSize.deprecation()).isNull();
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
