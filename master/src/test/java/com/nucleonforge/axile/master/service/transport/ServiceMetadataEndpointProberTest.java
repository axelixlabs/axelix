/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.service.transport;

import java.io.IOException;
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

import com.nucleonforge.axile.common.api.registration.ServiceMetadata;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.ApplicationEntrypoint;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ManagedServiceMetadataEndpointProber}.
 *
 * @since 19.09.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class ServiceMetadataEndpointProberTest {

    private static final String activeInstanceUrl = UUID.randomUUID().toString();

    private static MockWebServer mockWebServer;

    @Autowired
    private ManagedServiceMetadataEndpointProber metadataEndpointProber;

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
    void setUp() {
        // language=json
        String jsonResponse =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "javaVersion" : "17.0.14u",
              "springBootVersion" : "3.5.0",
              "healthStatus" : "UP"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceUrl + "/actuator/axile-metadata")) {
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
    void shouldReturnMetadata() throws EndpointInvocationException {
        String instanceUrl = mockWebServer.url(activeInstanceUrl).toString();
        ServiceMetadata metadata = metadataEndpointProber.invoke(instanceUrl + "/actuator", NoHttpPayload.INSTANCE);

        assertThat(metadata).isNotNull();
        assertThat(metadata.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(metadata.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(metadata.commitShortSha()).isEqualTo("a8b0929");
        assertThat(metadata.javaVersion()).isEqualTo("17.0.14u");
        assertThat(metadata.springBootVersion()).isEqualTo("3.5.0");
        assertThat(metadata.healthStatus()).isEqualTo(ServiceMetadata.HealthStatus.UP);
    }

    @Test
    void shouldThrowExceptionWhenStatusIsNot2xx() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                return new MockResponse().setResponseCode(500);
            }
        });

        String instanceUrl = mockWebServer.url(activeInstanceUrl).toString();
        assertThatThrownBy(() -> metadataEndpointProber.invoke(instanceUrl, NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }

    @Test
    void shouldThrowExceptionWhenInstanceUrlIsInvalid() {
        String invalidUrl = "http://localhost:0/non-existent";

        assertThatThrownBy(() -> metadataEndpointProber.invoke(invalidUrl, NoHttpPayload.INSTANCE))
                .isInstanceOf(EndpointInvocationException.class);
    }
}
