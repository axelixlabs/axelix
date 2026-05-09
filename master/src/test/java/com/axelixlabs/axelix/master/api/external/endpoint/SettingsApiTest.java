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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.io.IOException;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SettingsApi}.
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
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
    @AutoConfigureTestRestTemplate
    @TestPropertySource(
            properties = {
                "axelix.master.auth.options.static-admin.enabled=true",
                "axelix.master.auth.options.static-admin.credentials.username=admin",
                "axelix.master.auth.options.static-admin.credentials.password=password",
                "axelix.master.auth.options.oauth2.enabled=false"
            })
    @Nested
    class WhenStaticAdminEnabled {

        // The TestRestTemplateBuilder is intentionally not used here, since we do not require any auth to access
        // settings API.
        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void shouldReturnStaticAdminSettings() {

            ResponseEntity<String> response = restTemplate.getForEntity("/api/external/settings/auth", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // language=json
            String EXPECTED_JSON = """
                {
                  "authProviders": [
                    {
                      "type": "login-password"
                    }
                  ]
                }
                """;
            assertThatJson(response.getBody()).isEqualTo(EXPECTED_JSON);
        }
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureTestRestTemplate
    @TestPropertySource(
            properties = {
                "axelix.master.auth.options.oauth2.enabled=true",
                "axelix.master.auth.options.oauth2.issuer-uri=http://placeholder.will.be.overridden",
                "axelix.master.auth.options.oauth2.client-id=test-client",
                "axelix.master.auth.options.oauth2.client-secret=test-secret",
                "axelix.master.auth.options.oauth2.base-url=http://localhost:3000"
            })
    @Nested
    class WhenOAuth2Enabled {

        private static final String EXPECTED_JSON =
                // language=json
                """
            {
              "authProviders": [
                {
                  "clientId": "test-client",
                  "redirectUri": "http://localhost:3000/api/external/oauth2/callback",
                  "scope": "openid",
                  "authorizationEndpoint": "https://example.external.com/realms/axelix/openid-connect/auth",
                  "type": "oidc"
                }
              ]
            }
            """;

        // The TestRestTemplateBuilder is intentionally not used here, since we do not require any auth to access
        // settings API.
        @Autowired
        private TestRestTemplate restTemplate;

        @MockitoBean
        private OidcMetadataProvider oidcMetadataProvider;

        @BeforeEach
        void prepare() {
            Mockito.when(oidcMetadataProvider.getAuthorizationEndpoint())
                    .thenReturn("https://example.external.com/realms/axelix/openid-connect/auth");
        }

        @Test
        void shouldReturnOAuth2Settings() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/external/settings/auth", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThatJson(response.getBody()).isEqualTo(EXPECTED_JSON);
        }
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureTestRestTemplate
    @TestPropertySource(
            properties = {
                "axelix.master.auth.options.static-admin.enabled=true",
                "axelix.master.auth.options.static-admin.credentials.username=admin",
                "axelix.master.auth.options.static-admin.credentials.password=password",
                "axelix.master.auth.options.oauth2.enabled=true",
                "axelix.master.auth.options.oauth2.issuer-uri=http://placeholder.will.be.overridden",
                "axelix.master.auth.options.oauth2.client-id=test-client",
                "axelix.master.auth.options.oauth2.client-secret=test-secret",
                "axelix.master.auth.options.oauth2.base-url=http://localhost:3000"
            })
    @Nested
    class WhenStaticAdminAndOAuth2Enabled {

        private static final String EXPECTED_JSON =
                // language=json
                """
                {
                  "authProviders": [
                    {
                      "clientId": "test-client",
                      "redirectUri": "http://localhost:3000/api/external/oauth2/callback",
                      "scope": "openid",
                      "authorizationEndpoint": "https://example.external.com/realms/axelix/openid-connect/auth",
                      "type": "oidc"
                    },
                    {
                      "type": "login-password"
                    }
                  ]
                }
                """;

        @Autowired
        private TestRestTemplate restTemplate;

        @MockitoBean
        private OidcMetadataProvider oidcMetadataProvider;

        @BeforeEach
        void prepare() {
            Mockito.when(oidcMetadataProvider.getAuthorizationEndpoint())
                    .thenReturn("https://example.external.com/realms/axelix/openid-connect/auth");
        }

        @Test
        void shouldReturnBothProviders() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/external/settings/auth", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThatJson(response.getBody()).isEqualTo(EXPECTED_JSON);
        }
    }
}
