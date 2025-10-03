package com.nucleonforge.axile.master.api.caches;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.CachesApi;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CachesApiGetAllCachesTest {

    // language=json
    private static final String EXPECTED_ALL_CACHES_JSON =
            """
    {
      "cacheManagers": [
        {
          "name": "anotherCacheManager",
          "caches": [
            {
              "name": "countries",
              "target": "java.util.concurrent.ConcurrentHashMap"
            }
          ]
        },
        {
          "name": "cacheManager",
          "caches": [
            {
              "name": "cities",
              "target": "java.util.concurrent.ConcurrentHashMap"
            },
            {
              "name": "countries",
              "target": "java.util.concurrent.ConcurrentHashMap"
            }
          ]
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
        String jsonResponse =
                """
            {
          "cacheManagers" : {
            "anotherCacheManager" : {
              "caches" : {
                "countries" : {
                  "target" : "java.util.concurrent.ConcurrentHashMap"
                }
              }
            },
            "cacheManager" : {
              "caches" : {
                "cities" : {
                  "target" : "java.util.concurrent.ConcurrentHashMap"
                },
                "countries" : {
                  "target" : "java.util.concurrent.ConcurrentHashMap"
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

                if (path.equals("/" + activeInstanceId + "/caches")) {
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
    void shouldReturnJSONCachesResponse() {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // when
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/caches/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_ALL_CACHES_JSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<?> response =
                restTemplate.getForEntity("/api/axile/caches/{instanceId}", Void.class, instanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = "unregistered-loggers-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/caches/{instanceId}", EndpointInvocationException.class, instanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
