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
import java.util.Objects;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import com.axelixlabs.axelix.master.api.external.endpoint.SettingsApi;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * Integration tests for {@link SettingsApi}
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SettingsApiTest {

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void startMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownMockServer() throws IOException {
        mockWebServer.shutdown();
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(
            properties = {
                "axelix.master.auth.static-admin.enabled=true",
                "axelix.master.auth.static-admin.credentials.username=admin",
                "axelix.master.auth.static-admin.credentials.password=password"
            })
    @Nested
    class WhenStaticAdminEnabled {

        @LocalServerPort
        private int port;

        private TestRestTemplate restTemplate;

        @BeforeEach
        void prepare() {
            restTemplate = new TestRestTemplate().withRedirects(ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW);
        }

        @Test
        void shouldReturnStaticAdminSettings() {
            ResponseEntity<String> response =
                    restTemplate.getForEntity("http://localhost:" + port + "/api/external/settings/auth", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("{\"authProviders\":[{\"type\":\"static-admin\"}]}");
        }
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(
            properties = {
                "axelix.master.auth.oauth2.enabled=true",
                "axelix.master.auth.oauth2.issuer-uri=http://placeholder.will.be.overridden",
                "axelix.master.auth.oauth2.client-id=test-client",
                "axelix.master.auth.oauth2.client-secret=test-secret",
                "axelix.master.auth.oauth2.redirect-uri=http://localhost:3000/api/external/oauth2/callback"
            })
    @Nested
    class WhenOAuth2Enabled {

        private final String baseUrl = mockWebServer.url("").toString();

        private final String EXPECTED_JSON =
                // language=json
                """
            {
              "authProviders": [
                {
                  "clientId": "test-client",
                  "redirectUri": "http://localhost:3000/api/external/oauth2/callback",
                  "scope": "openid",
                  "authorizationEndpoint": "%srealms/axelix/protocol/openid-connect/auth",
                  "type": "oauth2"
                }
              ]
            }
            """
                        .formatted(baseUrl);

        @LocalServerPort
        private int port;

        private TestRestTemplate restTemplate;

        @Autowired
        private OidcMetadataProvider oidcMetadataProvider;

        @BeforeEach
        void prepare() {
            restTemplate = new TestRestTemplate().withRedirects(ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW);

            String baseUrl = mockWebServer.url("").toString();

            // override issuerUri after start mockWebServer
            ReflectionTestUtils.setField(oidcMetadataProvider, "issuerUri", baseUrl);
            ReflectionTestUtils.setField(oidcMetadataProvider, "authorizationEndpoint", null);

            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                    String path = request.getPath();
                    assert path != null;
                    if (path.equals("/.well-known/openid-configuration")
                            && Objects.equals(request.getMethod(), "GET")) {
                        return new MockResponse()
                                .setBody(
                                        // language=json
                                        """
                                        {
                                          "issuer": "%s",
                                          "jwks_uri": "%scerts",
                                          "token_endpoint": "%stoken",
                                          "authorization_endpoint": "%srealms/axelix/protocol/openid-connect/auth"
                                        }
                                        """
                                                .formatted(baseUrl, baseUrl, baseUrl, baseUrl))
                                .addHeader("Content-Type", APPLICATION_JSON_VALUE);
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });
        }

        @Test
        void shouldReturnOAuth2Settings() {
            ResponseEntity<String> response =
                    restTemplate.getForEntity("http://localhost:" + port + "/api/external/settings/auth", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThatJson(response.getBody()).isEqualTo(EXPECTED_JSON);
        }
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @TestPropertySource(
            properties = {
                "axelix.master.auth.static-admin.enabled=true",
                "axelix.master.auth.static-admin.credentials.username=admin",
                "axelix.master.auth.static-admin.credentials.password=password",
                "axelix.master.auth.oauth2.enabled=true",
                "axelix.master.auth.oauth2.issuer-uri=http://placeholder.will.be.overridden",
                "axelix.master.auth.oauth2.client-id=test-client",
                "axelix.master.auth.oauth2.client-secret=test-secret",
                "axelix.master.auth.oauth2.redirect-uri=http://localhost:3000/api/external/oauth2/callback"
            })
    @Nested
    class WhenStaticAdminAndOAuth2Enabled {

        private final String baseUrl = mockWebServer.url("").toString();

        private final String EXPECTED_JSON =
                // language=json
                """
                {
                  "authProviders": [
                    {
                      "clientId": "test-client",
                      "redirectUri": "http://localhost:3000/api/external/oauth2/callback",
                      "scope": "openid",
                      "authorizationEndpoint": "%srealms/axelix/protocol/openid-connect/auth",
                      "type": "oauth2"
                    },
                    {
                      "type": "static-admin"
                    }
                  ]
                }
                """
                        .formatted(baseUrl);

        @LocalServerPort
        private int port;

        private TestRestTemplate restTemplate;

        @Autowired
        private OidcMetadataProvider oidcMetadataProvider;

        @BeforeEach
        void prepare() {
            restTemplate = new TestRestTemplate().withRedirects(ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW);

            String baseUrl = mockWebServer.url("").toString();

            // override issuerUri after start mockWebServer
            ReflectionTestUtils.setField(oidcMetadataProvider, "issuerUri", baseUrl);
            ReflectionTestUtils.setField(oidcMetadataProvider, "authorizationEndpoint", null);

            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                    String path = request.getPath();
                    assert path != null;
                    if (path.equals("/.well-known/openid-configuration")
                            && Objects.equals(request.getMethod(), "GET")) {
                        return new MockResponse()
                                // language=json
                                .setBody(
                                        """
                                    {
                                      "issuer": "%s",
                                      "jwks_uri": "%scerts",
                                      "token_endpoint": "%stoken",
                                      "authorization_endpoint": "%srealms/axelix/protocol/openid-connect/auth"
                                    }
                                    """
                                                .formatted(baseUrl, baseUrl, baseUrl, baseUrl))
                                .addHeader("Content-Type", APPLICATION_JSON_VALUE);
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });
        }

        @Test
        void shouldReturnBothProviders() {
            ResponseEntity<String> response =
                    restTemplate.getForEntity("http://localhost:" + port + "/api/external/settings/auth", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThatJson(response.getBody()).isEqualTo(EXPECTED_JSON);
        }
    }
}
