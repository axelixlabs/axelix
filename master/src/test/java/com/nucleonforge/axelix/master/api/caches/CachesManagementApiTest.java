/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.nucleonforge.axelix.master.api.caches;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.TestRestTemplateBuilder;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CachesManagementApi}
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CachesManagementApiTest {

    private static final String activeInstanceId = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplateBuilder restTemplate;

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

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/vets/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/vets/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/enable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/disable")) {
                    return new MockResponse().setResponseCode(200);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/enable-all-cache")) {
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
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                        null,
                        Void.class,
                        Map.of(
                                "instanceId",
                                activeInstanceId,
                                "cacheManagerName",
                                "cacheManager",
                                "cacheName",
                                "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableSpecificCache() {
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/caches/{instanceId}/{cacheManagerName}/{cacheName}/disable",
                        null,
                        Void.class,
                        Map.of(
                                "instanceId",
                                activeInstanceId,
                                "cacheManagerName",
                                "cacheManager",
                                "cacheName",
                                "vets"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldEnableCacheManager() {
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/caches/{instanceId}/{cacheManagerName}/enable",
                        null,
                        Void.class,
                        Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableCacheManager() {
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/caches/{instanceId}/{cacheManagerName}/disable",
                        null,
                        Void.class,
                        Map.of("instanceId", activeInstanceId, "cacheManagerName", "cacheManager"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnInternalServerErrorWhenInstanceReturns404() {
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
                        null,
                        Void.class,
                        Map.of("instanceId", activeInstanceId, "cacheManagerName", "unknown", "cacheName", "unknown"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/axelix/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable",
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
