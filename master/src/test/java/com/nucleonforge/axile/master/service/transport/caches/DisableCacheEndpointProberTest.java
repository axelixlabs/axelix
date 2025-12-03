package com.nucleonforge.axile.master.service.transport.caches;

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

import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link DisableCacheEndpointProber}.
 *
 * @author Nikita Kirillov
 * @since 26.11.2025
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class DisableCacheEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private DisableCacheEndpointProber disableCacheEndpointProber;

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

                if (path.equals("/" + activeInstanceId + "/actuator/axile-caches/cacheManager/vets/disable")) {
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
    void shouldDisableCache() {
        HttpPayload payload = new DefaultHttpPayload(Map.of("cacheManagerName", "cacheManager", "cacheName", "vets"));

        assertThatNoException()
                .isThrownBy(() -> disableCacheEndpointProber.invoke(InstanceId.of(activeInstanceId), payload));
    }

    @Test
    void shouldThrowExceptionWhenInstanceNotFound() {
        HttpPayload payload = new DefaultHttpPayload(Map.of("cacheManagerName", "cacheManager", "cacheName", "vets"));
        InstanceId unregisteredInstanceId = InstanceId.of(UUID.randomUUID().toString());

        assertThatThrownBy(() -> disableCacheEndpointProber.invoke(unregisteredInstanceId, payload))
                .isInstanceOf(InstanceNotFoundException.class);
    }
}
