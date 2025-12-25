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
package com.nucleonforge.axile.master.api.caches;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.master.ApplicationEntrypoint;
import com.nucleonforge.axile.master.api.UserApi;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.auth.CookieService;
import com.nucleonforge.axile.master.service.auth.UserLoginService;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesManagementApi}
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "axile.master.auth.type=none")
class CachesManagementApiTest {

    @MockBean
    private UserApi userApi;

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private CookieService cookieService;

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
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axile-caches/cacheManager/vets/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axile-caches/cacheManager/vets/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axile-caches/cacheManager/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axile-caches/cacheManager/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axile-caches/enable-all-cache")) {
                    return new MockResponse().setResponseCode(200);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldEnableSpecificCache() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager", "cacheName", "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableSpecificCache() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/disable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager", "cacheName", "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldEnableCacheManager() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/enable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableCacheManager() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/disable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnInternalServerErrorWhenInstanceReturns404() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                null,
                Void.class,
                Map.of("instanceId", activeInstanceId, "cacheManagerName", "unknown", "cacheName", "unknown"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/axile/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                null,
                Void.class,
                Map.of(
                        "instanceId",
                        UUID.randomUUID().toString(),
                        "cacheManagerName",
                        "cacheManager",
                        "cacheName",
                        "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
