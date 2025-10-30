package com.nucleonforge.axile.master.api;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
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
             "commitTimestamp": "1761249922000"
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
           }
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
       }
     }
    """;

    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String instanceWithoutPluginId = UUID.randomUUID().toString();

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
                 "commitTimestamp": "1761249922000"
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

                if (path.equals("/" + activeInstanceId + "/actuator/axile-details")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + instanceWithoutPluginId + "/actuator/axile-details")) {
                    return new MockResponse()
                            .setBody(jsonResponseWithoutPlugin)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldReturnJSONDetailsResponse() {
        // when.
        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/details/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DETAILS_JSON);
    }

    @Test
    void shouldReturnJSONDetailsResponseWithoutPlugin() {
        // when.
        registry.register(createInstanceWithUrl(
                instanceWithoutPluginId, mockWebServer.url(instanceWithoutPluginId) + "/actuator"));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/details/{instanceId}", String.class, instanceWithoutPluginId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_DETAILS_JSON_WITHOUT_PLUGIN);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        registry.register(createInstance(instanceId));
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/details/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/details/{instanceId}", EndpointInvocationException.class, instanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
