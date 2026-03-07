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
package com.axelixlabs.axelix.master.service.auth.oauth;

import java.io.IOException;
import java.util.Objects;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Integration tests for {@link OidcMetadataProvider}.
 *
 * @since 04.03.2026
 * @author Nikita Kirillov
 */
class OidcMetadataProviderTest {

    private static MockWebServer mockWebServer;

    private OidcMetadataProvider subject;

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
        String baseUrl = mockWebServer.url("").toString();

        // language=json
        String jsonResponse =
                """
            {
              "issuer": "%s",
              "jwks_uri": "%srealms/axelix/protocol/openid-connect/certs",
              "token_endpoint": "%srealms/axelix/protocol/openid-connect/token",
              "authorization_endpoint": "%srealms/axelix/protocol/openid-connect/auth"
            }
            """
                        .formatted(baseUrl, baseUrl, baseUrl, baseUrl);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/.well-known/openid-configuration") && Objects.equals(request.getMethod(), "GET")) {
                    return new MockResponse().setBody(jsonResponse).addHeader("Content-Type", APPLICATION_JSON_VALUE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        subject = new OidcMetadataProvider(RestClient.builder().build(), baseUrl);
    }

    @Test
    void shouldFetchOidcMetadata() {
        String baseUrl = mockWebServer.url("").toString();

        assertThat(subject.getJwksUri()).isEqualTo(baseUrl + "realms/axelix/protocol/openid-connect/certs");
        assertThat(subject.getTokenEndpoint()).isEqualTo(baseUrl + "realms/axelix/protocol/openid-connect/token");
        assertThat(subject.getAuthorizationEndpoint())
                .isEqualTo(baseUrl + "realms/axelix/protocol/openid-connect/auth");
    }
}
