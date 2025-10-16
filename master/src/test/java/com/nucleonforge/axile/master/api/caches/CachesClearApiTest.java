package com.nucleonforge.axile.master.api.caches;

import java.io.IOException;
import java.util.Map;
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
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *  Integration tests for {@link CachesClearApi}
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CachesClearApiTest {

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
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/caches")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/caches/cities?cacheManager=cacheManager")) {
                    return new MockResponse();
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
    void shouldClearAllCaches() throws InterruptedException {
        // when
        restTemplate.delete("/api/axile/caches/{instanceId}", activeInstanceId);

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/actuator/caches");
    }

    @Test
    void shouldClearSpecificCache() throws InterruptedException {
        String cacheName = "cities";

        // when
        restTemplate.delete(
                "/api/axile/caches/{instanceId}/cache/{cacheName}?cacheManager=cacheManager",
                Map.of("instanceId", activeInstanceId, "cacheName", cacheName));
        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + activeInstanceId + "/actuator/caches/cities?cacheManager=cacheManager");
    }
}
