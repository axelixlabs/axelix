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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearRequest;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CacheDispatcherApi}.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CacheDispatcherApiTest {

    private static final String EXPECTED_CACHE_DISPATCHER_JSON =
            // language=json
            """
        {
          "cleared": false
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
        String jsonResponse = """
        {
          "cleared": false
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/cache-dispatcher/cacheManager/clear")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/cache-dispatcher/cacheManager/clear-all")) {
                    return new MockResponse()
                            .setBody(jsonResponse)
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
    void shouldReturnJSONCacheDispatcherClearAll() {
        String cacheManagerName = "cacheManager";

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/axile/cache-dispatcher/{instanceId}/{cacheManagerName}/clear-all",
                defaultEntity(null),
                String.class,
                activeInstanceId,
                cacheManagerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();

        assertThatJson(body).isEqualTo(EXPECTED_CACHE_DISPATCHER_JSON);
    }

    @Test
    void shouldReturnJSONCacheDispatcherClear() {
        String cacheManagerName = "cacheManager";
        CacheDispatcherClearRequest request = new CacheDispatcherClearRequest("cacheName", "key");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/axile/cache-dispatcher/{instanceId}/{cacheManagerName}/clear",
                defaultEntity(request),
                String.class,
                activeInstanceId,
                cacheManagerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();

        assertThatJson(body).isEqualTo(EXPECTED_CACHE_DISPATCHER_JSON);
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when clearing all caches fails")
    void shouldReturnInternalServerErrorOnClearAll() {
        String instanceId = UUID.randomUUID().toString();
        String cacheManagerName = "cacheManager";

        registry.register(createInstance(instanceId));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/axile/cache-dispatcher/{instanceId}/{cacheManagerName}/clear-all",
                defaultEntity(null),
                String.class,
                instanceId,
                cacheManagerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when clearing cache fails")
    void shouldReturnInternalServerErrorOnClear() {
        String instanceId = UUID.randomUUID().toString();
        String cacheManagerName = "cacheManager";
        CacheDispatcherClearRequest request = new CacheDispatcherClearRequest("cacheName", null);

        registry.register(createInstance(instanceId));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/axile/cache-dispatcher/{instanceId}/{cacheManagerName}/clear",
                defaultEntity(request),
                String.class,
                instanceId,
                cacheManagerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when clearing cache for unregistered instance")
    void shouldReturnBadRequestForUnregisteredInstanceOnClearAll() {
        String cacheManagerName = "cacheManager";
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/cache-dispatcher/{instanceId}/{cacheManagerName}/clear-all",
                defaultEntity(null),
                EndpointInvocationException.class,
                instanceId,
                cacheManagerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when clearing all caches for unregistered instance")
    void shouldReturnBadRequestForUnregisteredInstanceOnClear() {
        String instanceId = UUID.randomUUID().toString();
        String cacheManagerName = "cacheManager";
        CacheDispatcherClearRequest request = new CacheDispatcherClearRequest("cacheName", "key");

        ResponseEntity<EndpointInvocationException> response = restTemplate.postForEntity(
                "/api/axile/cache-dispatcher/{instanceId}/{cacheManagerName}/clear",
                defaultEntity(request),
                EndpointInvocationException.class,
                instanceId,
                cacheManagerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private HttpEntity<?> defaultEntity(Object request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }
}
