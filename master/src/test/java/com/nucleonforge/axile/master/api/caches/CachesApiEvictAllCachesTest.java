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
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.CachesApi;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CachesApiEvictAllCachesTest {

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

                if (path.equals("/" + activeInstanceId + "/caches")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });
    }

    @Test
    void shouldEvictAllCaches() throws InterruptedException {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // when
        restTemplate.delete("/api/axile/caches/{instanceId}", activeInstanceId);

        // then.
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("DELETE");
        assertThat(recordedRequest.getPath()).isEqualTo("/" + activeInstanceId + "/caches");
    }
}
