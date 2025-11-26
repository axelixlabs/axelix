package com.nucleonforge.axile.master.api.caches;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesManagementApi}
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CachesManagementApiTest {

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
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/cache-dispatcher/cacheManager/vets/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals(
                        "/" + activeInstanceId + "/actuator/cache-dispatcher/cacheManager/vets/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/cache-dispatcher/cacheManager/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/cache-dispatcher/cacheManager/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/cache-dispatcher/enable-all-cache")) {
                    return new MockResponse().setResponseCode(200);
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
    void shouldEnableSpecificCache() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager", "cacheName", "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableSpecificCache() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/disable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager", "cacheName", "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldEnableCacheManager() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/enable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableCacheManager() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/disable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldEnableAllCaches() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/enable-all-cache",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnInternalServerErrorWhenInstanceReturns404() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "unknown", "cacheName", "unknown"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                null,
                Void.class,
                Map.of(
                        "instanceId",
                        UUID.randomUUID().toString(),
                        "cacheManagerName",
                        "cacheManager",
                        "cacheName",
                        "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
