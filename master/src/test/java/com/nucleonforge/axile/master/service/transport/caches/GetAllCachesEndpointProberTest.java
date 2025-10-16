package com.nucleonforge.axile.master.service.transport.caches;

import java.io.IOException;
import java.util.Map;
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

import com.nucleonforge.axile.common.api.caches.ServiceCaches;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.instance.InstanceId;
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

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/caches")) {
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
        ServiceCaches caches =
                getAllCachesEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // ServiceCaches -> CacheManagers
        Map<String, ServiceCaches.CacheManagers> cacheManagers = caches.cacheManagers();
        assertThat(cacheManagers).hasSize(2).containsKeys("anotherCacheManager", "cacheManager");

        // CacheManagers -> "anotherCacheManager"
        ServiceCaches.CacheManagers another = cacheManagers.get("anotherCacheManager");
        assertThat(another.caches()).hasSize(1).containsKey("countries");

        // "anotherCacheManager" -> Caches -> "countries"
        assertThat(another.caches().get("countries").target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");

        // CacheManagers -> "cacheManager"
        ServiceCaches.CacheManagers main = cacheManagers.get("cacheManager");
        assertThat(main.caches()).hasSize(2).containsKeys("cities", "countries");

        // "cacheManager" -> Caches -> "countries"
        assertThat(main.caches().get("cities").target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        // "cacheManager" -> Caches -> "cities"
        assertThat(main.caches().get("countries").target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
    }
}
