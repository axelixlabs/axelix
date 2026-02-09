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
package com.axelixlabs.axelix.master.api;

import java.io.IOException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.gclog.GcLogEnableRequest;
import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.GcLogFileApi;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestObjectFactory;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link GcLogFileApi}.
 *
 * @since 11.01.2026
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GcLogFileApiTest {

    private static final String GC_LOG_STATUS_RESPONSE =
            // language=json
            """
        {
            "enabled": true,
            "level": "info",
            "availableLevels": [
                "trace",
                "debug",
                "info",
                "warning",
                "error"
            ]
        }
        """;

    private static final String GC_LOG_FILE_CONTENT =
            """
            [2026-01-11T23:20:50.868+0500][info][gc] GC(348) Concurrent Mark Cycle
            [2026-01-11T23:20:50.878+0500][info][gc] GC(350) Pause Young (Normal) (G1 Evacuation Pause) 32M->31M(42M) 0.532ms
            [2026-01-11T23:20:50.883+0500][info][gc] GC(348) Pause Remark 33M->33M(42M) 2.256ms
            [2026-01-11T23:20:50.884+0500][info][gc] GC(351) Pause Young (Normal) (G1 Evacuation Pause) 33M->31M(42M) 0.380ms
            [2026-01-11T23:20:50.888+0500][info][gc] GC(352) Pause Young (Normal) (G1 Evacuation Pause) 33M->31M(42M) 0.342ms
        """;

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

                if (path.equals("/" + activeInstanceId + "/actuator/axelix-gc/log/status")) {
                    return new MockResponse()
                            .setBody(GC_LOG_STATUS_RESPONSE)
                            .addHeader("Content-Type", "application/json");
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-gc/log/file")) {
                    return new MockResponse()
                            .setBody(GC_LOG_FILE_CONTENT)
                            .addHeader("Content-Type", "text/plain;charset=UTF-8");
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-gc/trigger")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-gc/log/enable")) {
                    return new MockResponse();
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-gc/log/disable")) {
                    return new MockResponse();
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        registry.register(
                TestObjectFactory.createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnGcLogFileAsPlainText() {
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/external/garbage-collector/logs/{instanceId}/file", String.class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getBody()).contains(GC_LOG_FILE_CONTENT);
    }

    @Test
    void shouldReturnStatusGcLogging() {
        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity(
                        "/api/external/garbage-collector/logs/{instanceId}/status", String.class, activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).isEqualTo(GC_LOG_STATUS_RESPONSE);
    }

    @Test
    void shouldTriggerGc() {
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/garbage-collector/{instanceId}/trigger",
                        HttpEntity.EMPTY,
                        Void.class,
                        activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldEnableGcLogging() {
        GcLogEnableRequest requestBody = new GcLogEnableRequest("info");
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/garbage-collector/logs/{instanceId}/enable",
                        requestBody,
                        Void.class,
                        activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDisableGcLogging() {
        ResponseEntity<Void> response = restTemplate
                .withoutAuthorities()
                .postForEntity(
                        "/api/external/garbage-collector/logs/{instanceId}/disable",
                        HttpEntity.EMPTY,
                        Void.class,
                        activeInstanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should return 500 on EndpointInvocationError")
    void shouldReturnInternalServerError() {
        String instanceId = UUID.randomUUID().toString();

        registry.register(createInstance(instanceId));

        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/external/garbage-collector/logs/{instanceId}/file", String.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldReturnBadRequestForUnregisteredInstance() {
        String instanceId = UUID.randomUUID().toString();

        ResponseEntity<String> response = restTemplate
                .withoutAuthorities()
                .getForEntity("/api/external/garbage-collector/logs/{instanceId}/file", String.class, instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
