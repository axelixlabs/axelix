package com.nucleonforge.axile.master.service.transport.caches;

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
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link EvictAllCachesEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class EvictAllCachesEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private EvictAllCachesEndpointProber evictAllCachesEndpointProber;

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

                if (path.equals("/" + activeInstanceId + "/caches")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldDeleteAllCaches() throws InterruptedException {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // when
        evictAllCachesEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/caches");
    }
}
