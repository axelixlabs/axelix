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

import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.SingleValueQueryParameter;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GetCacheByNameEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class GetCacheByNameEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private GetCacheByNameEndpointProber getCacheByNameEndpointProber;

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
        String jsonResponseCities =
                """
            {
              "target" : "java.util.concurrent.ConcurrentHashMap",
              "name" : "cities",
              "cacheManager" : "cacheManager"
            }
            """;

        // language=json
        String jsonResponseCountries =
                """
            {
              "target" : "java.util.concurrent.ConcurrentHashMap",
              "name" : "countries",
              "cacheManager" : "cacheManager"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/caches/cities")) {
                    return new MockResponse()
                            .setBody(jsonResponseCities)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/caches/countries?cacheManager=cacheManager")) {
                    return new MockResponse()
                            .setBody(jsonResponseCountries)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
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
    void shouldReturnSingleCache() {
        String cacheName = "cities";
        HttpPayload payload = new DefaultHttpPayload(Map.of("name", cacheName));

        // when.
        SingleCache cache = getCacheByNameEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        // then
        assertThat(cache.name()).isEqualTo("cities");
        assertThat(cache.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(cache.cacheManager()).isEqualTo("cacheManager");
    }

    @Test
    void shouldReturnSingleCacheWithQueryParameter() {
        String cacheName = "countries";
        HttpPayload payload = new DefaultHttpPayload(
                List.of(new SingleValueQueryParameter("cacheManager", "cacheManager")), Map.of("name", cacheName));

        // when.
        SingleCache cache = getCacheByNameEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        // then
        assertThat(cache.name()).isEqualTo("countries");
        assertThat(cache.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(cache.cacheManager()).isEqualTo("cacheManager");
    }
}
