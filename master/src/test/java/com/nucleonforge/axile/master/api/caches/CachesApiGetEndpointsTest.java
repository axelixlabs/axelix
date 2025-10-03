package com.nucleonforge.axile.master.api.caches;

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

import com.nucleonforge.axile.common.domain.InstanceId;
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
public class CachesApiGetEndpointsTest {

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
        String jsonResponseAllCaches =
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

        // language=json
        String jsonResponseSingleCache =
                """
            {
              "target" : "java.util.concurrent.ConcurrentHashMap",
              "name" : "cities",
              "cacheManager" : "cacheManager"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/caches")) {
                    return new MockResponse()
                            .setBody(jsonResponseAllCaches)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/caches/cities?cacheManager=cacheManager")) {
                    return new MockResponse()
                            .setBody(jsonResponseSingleCache)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(createInstanceWithUrl(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnJSONCachesResponse() {
        // language=json
        String expectedAllCachesJSON =
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
        // when
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/axile/caches/{instanceId}", String.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedAllCachesJSON);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorCachesResponse() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<?> response =
                restTemplate.getForEntity("/api/axile/caches/{instanceId}", Void.class, instanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceCachesResponse() {
        String instanceId = "unregistered-caches-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/caches/{instanceId}", EndpointInvocationException.class, instanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnJSONCacheProfileResponse() {
        // language=json
        String extendsCacheCities =
                """
            {
              "name": "cities",
              "target": "java.util.concurrent.ConcurrentHashMap",
              "cacheManager": "cacheManager"
            }
            """;
        String cacheName = "cities";

        // when.
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/axile/caches/{instanceId}/cache/{cacheName}?cacheManager=cacheManager",
                String.class,
                activeInstanceId,
                cacheName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER).isEqualTo(extendsCacheCities);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorCacheProfileResponse() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));
        String cacheName = "cities";

        // when.
        ResponseEntity<?> response = restTemplate.getForEntity(
                "/api/axile/caches/{instanceId}/cache/{cacheName}?cacheManager=cacheManager",
                Void.class,
                instanceId,
                cacheName);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceCacheProfileResponse() {
        String instanceId = "unregistered-single-caches-instance";
        String cacheName = "cities";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate.getForEntity(
                "/api/axile/caches/{instanceId}/cache/{cacheName}?cacheManager=cacheManager",
                EndpointInvocationException.class,
                instanceId,
                cacheName);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
