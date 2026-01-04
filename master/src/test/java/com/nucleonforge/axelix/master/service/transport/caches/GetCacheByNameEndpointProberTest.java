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

import com.nucleonforge.axelix.common.api.caches.SingleCache;
import com.nucleonforge.axelix.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
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

                if (path.equals("/" + activeInstanceId + "/axelix-caches/cacheManager/countries")) {
                    return new MockResponse()
                            .setBody(jsonResponseCountries)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(createInstance(
                activeInstanceId, mockWebServer.url(activeInstanceId).toString()));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnSingleCache() {
        String cacheName = "countries";
        HttpPayload payload =
                new DefaultHttpPayload(Map.of("cacheName", cacheName, "cacheManagerName", "cacheManager"));

        // when.
        SingleCache cache = getCacheByNameEndpointProber.invoke(InstanceId.of(activeInstanceId), payload);

        // then
        assertThat(cache.name()).isEqualTo("countries");
        assertThat(cache.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(cache.cacheManager()).isEqualTo("cacheManager");
    }
}
