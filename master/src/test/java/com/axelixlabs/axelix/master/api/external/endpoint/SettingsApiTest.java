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

import net.javacrumbs.jsonunit.core.Option;
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

import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the OSS {@link SettingsApi} {@code /settings} endpoint. The licensing information is
 * contributed by the OSS {@code LicensingInfoResolver} (see {@code MasterTestConfiguration}).
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class SettingsApiTest {

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureTestRestTemplate
    @TestPropertySource(
            properties = {
                "axelix.master.auth.options.local.enabled=true",
                "axelix.master.auth.options.super-admin.credentials.username=admin",
                "axelix.master.auth.options.super-admin.credentials.password=admin",
                "axelix.master.auth.options.oauth2.enabled=false"
            })
    @Nested
    class WhenLocalEnabled {

        // The TestRestTemplateBuilder is intentionally not used here, since we do not require any auth to access
        // settings API.
        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void shouldReturnLocalLoginSettings() {

            ResponseEntity<String> response = restTemplate.getForEntity("/api/external/settings", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // language=json
            String EXPECTED_JSON = """
                {
                  "authenticationOptions": [
                    {
                      "type": "super-admin"
                    },
                    {
                      "type" : "local"
                    }
                  ],
                  "isMcpServerEnabled" : false,
                  "licensing" : {
                    "license" : "LGPL-3.0",
                    "issuedAt" : null,
                    "validUntil" : null,
                    "licenseId" : null,
                    "issuedTo" : null,
                    "functions" : [
                      { "name" : "Core monitoring", "enabled" : true },
                      { "name" : "Runtime debugging", "enabled" : true },
                      { "name" : "Custom RBAC", "enabled" : false },
                      { "name" : "Large-Scale Activity Monitoring", "enabled" : false },
                      { "name" : "Policy Enforcement", "enabled" : false }
                    ]
                  }
                }
                """;
            assertThatJson(response.getBody()).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_JSON);
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
              "authenticationOptions": [
                {
                  "type": "super-admin"
                },
                {
                  "clientId": "test-client",
                  "redirectUri": "http://localhost:3000/api/external/oauth2/callback",
                  "scope": "openid",
                  "authorizationEndpoint": "https://example.external.com/realms/axelix/openid-connect/auth",
                  "additionalParameters": {},
                  "type": "oidc"
                }
              ],
              "isMcpServerEnabled" : false,
              "licensing" : {
                "license" : "LGPL-3.0",
                "issuedAt" : null,
                "validUntil" : null,
                "licenseId" : null,
                "issuedTo" : null,
                "functions" : [
                  { "name" : "Core monitoring", "enabled" : true },
                  { "name" : "Runtime debugging", "enabled" : true },
                  { "name" : "Custom RBAC", "enabled" : false },
                  { "name" : "Large-Scale Activity Monitoring", "enabled" : false },
                  { "name" : "Policy Enforcement", "enabled" : false }
                ]
              }
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
            ResponseEntity<String> response = restTemplate.getForEntity("/api/external/settings", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThatJson(response.getBody()).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_JSON);
        }
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureTestRestTemplate
    @TestPropertySource(
            properties = {
                "axelix.master.auth.options.local.enabled=true",
                "axelix.master.auth.options.super-admin.credentials.password=admin",
                "axelix.master.auth.options.super-admin.credentials.username=admin",
                "axelix.master.auth.options.oauth2.enabled=true",
                "axelix.master.auth.options.oauth2.issuer-uri=http://placeholder.will.be.overridden",
                "axelix.master.auth.options.oauth2.client-id=test-client",
                "axelix.master.auth.options.oauth2.client-secret=test-secret",
                "axelix.master.auth.options.oauth2.base-url=http://localhost:3000"
            })
    @Nested
    class WhenLocalAndOAuth2Enabled {

        private static final String EXPECTED_JSON =
                // language=json
                """
                {
                  "authenticationOptions": [
                    {
                      "clientId": "test-client",
                      "redirectUri": "http://localhost:3000/api/external/oauth2/callback",
                      "scope": "openid",
                      "authorizationEndpoint": "https://example.external.com/realms/axelix/openid-connect/auth",
                      "additionalParameters": {},
                      "type": "oidc"
                    },
                    {
                      "type": "super-admin"
                    },
                    {
                      "type": "local"
                    }
                  ],
                  "isMcpServerEnabled" : false,
                  "licensing" : {
                    "license" : "LGPL-3.0",
                    "issuedAt" : null,
                    "validUntil" : null,
                    "licenseId" : null,
                    "issuedTo" : null,
                    "functions" : [
                      { "name" : "Core monitoring", "enabled" : true },
                      { "name" : "Runtime debugging", "enabled" : true },
                      { "name" : "Custom RBAC", "enabled" : false },
                      { "name" : "Large-Scale Activity Monitoring", "enabled" : false },
                      { "name" : "Policy Enforcement", "enabled" : false }
                    ]
                  }
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
            ResponseEntity<String> response = restTemplate.getForEntity("/api/external/settings", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThatJson(response.getBody()).isEqualTo(EXPECTED_JSON);
        }
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureTestRestTemplate
    @TestPropertySource(
            properties = {
                "axelix.master.auth.options.local.enabled=true",
                "axelix.master.auth.options.super-admin.credentials.password=admin",
                "axelix.master.auth.options.super-admin.credentials.username=admin",
                "axelix.master.auth.options.oauth2.enabled=true",
                "axelix.master.auth.options.oauth2.issuer-uri=http://unreachable.provider.test",
                "axelix.master.auth.options.oauth2.client-id=test-client",
                "axelix.master.auth.options.oauth2.client-secret=test-secret",
                "axelix.master.auth.options.oauth2.base-url=http://localhost:3000"
            })
    @Nested
    class WhenOAuth2ProviderUnreachable {

        // The OIDC option is dropped so that the UI still loads and local/super-admin login remain usable.
        private static final String EXPECTED_JSON =
                // language=json
                """
                {
                  "authenticationOptions": [
                    {
                      "type": "super-admin"
                    },
                    {
                      "type": "local"
                    }
                  ],
                  "isMcpServerEnabled" : false,
                  "licensing" : {
                    "license" : "LGPL-3.0",
                    "issuedAt" : null,
                    "validUntil" : null,
                    "licenseId" : null,
                    "issuedTo" : null,
                    "functions" : [
                      { "name" : "Core monitoring", "enabled" : true },
                      { "name" : "Runtime debugging", "enabled" : true },
                      { "name" : "Custom RBAC", "enabled" : false },
                      { "name" : "Large-Scale Activity Monitoring", "enabled" : false },
                      { "name" : "Policy Enforcement", "enabled" : false }
                    ]
                  }
                }
                """;

        @Autowired
        private TestRestTemplate restTemplate;

        @MockitoBean
        private OidcMetadataProvider oidcMetadataProvider;

        @BeforeEach
        void prepare() {
            Mockito.when(oidcMetadataProvider.getAuthorizationEndpoint())
                    .thenThrow(new OidcMetadataUnavailableException("http://unreachable.provider.test"));
        }

        @Test
        void shouldOmitOidcAndReturnOtherProviders() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/external/settings", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThatJson(response.getBody()).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(EXPECTED_JSON);
        }
    }
}
