/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.api;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.EnvironmentApi;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link EnvironmentApi}.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
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
              "description": "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'",
              "properties": [
                {
                  "name": "java.specification.version",
                  "value": "17",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null
                },
                {
                  "name": "java.vm.vendor",
                  "value": "BellSoft",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null,
                  "injectionPoints": [
                    {
                      "beanName": "systemPropertiesBean",
                      "injectionType": "FIELD",
                      "targetName": "vendorField",
                      "propertyExpression": "${java.vm.vendor}"
                    }
                  ]
                }
              ]
            },
            {
              "name": "systemEnvironment",
              "description": "Contains all OS environment variables available to the 'JVM' process and has higher priority than properties from 'application.*'",
              "properties": [
                {
                  "name": "JAVA_HOME",
                  "value": "Java_Liberica_jdk/17.0.16-12/x64",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": "System Environment Property \\"JAVA_HOME\\""
                },
                {
                  "name": "logging.path",
                  "value": "pattern",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": "Location of the log file. For instance, `/var/log`.",
                  "deprecation": {
                      "message": "Deprecated in favor of logging.file.path property."
                  }
                }
              ]
            },
            {
              "name": "Config resource classpath:actuate/env/",
              "description": "Contains properties from the 'application.*' configuration file loaded from the classpath (optional:classpath:/) and serves as one of the primary Spring Boot configuration sources.",
              "properties": [
                {
                  "name": "com.example.cache.max-size",
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

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

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
          "defaultProfiles": ["default", "development"],
          "propertySources": [
            {
              "sourceName": "systemProperties",
              "sourceDescription": "Contains all Java system properties (those set via -Dkey=value at JVM startup, as well as properties set via 'System.setProperty()' at runtime) and has higher priority than properties in 'systemEnvironment'",
              "properties": [
                {
                  "propertyName": "java.specification.version",
                  "value": "17",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null,
                  "injectionPoints": null
                },
                {
                  "propertyName": "java.vm.vendor",
                  "value": "BellSoft",
                  "isPrimary": true,
                  "configPropsBeanName": "org.springframework.boot.test.property.SystemProperties",
                  "description": null,
                  "injectionPoints": [
                    {
                      "beanName": "systemPropertiesBean",
                      "injectionType": "FIELD",
                      "targetName": "vendorField",
                      "propertyExpression": "${java.vm.vendor}"
                    }
                  ]
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
                  "description": "System Environment Property \\"JAVA_HOME\\"",
                  "injectionPoints": null
                },
                {
                  "propertyName": "logging.path",
                  "value": "pattern",
                  "isPrimary": true,
                  "configPropsBeanName": null,
                  "description": "Location of the log file. For instance, `/var/log`.",
                  "deprecation": {
                      "message": "Deprecated in favor of logging.file.path property."
                  },
                  "injectionPoints": null
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

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-env")) {
                    return new MockResponse()
                            .setBody(jsonEnvResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONEnvironmentFeed() {
        // when.
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/env/feed/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_ENV_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError when fetching EnvironmentFeed")
    void shouldReturnInternalServerErrorOnEnvFeed() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/env/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = "unregistered-env-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/env/feed/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorizedOnEnvFeed(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .getForEntity("/axelix/api/external/env/feed/{instanceId}", Void.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
