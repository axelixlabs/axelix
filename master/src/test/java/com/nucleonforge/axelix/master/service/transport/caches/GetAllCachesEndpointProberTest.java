/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.master.service.transport.caches;

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

import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
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
                  "hitsCount" : 15,
                  "missesCount" : 2,
                  "estimatedEntrySize": 10,
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
                  "hitsCount" : 25,
                  "missesCount" : 5,
                  "estimatedEntrySize": 6,
                  "enabled": false
                },
                {
                  "name": "countries",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "hitsCount" : 35,
                  "missesCount" : 5,
                  "estimatedEntrySize": 10,
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

                if (path.equals("/" + activeInstanceId + "/axelix-caches")) {
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
        registry.register(createInstance(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));

        // when.
        CachesFeed caches = getAllCachesEndpointProber.invoke(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        // ServiceCaches -> CacheManagers
        List<CachesFeed.CacheManager> cacheManagerList = caches.cacheManagers();
        assertThat(cacheManagerList).hasSize(2);

        CachesFeed.CacheManager another = cacheManagerList.stream()
                .filter(cm -> "anotherCacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(another.caches()).hasSize(1);

        CachesFeed.CacheManager main = cacheManagerList.stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(main.caches()).hasSize(2);

        // "anotherCacheManager" -> Caches -> "countries"
        CachesFeed.Cache anotherCountries = another.caches().stream()
                .filter(c -> "countries".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(anotherCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(anotherCountries.hitsCount()).isEqualTo(15);
        assertThat(anotherCountries.missesCount()).isEqualTo(2);
        assertThat(anotherCountries.estimatedEntrySize()).isEqualTo(10);
        assertThat(anotherCountries.enabled()).isTrue();

        // "cacheManager" -> Caches -> "countries"
        CachesFeed.Cache mainCountries = main.caches().stream()
                .filter(c -> "countries".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCountries.hitsCount()).isEqualTo(35);
        assertThat(mainCountries.missesCount()).isEqualTo(5);
        assertThat(mainCountries.estimatedEntrySize()).isEqualTo(10);
        assertThat(mainCountries.enabled()).isTrue();

        // "cacheManager" -> Caches -> "cities"
        CachesFeed.Cache mainCities = main.caches().stream()
                .filter(c -> "cities".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCities.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCities.hitsCount()).isEqualTo(25);
        assertThat(mainCities.missesCount()).isEqualTo(5);
        assertThat(mainCities.estimatedEntrySize()).isEqualTo(6);
        assertThat(mainCities.enabled()).isFalse();
    }
}
