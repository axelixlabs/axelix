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
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
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

import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.master.autoconfiguration.auth.OAuth2Properties;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * Integration tests for {@link DefaultOidcClient}.
 *
 * @since 05.03.2026
 * @author Nikita Kirillov
 */
class DefaultOidcClientTest {

    private static final String KEY_ID = "test-key-id";
    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String AUTH_CODE = "test-code";

    private static MockWebServer mockWebServer;

    private OidcClient oidcClient;

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

        OAuth2Properties oAuth2Properties =
                new OAuth2Properties(baseUrl, CLIENT_ID, CLIENT_SECRET, baseUrl + "/oauth2/callback", null, null);

        OidcMetadataProvider oidcMetadataProvider = mock(OidcMetadataProvider.class);
        when(oidcMetadataProvider.getTokenEndpoint()).thenReturn(baseUrl + "/token");
        when(oidcMetadataProvider.getJwksUri()).thenReturn(baseUrl + "/certs");

        oidcClient = new DefaultOidcClient(RestClient.builder().build(), oAuth2Properties, oidcMetadataProvider);
    }

    @Test
    void shouldExchangeCodeForIdToken() {
        String idToken = "eyJhbGciOiJSUzI1NiJ9.test.token";

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/token") && Objects.equals(request.getMethod(), "POST")) {
                    return new MockResponse()
                            .setBody("{\"id_token\": \"%s\"}\n".formatted(idToken))
                            .addHeader("Content-Type", APPLICATION_JSON_VALUE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        String result = oidcClient.exchangeCodeForIdToken(AUTH_CODE);

        assertThat(result).isEqualTo(idToken);
    }

    @Test
    void shouldThrowWhenIdTokenMissingInResponse() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                return new MockResponse()
                        .setBody("{\"access_token\": \"some-token\"}")
                        .addHeader("Content-Type", APPLICATION_JSON_VALUE);
            }
        });

        assertThatThrownBy(() -> oidcClient.exchangeCodeForIdToken(AUTH_CODE))
                .isInstanceOf(OidcTokenExchangeException.class);
    }

    @Test
    void shouldFetchRsaPublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        RSAPublicKey rsaPublicKey = (RSAPublicKey) generator.generateKeyPair().getPublic();
        String modulus = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rsaPublicKey.getModulus().toByteArray());

        String exponent = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

        String jwksJson =
                // language=json
                """
                {
                  "keys": [
                    {
                      "kid": "%s",
                      "kty": "RSA",
                      "n": "%s",
                      "e": "%s"
                    }
                  ]
                }
                """
                        .formatted(KEY_ID, modulus, exponent);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/certs") && Objects.equals(request.getMethod(), "GET")) {
                    return new MockResponse().setBody(jwksJson).addHeader("Content-Type", APPLICATION_JSON_VALUE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        PublicKey result = oidcClient.fetchPublicKey(KEY_ID);

        assertThat(result).isNotNull();
        assertThat(result.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void shouldFetchEcPublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));

        ECPublicKey ecPublicKey = (ECPublicKey) generator.generateKeyPair().getPublic();
        String xCoord = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(ecPublicKey.getW().getAffineX().toByteArray());
        String yCoord = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(ecPublicKey.getW().getAffineY().toByteArray());

        String jwksJson =
                // language=json
                """
                {
                  "keys": [
                    {
                      "kid": "%s",
                      "kty": "EC",
                      "crv": "P-256",
                      "x": "%s",
                      "y": "%s"
                    }
                  ]
                }
                """
                        .formatted(KEY_ID, xCoord, yCoord);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/certs") && Objects.equals(request.getMethod(), "GET")) {
                    return new MockResponse().setBody(jwksJson).addHeader("Content-Type", APPLICATION_JSON_VALUE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        PublicKey result = oidcClient.fetchPublicKey(KEY_ID);

        assertThat(result).isNotNull();
        assertThat(result.getAlgorithm()).isEqualTo("EC");
    }

    @Test
    void shouldThrowWhenKeyNotFound() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                return new MockResponse().setBody("{\"keys\": []}").addHeader("Content-Type", APPLICATION_JSON_VALUE);
            }
        });

        assertThatThrownBy(() -> oidcClient.fetchPublicKey(KEY_ID)).isInstanceOf(JwtParsingException.class);
    }
}
