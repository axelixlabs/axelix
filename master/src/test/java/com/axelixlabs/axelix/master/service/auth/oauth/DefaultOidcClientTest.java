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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.utils.TestResourceReader;

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
 * @author Mikhail Polivakha
 */
class DefaultOidcClientTest {

    private static final String RSA_KEY_ID = "rsa-key-id";
    private static final String EC_KEY_ID = "ec-key-id";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String AUTH_CODE = "test-code";

    private static MockWebServer mockWebServer;
    private static RSAKey rsaKey;
    private static ECKey ecKey;

    private OidcClient oidcClient;

    @BeforeAll
    static void startServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        rsaKey = new RSAKeyGenerator(2048).keyID(RSA_KEY_ID).generate();
        ecKey = new ECKeyGenerator(Curve.P_256).keyID(EC_KEY_ID).generate();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
        String issuerUri = mockWebServer.url("").toString();

        OAuth2Properties oAuth2Properties =
                new OAuth2Properties(issuerUri, CLIENT_ID, CLIENT_SECRET, issuerUri, null, null, null);

        OidcMetadataProvider oidcMetadataProvider = mock(OidcMetadataProvider.class);
        when(oidcMetadataProvider.getTokenEndpoint()).thenReturn(issuerUri + "/token");
        when(oidcMetadataProvider.getJwksUri()).thenReturn(issuerUri + "/certs");
        when(oidcMetadataProvider.getUserInfoEndpoint()).thenReturn(issuerUri + "/userinfo");

        oidcClient = new DefaultOidcClient(
                RestClient.builder().build(), oAuth2Properties, oidcMetadataProvider, new ObjectMapper());
    }

    @Test
    void shouldExchangeCodeForIdToken() throws Exception {
        // given.
        String jsonResponse = TestResourceReader.readResource("other/id-token-response.json");

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                if ("/token".equals(request.getPath())
                        && "POST".equals(request.getMethod())
                        && MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(
                                request.getHeader(HttpHeaders.CONTENT_TYPE))) {
                    return new MockResponse().setBody(jsonResponse).addHeader("Content-Type", APPLICATION_JSON_VALUE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        // when.
        Tokens data = oidcClient.exchangeCodeForTokens(AUTH_CODE);

        // then.
        String idToken =
                new ObjectMapper().readTree(jsonResponse).get("id_token").asString();
        assertThat(data.idToken()).isEqualTo(idToken);

        // and then.
        String accessToken =
                new ObjectMapper().readTree(jsonResponse).get("access_token").asString();
        assertThat(data.accessToken()).isEqualTo(accessToken);

        // and then.
        RecordedRequest tokenRequest = mockWebServer.takeRequest();
        Map<String, String> formParams = parseFormBody(tokenRequest.getBody().readUtf8());
        String expectedRedirectUri = mockWebServer.url("") + "/api/external/oauth2/callback";

        assertThat(formParams)
                .containsEntry("grant_type", "authorization_code")
                .containsEntry("client_id", CLIENT_ID)
                .containsEntry("client_secret", CLIENT_SECRET)
                .containsEntry("redirect_uri", expectedRedirectUri)
                .containsEntry("code", AUTH_CODE);
    }

    @Test
    void shouldThrowWhenIdTokenMissingInResponse() {
        // given.
        // language=json
        String jsonResponse = """
            {
              "access_token": "some-token"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                if ("/token".equals(request.getPath()) && "POST".equals(request.getMethod())) {
                    return new MockResponse().setBody(jsonResponse).addHeader("Content-Type", APPLICATION_JSON_VALUE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        // when/then
        assertThatThrownBy(() -> oidcClient.exchangeCodeForTokens(AUTH_CODE))
                .isInstanceOf(OidcTokenExchangeException.class);
    }

    @Nested
    class IdTokenValidation {

        @Test
        void shouldExtractUsernameFromTokenSignedWithRsaKey() throws Exception {
            setupJwksDispatcher();
            String token = buildIdToken(RSA_KEY_ID, rsaKey.toPrivateKey(), "preferred-user", null);

            String result = oidcClient.validateIdTokenAndExtractUsername(token);

            assertThat(result).isEqualTo("preferred-user");
        }

        @Test
        void shouldExtractUsernameFromTokenSignedWithEcKey() throws Exception {
            setupJwksDispatcher();
            String token = buildIdToken(EC_KEY_ID, ecKey.toPrivateKey(), "ec-user", null);

            String result = oidcClient.validateIdTokenAndExtractUsername(token);

            assertThat(result).isEqualTo("ec-user");
        }

        @Test
        void shouldFallbackToSubjectWhenNoPreferredUsername() throws Exception {
            setupJwksDispatcher();
            String subject = UUID.randomUUID().toString();
            String token = buildIdToken(RSA_KEY_ID, rsaKey.toPrivateKey(), null, subject);

            String result = oidcClient.validateIdTokenAndExtractUsername(token);

            assertThat(result).isEqualTo(subject);
        }

        @Test
        void shouldThrowWhenTokenExpired() throws Exception {
            setupJwksDispatcher();
            String token = buildExpiredToken();

            assertThatThrownBy(() -> oidcClient.validateIdTokenAndExtractUsername(token))
                    .isInstanceOf(ExpiredJwtTokenException.class);
        }

        @Test
        void shouldThrowWhenSignatureValidationFails() throws Exception {
            // given.
            setupJwksDispatcher();
            RSAKey differentRsaKey = new RSAKeyGenerator(2048).keyID(RSA_KEY_ID).generate();
            String token = buildIdToken(RSA_KEY_ID, differentRsaKey.toPrivateKey(), "user", null);

            // when/then.
            assertThatThrownBy(() -> oidcClient.validateIdTokenAndExtractUsername(token))
                    .isInstanceOf(InvalidJwtTokenException.class);
        }

        @Test
        void shouldThrowWhenKeyNotFoundInJwks() throws Exception {
            // given.
            setupJwksDispatcher();
            String token = buildIdToken("unknown-key-id", rsaKey.toPrivateKey(), "user", null);

            // when/then.
            assertThatThrownBy(() -> oidcClient.validateIdTokenAndExtractUsername(token))
                    .isInstanceOf(JwtParsingException.class);
        }

        private void setupJwksDispatcher() {
            String jwksJson = new ObjectMapper().writeValueAsString(new JWKSet(List.of(rsaKey, ecKey)).toJSONObject());

            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                    if ("/certs".equals(request.getPath()) && "GET".equals(request.getMethod())) {
                        return new MockResponse().setBody(jwksJson).addHeader("Content-Type", APPLICATION_JSON_VALUE);
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });
        }

        private String buildIdToken(
                String keyId, PrivateKey privateKey, @Nullable String preferredUsername, @Nullable String subject) {
            String baseUrl = mockWebServer.url("").toString();
            Instant now = Instant.now();

            JwtBuilder builder = Jwts.builder()
                    .header()
                    .keyId(keyId)
                    .and()
                    .subject(subject != null ? subject : UUID.randomUUID().toString())
                    .issuer(baseUrl)
                    .audience()
                    .add(CLIENT_ID)
                    .and()
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(Duration.ofHours(1))))
                    .signWith(privateKey);

            if (preferredUsername != null) {
                builder.claim("preferred_username", preferredUsername);
            }

            return builder.compact();
        }

        private String buildExpiredToken() throws JOSEException {
            String baseUrl = mockWebServer.url("").toString();
            Instant past = Instant.now().minus(Duration.ofHours(2));

            return Jwts.builder()
                    .header()
                    .keyId(RSA_KEY_ID)
                    .and()
                    .subject("user")
                    .issuer(baseUrl)
                    .audience()
                    .add(CLIENT_ID)
                    .and()
                    .issuedAt(Date.from(past))
                    .expiration(Date.from(past.plus(Duration.ofMinutes(1))))
                    .signWith(rsaKey.toPrivateKey())
                    .compact();
        }
    }

    @Nested
    class AccessTokenValidation {

        @BeforeEach
        void drainStaleRecordedRequests() throws InterruptedException {
            while (mockWebServer.takeRequest(1, TimeUnit.MILLISECONDS) != null) {
                // Clear recorded requests.
                // Other test do not always consume MockWebServer's recorded requests.
            }
        }

        @Test
        void shouldCompleteWhenUserInfoReturnsOk() throws Exception {
            String accessToken = "test-access-token";

            String jsonResponse = TestResourceReader.readResource("other/user-info-response.json");

            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                    if ("/userinfo".equals(request.getPath()) && "GET".equals(request.getMethod())) {
                        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                        if (("Bearer " + accessToken).equals(auth)) {
                            return new MockResponse()
                                    .setBody(jsonResponse)
                                    .addHeader("Content-Type", APPLICATION_JSON_VALUE)
                                    .setResponseCode(200);
                        }
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });

            String userInfoJson = oidcClient.validateAccessTokenAndExtractUserInfo(accessToken);

            assertThat(userInfoJson).isEqualTo(jsonResponse);
            RecordedRequest userInfoRequest = mockWebServer.takeRequest();
            assertThat(userInfoRequest.getPath()).isEqualTo("/userinfo");
            assertThat(userInfoRequest.getMethod()).isEqualTo("GET");
            assertThat(userInfoRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + accessToken);
        }

        @Test
        void shouldThrowTokenExchangeExceptionWhenUserInfoReturnsUnauthorized() {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                    if ("/userinfo".equals(request.getPath()) && "GET".equals(request.getMethod())) {
                        return new MockResponse().setResponseCode(401);
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });

            assertThatThrownBy(() -> oidcClient.validateAccessTokenAndExtractUserInfo("invalid-token"))
                    .isInstanceOf(OidcTokenExchangeException.class);
        }

        @Test
        void shouldThrowOidcMetadataUnavailableExceptionWhenUserInfoReturnsNotFound() {

            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                    return new MockResponse().setResponseCode(404);
                }
            });

            assertThatThrownBy(() -> oidcClient.validateAccessTokenAndExtractUserInfo("invalid-token"))
                    .isInstanceOf(OidcMetadataUnavailableException.class);
        }
    }

    private static Map<String, String> parseFormBody(String body) {
        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            params.put(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return params;
    }
}
