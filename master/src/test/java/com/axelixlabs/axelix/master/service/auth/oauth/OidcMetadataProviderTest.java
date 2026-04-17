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

import com.jayway.jsonpath.JsonPath;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.master.utils.TestResourceReader;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Integration tests for {@link OidcMetadataProvider}.
 *
 * @since 04.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
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

        Object jsonResponse = JsonPath.parse(TestResourceReader.readResource("other/google-oidc-configuration.json"))
                .set(JsonPath.compile("$.issuer"), mockWebServer.url("").toString())
                .jsonString();

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/.well-known/openid-configuration") && Objects.equals(request.getMethod(), "GET")) {
                    return new MockResponse()
                            .setBody(jsonResponse.toString())
                            .addHeader("Content-Type", APPLICATION_JSON_VALUE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        subject = new OidcMetadataProvider(
                RestClient.builder().build(), mockWebServer.url("").toString());
    }

    @Test
    void shouldSuccessfullyDecodeOidcConfiguration() {

        // when/then
        SoftAssertions.assertSoftly(it -> {
            it.assertThat(subject.getJwksUri()).isEqualTo("https://www.googleapis.com/oauth2/v3/certs");
            it.assertThat(subject.getTokenEndpoint()).isEqualTo("https://oauth2.googleapis.com/token");
            it.assertThat(subject.getAuthorizationEndpoint()).isEqualTo("https://accounts.google.com/o/oauth2/v2/auth");
        });
    }
}
