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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.DetailsApi;
import com.axelixlabs.axelix.master.domain.Instance;
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
 * Integration tests for {@link DetailsApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class DetailsApiTest {

    private static final String EXPECTED_DETAILS_JSON =
            // language=json
            """
         {
           "serviceName": "test-object-factory-instance",
           "git": {
             "commitShaShort": "7a663cb",
             "branch": "local/local-test",
             "authorName": "NikitaKirilloff",
             "authorEmail": "NikitaKirilloff@github.com",
             "commitTimestamp": "2025-11-23T02:25:22Z"
           },
           "runtime": {
             "javaVersion": "17.0.16",
             "kotlinVersion": "1.9.0",
             "jdkVendor": "Corretto-17.0.16.8.1",
             "garbageCollector": "G1 GC"
           },
           "spring": {
             "springBootVersion": "3.5.0",
             "springFrameworkVersion": "7.0",
             "springCloudVersion": "4.1.4"
           },
           "build": {
             "artifact": "spring-petclinic",
             "version": "3.5.0-SNAPSHOT",
             "group": "org.springframework.samples",
             "time": "2025-10-29T15:10:54.770Z"
           },
           "os": {
             "name": "Windows 10",
             "version": "10.0",
             "arch": "amd64"
           },
           "vmFeatures": [
             {
              "name" : "AppCDS",
              "description" : "AppCDS Description",
              "enabled" : false
             },
             {
              "name" : "CompressedObjectHeaders",
              "description" : "CompressedObjectHeaders Description",
              "enabled" : true
             }
           ]
         }
        """;

    private static final String EXPECTED_DETAILS_JSON_WITHOUT_PLUGIN =
            // language=json
            """
     {
       "serviceName": "test-object-factory-instance",
       "git": {
             "commitShaShort": "",
             "branch": "",
             "authorName": "",
             "authorEmail": "",
             "commitTimestamp": ""
       },
       "runtime": {
         "javaVersion": "17.0.16",
         "kotlinVersion": "1.9.0",
         "jdkVendor": "Corretto-17.0.16.8.1",
         "garbageCollector": "G1 GC"
       },
       "spring": {
         "springBootVersion": "3.5.0",
         "springFrameworkVersion": "7.0",
         "springCloudVersion": "4.1.4"
       },
       "build": {
             "artifact": "",
             "version": "",
             "group": "",
             "time": ""
       },
       "os": {
         "name": "Windows 10",
         "version": "10.0",
         "arch": "amd64"
       },
       "vmFeatures": [
             {
              "name" : "AppCDS",
              "description" : "AppCDS Description",
              "enabled" : false
             },
             {
              "name" : "CompressedObjectHeaders",
              "description" : "CompressedObjectHeaders Description",
              "enabled" : true
             }
       ]
     }
    """;

    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String instanceWithoutPluginId = UUID.randomUUID().toString();

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
        String jsonResponse =
                """
        {
             "git": {
                 "commitShaShort": "7a663cb",
                 "branch": "local/local-test",
                 "commitAuthor": {
                     "name": "NikitaKirilloff",
                     "email": "NikitaKirilloff@github.com"
                 },
                 "commitTimestamp": "2025-11-23T02:25:22Z"
             },
             "spring": {
                 "springBootVersion": "3.5.0",
                 "springFrameworkVersion": "7.0",
                 "springCloudVersion": "4.1.4"
             },
             "runtime": {
                 "javaVersion": "17.0.16",
                 "jdkVendor": "Corretto-17.0.16.8.1",
                 "garbageCollector": "G1 GC",
                 "kotlinVersion": "1.9.0"
             },
             "build": {
                 "artifact": "spring-petclinic",
                 "version": "3.5.0-SNAPSHOT",
                 "group": "org.springframework.samples",
                 "time": "2025-10-29T15:10:54.770Z"
             },
             "os": {
                 "name": "Windows 10",
                 "version": "10.0",
                 "arch": "amd64"
             }
         }
        """;

        // language=json
        String jsonResponseWithoutPlugin =
                """
            {
              "git": {
                 "commitShaShort": "",
                 "branch": "",
                 "commitAuthor": {
                     "name": "",
                     "email": ""
                 },
                 "commitTimestamp": ""
             },
              "spring": {
                "springBootVersion": "3.5.0",
                "springFrameworkVersion": "7.0",
                "springCloudVersion": "4.1.4"
              },
              "runtime": {
                "javaVersion": "17.0.16",
                "jdkVendor": "Corretto-17.0.16.8.1",
                "garbageCollector": "G1 GC",
                "kotlinVersion": "1.9.0"
              },
              "build": {
                 "artifact": "",
                 "version": "",
                 "group": "",
                 "time": ""
             },
              "os": {
                "name": "Windows 10",
                "version": "10.0",
                "arch": "amd64"
              }
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-details")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + instanceWithoutPluginId + "/actuator/axelix-details")) {
                    return new MockResponse()
                            .setBody(jsonResponseWithoutPlugin)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(TestObjectFactory.createInstance(
                activeInstanceId,
                mockWebServer.url(activeInstanceId) + "/actuator",
                new Instance.VMFeature("AppCDS", "AppCDS Description", false),
                new Instance.VMFeature("CompressedObjectHeaders", "CompressedObjectHeaders Description", true)));

        registry.register(TestObjectFactory.createInstance(
                instanceWithoutPluginId,
                mockWebServer.url(instanceWithoutPluginId) + "/actuator",
                new Instance.VMFeature("AppCDS", "AppCDS Description", false),
                new Instance.VMFeature("CompressedObjectHeaders", "CompressedObjectHeaders Description", true)));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
        registry.deRegister(InstanceId.of(instanceWithoutPluginId));
    }

    @Test
    void shouldReturnJSONDetailsResponse() {
        // when.
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/details/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DETAILS_JSON);
    }

    @Test
    void shouldReturnJSONDetailsResponseWithoutPlugin() {
        // when.
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/details/{instanceId}", String.class, instanceWithoutPluginId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DETAILS_JSON_WITHOUT_PLUGIN);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/details/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/details/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .getForEntity("/axelix/api/external/details/{instanceId}", Void.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
