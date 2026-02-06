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
package com.axelixlabs.axelix.master.service.transport;

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

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.ApplicationEntrypoint;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ManagedServiceMetadataEndpointProber}.
 *
 * @since 19.09.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = ApplicationEntrypoint.class)
class BasicDiscoveryMetadataEndpointProberTest {

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
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.0",
                "java" : "17.0.14u",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceUrl + "/actuator/axelix-metadata")) {
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
        BasicDiscoveryMetadata metadata =
                metadataEndpointProber.invoke(instanceUrl + "/actuator", NoHttpPayload.INSTANCE);

        assertThat(metadata).isNotNull();
        assertThat(metadata.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(metadata.getServiceVersion()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(metadata.getCommitShortSha()).isEqualTo("a8b0929");
        assertThat(metadata.getSoftwareVersions().getJava()).isEqualTo("17.0.14u");
        assertThat(metadata.getSoftwareVersions().getSpringBoot()).isEqualTo("3.5.0");
        assertThat(metadata.getHealthStatus()).isEqualTo(BasicDiscoveryMetadata.HealthStatus.UP);
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
