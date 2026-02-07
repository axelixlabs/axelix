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
package com.axelixlabs.axelix.master.api.caches;

import java.io.IOException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.caches.CachesReadApi;
import com.axelixlabs.axelix.master.api.external.response.caches.CachesResponse;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests of {@link CachesReadApi}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CachesReadApiTest {
    private static final String activeInstanceId = UUID.randomUUID().toString();
    private static final String activeInstanceIdEmptyCaches = UUID.randomUUID().toString();

    // language=json
    String EXPECTED_ALL_CACHES_JSON =
            """
    {
      "cacheManagers": [
        {
          "name": "anotherCacheManager",
          "caches": [
            {
              "name": "countries",
              "target": "java.util.concurrent.ConcurrentHashMap",
              "hitsCount" : 15,
              "missesCount" : 2,
              "estimatedEntrySize": 10,
              "enabled" : true
            }
          ]
        },
        {
          "name": "cacheManager",
          "caches": [
            {
              "name": "cities",
              "target": "java.util.concurrent.ConcurrentHashMap",
              "hitsCount" : 25,
              "missesCount" : 5,
              "estimatedEntrySize": 6,
              "enabled" : false
            },
            {
              "name": "countries",
              "target": "java.util.concurrent.ConcurrentHashMap",
              "hitsCount" : 35,
              "missesCount" : 5,
              "estimatedEntrySize": 10,
              "enabled" : true
            }
          ]
        }
      ]
    }
    """;

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
        // language=json
        String jsonResponseAllCaches =
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

        // language=json
        String jsonResponseSingleCache =
                """
        {
          "target" : "java.util.concurrent.ConcurrentHashMap",
          "name" : "cities",
          "cacheManager" : "cacheManager"
        }
        """;

        // language=json
        String jsonResponseEmptyCaches = """
        {
          "cacheManagers" : []
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches")) {
                    return new MockResponse()
                            .setBody(jsonResponseAllCaches)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                if (path.equals("/" + activeInstanceId + "/actuator/axelix-caches/cacheManager/cities")) {
                    return new MockResponse()
                            .setBody(jsonResponseSingleCache)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceIdEmptyCaches + "/actuator/axelix-caches")) {
                    return new MockResponse()
                            .setBody(jsonResponseEmptyCaches)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
        registry.register(TestObjectFactory.createInstance(
                activeInstanceIdEmptyCaches, mockWebServer.url(activeInstanceIdEmptyCaches) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
        registry.deRegister(InstanceId.of(activeInstanceIdEmptyCaches));
    }

    @Test
    void shouldReturnJSONAllCachesResponse() {
        // when
        ResponseEntity<CachesResponse> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/caches/{instanceId}", CachesResponse.class, activeInstanceId);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_ALL_CACHES_JSON);
    }

    @Test
    void shouldReturnJSONCacheProfileResponse() {
        String requestedCacheName = "cities";
        String requestedCacheManagerName = "cacheManager";

        // language=json
        String expectedResponseFromMaster =
                """
        {
          "name": "%s",
          "target": "java.util.concurrent.ConcurrentHashMap",
          "cacheManager": "%s"
        }
        """
                        .formatted(requestedCacheName, requestedCacheManagerName);

        // when.
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/axelix/api/external/caches/{instanceId}/cache/{cacheName}?cacheManager=" + requestedCacheManagerName,
                        String.class,
                        activeInstanceId,
                        requestedCacheName);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedResponseFromMaster);
    }

    @Test
    void shouldHandleEmptyCacheManagersResponse() {
        // language=json
        String expectedEmptyCaches = """
            {
              "cacheManagers": []
            }
            """;

        // when
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/caches/{instanceId}", String.class, activeInstanceIdEmptyCaches);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedEmptyCaches);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorCachesResponse() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));

        // when.
        ResponseEntity<?> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/caches/{instanceId}", Void.class, instanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceCachesResponse() {
        String instanceId = "unregistered-caches-instance";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/axelix/api/external/caches/{instanceId}", EndpointInvocationException.class, instanceId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerErrorCacheProfileResponse() {
        String instanceId = UUID.randomUUID().toString();
        registry.register(createInstance(instanceId));
        String cacheName = "cities";

        // when.
        ResponseEntity<?> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/axelix/api/external/caches/{instanceId}/cache/{cacheName}?cacheManager=cacheManager",
                        Void.class,
                        instanceId,
                        cacheName);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstanceCacheProfileResponse() {
        String instanceId = "unregistered-single-caches-instance";
        String cacheName = "cities";

        // when.
        ResponseEntity<EndpointInvocationException> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/axelix/api/external/caches/{instanceId}/cache/{cacheName}?cacheManager=cacheManager",
                        EndpointInvocationException.class,
                        instanceId,
                        cacheName);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnCachesResponse(InvalidAuthScenario scenario) {

        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .getForEntity("/axelix/api/external/caches/{instanceId}", Void.class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized_OnCacheProfileResponse(InvalidAuthScenario scenario) {
        String cacheName = "cities";

        ResponseEntity<Void> response = scenario.getModifier()
                .apply(restTemplate)
                .getForEntity(
                        "/axelix/api/external/caches/{instanceId}/cache/{cacheName}?cacheManager=cacheManager",
                        Void.class,
                        activeInstanceId,
                        cacheName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
