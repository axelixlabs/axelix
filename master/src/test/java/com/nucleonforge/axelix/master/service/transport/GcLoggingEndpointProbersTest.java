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
package com.nucleonforge.axelix.master.service.transport;

import java.io.IOException;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import com.nucleonforge.axelix.common.api.gclog.GcLogEnableRequest;
import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.ApplicationEntrypoint;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;
import com.nucleonforge.axelix.master.service.transport.gclog.DisableGcLoggingEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.EnableGcLoggingEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.GcLogFileEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.GcLogStatusEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.GcTriggerEndpointProber;

import static com.nucleonforge.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static com.nucleonforge.axelix.master.utils.TestObjectFactory.createInstance;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GcLoggingEndpointProbers.
 *
 * @since 12.01.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
public class GcLoggingEndpointProbersTest {

    private static final String STATUS_RESPONSE =
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

    private static final String GC_LOG_CONTENT =
            """
            [2026-01-11T23:20:50.868+0500][info][gc] GC(348) Concurrent Mark Cycle
            [2026-01-11T23:20:50.878+0500][info][gc] GC(350) Pause Young (Normal) (G1 Evacuation Pause) 32M->31M(42M) 0.532ms
            [2026-01-11T23:20:50.883+0500][info][gc] GC(348) Pause Remark 33M->33M(42M) 2.256ms
            [2026-01-11T23:20:50.884+0500][info][gc] GC(351) Pause Young (Normal) (G1 Evacuation Pause) 33M->31M(42M) 0.380ms
            [2026-01-11T23:20:50.888+0500][info][gc] GC(352) Pause Young (Normal) (G1 Evacuation Pause) 33M->31M(42M) 0.342ms
        """;

    private final String activeInstanceId = UUID.randomUUID().toString();

    private MockWebServer mockWebServer;

    @Autowired
    private InstanceRegistry registry;

    @Autowired
    private GcLogFileEndpointProber gcLogFileEndpointProber;

    @Autowired
    private GcTriggerEndpointProber gcTriggerEndpointProber;

    @Autowired
    private GcLogStatusEndpointProber gcLogStatusEndpointProber;

    @Autowired
    private DisableGcLoggingEndpointProber disableGcLoggingEndpointProber;

    @Autowired
    private EnableGcLoggingEndpointProber enableGcLoggingEndpointProber;

    @Autowired
    private JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    @BeforeEach
    void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void shutdownServer() throws IOException {
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
                            .setBody(STATUS_RESPONSE)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + activeInstanceId + "/actuator/axelix-gc/log/file")) {
                    return new MockResponse()
                            .setBody(GC_LOG_CONTENT)
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

        registry.register(createInstance(activeInstanceId, mockWebServer.url(activeInstanceId) + "/actuator"));
    }

    @AfterEach
    void cleanup() {
        registry.deRegister(InstanceId.of(activeInstanceId));
    }

    @Test
    void shouldReturnGcLogFileAsPlainText() throws IOException {
        Resource response = gcLogFileEndpointProber.invoke(
                mockWebServer.url(activeInstanceId) + "/actuator", NoHttpPayload.INSTANCE);

        assertThat(response.getContentAsByteArray()).isEqualTo(GC_LOG_CONTENT.getBytes());
    }

    @Test
    void shouldReturnStatusGcLogging() {
        GcLogStatusResponse response = gcLogStatusEndpointProber.invoke(
                mockWebServer.url(activeInstanceId) + "/actuator", NoHttpPayload.INSTANCE);

        assertThat(response.level()).isEqualTo("info");
        assertThat(response.enabled()).isTrue();
        assertThat(response.availableLevels()).containsOnly("info", "debug", "warning", "error", "trace");
    }

    @Test
    void shouldTriggerGc() throws InterruptedException {
        gcTriggerEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldEnableGcLogging() throws InterruptedException {
        GcLogEnableRequest request = new GcLogEnableRequest("info");
        HttpPayload httpPayload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));

        enableGcLoggingEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), httpPayload);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThatJson(recordedRequest.getBody().readUtf8()).isEqualTo(request);
    }

    @Test
    void shouldDisableGcLogging() throws InterruptedException {
        disableGcLoggingEndpointProber.invokeNoValue(InstanceId.of(activeInstanceId), NoHttpPayload.INSTANCE);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
    }
}
