package com.nucleonforge.axile.master.service.transport.caches;

import java.io.IOException;
import java.util.List;
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

import com.nucleonforge.axile.common.api.caches.CachesFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstanceWithUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GetAllCachesEndpointProber}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class GetAllCachesEndpointProberTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private GetAllCachesEndpointProber getAllCachesEndpointProber;

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
          "cacheManagers" : [
            {
              "name": "anotherCacheManager",
              "caches": [
                {
                  "name": "countries",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "enabled": true
                }
              ]
            },
            {
            "name": "cacheManager",
              "caches": [
                {
                  "name": "cities",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "enabled": false
                },
                {
                  "name": "countries",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "enabled": true
                }
              ]
            }
          ]
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/axile-caches")) {
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
    void shouldReturnServiceCaches() {
        registry.register(createInstanceWithUrl(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // when.
        CachesFeed caches = getAllCachesEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // ServiceCaches -> CacheManagers
        List<CachesFeed.CacheManagers> cacheManagersList = caches.cacheManagers();
        assertThat(cacheManagersList).hasSize(2);

        CachesFeed.CacheManagers another = cacheManagersList.stream()
                .filter(cm -> "anotherCacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(another.caches()).hasSize(1);

        CachesFeed.CacheManagers main = cacheManagersList.stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(main.caches()).hasSize(2);

        // "anotherCacheManager" -> Caches -> "countries"
        CachesFeed.Caches anotherCountries = another.caches().stream()
                .filter(c -> "countries".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(anotherCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(anotherCountries.enabled()).isTrue();

        // "cacheManager" -> Caches -> "countries"
        CachesFeed.Caches mainCountries = main.caches().stream()
                .filter(c -> "countries".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCountries.enabled()).isTrue();

        // "cacheManager" -> Caches -> "cities"
        CachesFeed.Caches mainCities = main.caches().stream()
                .filter(c -> "cities".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCities.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCities.enabled()).isFalse();
    }
}
