package com.nucleonforge.axile.master.service.transport.caches;

import java.io.IOException;
import java.util.List;
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

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.SingleValueQueryParameter;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link EvictCacheByNameEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class EvictCacheByNameEndpointProberTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private EvictCacheByNameEndpointProber evictCacheByNameEndpointProber;

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

                if (path.equals("/" + activeInstanceId + "/caches/cities")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/caches/countries?cacheManager=cacheManager")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldEvictCacheByName() throws InterruptedException {
        String cacheName = "cities";
        HttpPayload payload = new DefaultHttpPayload(Map.of("name", cacheName));

        // when
        evictCacheByNameEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/caches/cities");
    }

    @Test
    void shouldEvictCacheByNameWithParameter() throws InterruptedException {
        String cacheName = "countries";
        HttpPayload payload = new DefaultHttpPayload(
                List.of(new SingleValueQueryParameter("cacheManager", "cacheManager")), Map.of("name", cacheName));

        // when
        evictCacheByNameEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath())
                .isEqualTo("/" + activeInstanceId + "/caches/countries?cacheManager=cacheManager");
    }
}
