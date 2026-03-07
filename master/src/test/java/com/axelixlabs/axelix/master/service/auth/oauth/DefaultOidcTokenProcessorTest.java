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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
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

import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.master.autoconfiguration.auth.OAuth2Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * Integration tests for {@link DefaultOidcTokenProcessor}
 *
 * @since 05.03.2026
 * @author Nikita Kirillov
 */
class DefaultOidcTokenProcessorTest {

    private static final String RSA_KEY_ID = "test-rsa-key-id";
    private static final String EC_KEY_ID = "test-ec-key-id";
    private static final String CLIENT_ID = "test-client";
    private static final String USERNAME = "test-user";

    private static MockWebServer mockWebServer;
    private static KeyPair rsaKeyPair;
    private static KeyPair ecKeyPair;

    private OidcTokenProcessor oidcTokenProcessor;

    @BeforeAll
    static void startServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        KeyPairGenerator rsaGenerator = KeyPairGenerator.getInstance("RSA");
        rsaGenerator.initialize(2048);
        rsaKeyPair = rsaGenerator.generateKeyPair();

        KeyPairGenerator ecGenerator = KeyPairGenerator.getInstance("EC");
        ecGenerator.initialize(new ECGenParameterSpec("secp256r1"));
        ecKeyPair = ecGenerator.generateKeyPair();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void prepare() {
        String baseUrl = mockWebServer.url("").toString();

        RSAPublicKey rsaPublicKey = (RSAPublicKey) rsaKeyPair.getPublic();
        ECPublicKey ecPublicKey = (ECPublicKey) ecKeyPair.getPublic();

        String modulus = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rsaPublicKey.getModulus().toByteArray());

        String exponent = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

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
              "kty": "RSA",
              "n": "%s",
              "e": "%s"
            },
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
                        .formatted(RSA_KEY_ID, modulus, exponent, EC_KEY_ID, xCoord, yCoord);

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/certs") && Objects.equals(request.getMethod(), "GET")) {
                    return new MockResponse().setBody(jwksJson).addHeader("Content-Type", APPLICATION_JSON_VALUE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        OAuth2Properties properties =
                new OAuth2Properties(baseUrl, CLIENT_ID, "secret", baseUrl + "/oauth2/callback", null, null);

        OidcMetadataProvider metadataProvider = mock(OidcMetadataProvider.class);
        when(metadataProvider.getJwksUri()).thenReturn(baseUrl + "/certs");

        OidcClient oidcClient = new DefaultOidcClient(RestClient.builder().build(), properties, metadataProvider);

        oidcTokenProcessor = new DefaultOidcTokenProcessor(oidcClient, properties);
    }

    @Test
    void shouldExtractUsernameFromValidTokenSignedWithRsaKey() {
        String subject = String.valueOf(UUID.randomUUID());
        String token = buildRsaToken(subject, Map.of("preferred_username", "preferred-" + USERNAME));

        String result = oidcTokenProcessor.validateOAuth2JwtTokenAndExtractUsername(token);

        assertThat(result).isEqualTo("preferred-" + USERNAME);
    }

    @Test
    void shouldExtractUsernameFromTokenSignedWithEcKey() {
        String subject = String.valueOf(UUID.randomUUID());
        String token = buildEcToken(subject, Map.of("preferred_username", "preferred-" + USERNAME));

        String result = oidcTokenProcessor.validateOAuth2JwtTokenAndExtractUsername(token);

        assertThat(result).isEqualTo("preferred-" + USERNAME);
    }

    @Test
    void shouldFallbackToSubjectWhenNoPreferredUsername() {
        String subject = String.valueOf(UUID.randomUUID());
        String token = buildRsaToken(subject, Map.of());

        String result = oidcTokenProcessor.validateOAuth2JwtTokenAndExtractUsername(token);

        assertThat(result).isEqualTo(subject);
    }

    @Test
    void shouldThrowWhenTokenExpired() {
        String token = buildExpiredToken();

        assertThatThrownBy(() -> oidcTokenProcessor.validateOAuth2JwtTokenAndExtractUsername(token))
                .isInstanceOf(ExpiredJwtTokenException.class);
    }

    private String buildRsaToken(String subject, Map<String, Object> extraClaims) {
        String baseUrl = mockWebServer.url("").toString();
        Instant now = Instant.now();

        JwtBuilder builder = Jwts.builder()
                .header()
                .keyId(RSA_KEY_ID)
                .and()
                .subject(subject)
                .issuer(baseUrl)
                .audience()
                .add(CLIENT_ID)
                .and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(1))))
                .signWith(rsaKeyPair.getPrivate());

        extraClaims.forEach(builder::claim);

        return builder.compact();
    }

    private String buildEcToken(String subject, Map<String, Object> extraClaims) {
        String baseUrl = mockWebServer.url("").toString();
        Instant now = Instant.now();

        JwtBuilder builder = Jwts.builder()
                .header()
                .keyId(EC_KEY_ID)
                .and()
                .subject(subject)
                .issuer(baseUrl)
                .audience()
                .add(CLIENT_ID)
                .and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(1))))
                .signWith(ecKeyPair.getPrivate());

        extraClaims.forEach(builder::claim);

        return builder.compact();
    }

    private String buildExpiredToken() {
        String baseUrl = mockWebServer.url("").toString();
        Instant past = Instant.now().minus(Duration.ofHours(1));

        return Jwts.builder()
                .header()
                .keyId(RSA_KEY_ID)
                .and()
                .subject(USERNAME)
                .issuer(baseUrl)
                .audience()
                .add(CLIENT_ID)
                .and()
                .issuedAt(Date.from(past))
                .expiration(Date.from(past.plus(Duration.ofHours(1))))
                .signWith(rsaKeyPair.getPrivate())
                .compact();
    }
}
