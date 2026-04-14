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
package com.axelixlabs.axelix.common.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.JwtTokenGenerationException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link JwtEncoderService}, verifying correct JWT generation logic.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@SpringBootTest
@Import(DefaultJwtEncoderServiceTest.DefaultJwtEncoderServiceTestConfiguration.class)
class DefaultJwtEncoderServiceTest {
    private static final String USER_NAME = "testUser";
    private static final String PASSWORD = "testPassword";

    @Value("${axelix.master.auth.jwt.signing_key}")
    private String signingKey;

    @Value("${axelix.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @Test
    void shouldGenerateTokenWithRequiredClaims() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.VIEWER));

        // when.
        String token = jwtEncoderService.generateToken(user);

        // then.
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token);

        // header
        assertThat(claims.getHeader().getAlgorithm()).isEqualTo(JwtAlgorithm.HMAC512.getAlgorithmName());

        // payload
        assertThat(claims.getPayload().getSubject()).isEqualTo(USER_NAME);
        assertThat(claims.getPayload().getExpiration()).isNotNull();
        assertThat(claims.getPayload().getIssuedAt()).isNotNull();
        assertThat(claims.getPayload().getIssuedAt()).isNotNull();

        // signature
        assertThat(claims.getDigest()).isNotNull();
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithRoleAdmin() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN));

        // when.
        String token = jwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": [{"
                        + "    \"name\": \"ADMIN\","
                        + "    \"authorities\": ["
                        + "      \"SCHEDULED_TASKS_MODIFY\","
                        + "      \"CACHES_CLEAR\","
                        + "      \"GARBAGE_COLLECTOR\","
                        + "      \"ENV_VALUES_READ\","
                        + "      \"CONFIG_PROPS_VALUES_READ\","
                        + "      \"CACHES_TOGGLE\""
                        + "    ],"
                        + "    \"components\": []"
                        + "  }]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token))
                .whenIgnoringPaths("exp", "iat")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithRoleEditor() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.EDITOR));

        // when.
        String token = jwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": ["
                        + "    {"
                        + "      \"name\": \"EDITOR\","
                        + "      \"authorities\": ["
                        + "        \"SCHEDULED_TASKS_MODIFY\","
                        + "        \"CACHES_CLEAR\","
                        + "        \"GARBAGE_COLLECTOR\","
                        + "        \"CACHES_TOGGLE\""
                        + "      ],"
                        + "      \"components\": []"
                        + "    }"
                        + "  ]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token)).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithRoleViewer() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.VIEWER));

        // when.
        String token = jwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": ["
                        + "    {"
                        + "      \"name\": \"VIEWER\","
                        + "      \"authorities\": [],"
                        + "      \"components\": []"
                        + "    }"
                        + "  ]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token)).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_MultipleRoles() {
        User user =
                new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.ADMIN, DefaultRole.EDITOR, DefaultRole.VIEWER));

        // when.
        String token = jwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": ["
                        + "    {"
                        + "      \"name\": \"ADMIN\","
                        + "      \"authorities\": ["
                        + "        \"SCHEDULED_TASKS_MODIFY\","
                        + "        \"CACHES_CLEAR\","
                        + "        \"GARBAGE_COLLECTOR\","
                        + "        \"ENV_VALUES_READ\","
                        + "        \"CONFIG_PROPS_VALUES_READ\","
                        + "        \"CACHES_TOGGLE\""
                        + "      ],"
                        + "      \"components\": []"
                        + "    },"
                        + "    {"
                        + "      \"name\": \"EDITOR\","
                        + "      \"authorities\": ["
                        + "        \"SCHEDULED_TASKS_MODIFY\","
                        + "        \"CACHES_CLEAR\","
                        + "        \"GARBAGE_COLLECTOR\","
                        + "        \"CACHES_TOGGLE\""
                        + "      ],"
                        + "      \"components\": []"
                        + "    },"
                        + "    {"
                        + "      \"name\": \"VIEWER\","
                        + "      \"authorities\": [],"
                        + "      \"components\": []"
                        + "    }"
                        + "  ]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token))
                .whenIgnoringPaths("exp", "iat")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithoutRoles() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());

        // when.
        String token = jwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format("{" + "  \"sub\": \"%s\"," + "  \"roles\": []" + "}", USER_NAME);

        assertThatJson(getPayload(token)).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldContainCorrectExpirationTime() throws JsonProcessingException {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());

        // when.
        String token = jwtEncoderService.generateToken(user);

        // then.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(getPayload(token));

        long actualExpiration = node.get("exp").asLong() - node.get("iat").asLong();

        assertThat(actualExpiration).isEqualTo(lifespan.toSeconds());
    }

    @Test
    void shouldGenerateValidJwtToken_WithHs256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        JwtEncoderService encoder = new DefaultJwtEncoderService(JwtAlgorithm.HMAC256, key256, lifespan);

        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.EDITOR));

        // when.
        String token = encoder.generateToken(user);

        // header
        // language=json
        String expectedHeader = "{\"alg\": \"HS256\"}";

        assertThatJson(getHeader(token)).isEqualTo(expectedHeader);

        // payload
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": ["
                        + "    {"
                        + "      \"name\": \"EDITOR\","
                        + "      \"authorities\": ["
                        + "        \"SCHEDULED_TASKS_MODIFY\","
                        + "        \"CACHES_CLEAR\","
                        + "        \"GARBAGE_COLLECTOR\","
                        + "        \"CACHES_TOGGLE\""
                        + "      ],"
                        + "      \"components\": []"
                        + "    }"
                        + "  ]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token))
                .whenIgnoringPaths("exp", "iat")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_WithHs384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        JwtEncoderService encoder = new DefaultJwtEncoderService(JwtAlgorithm.HMAC384, key384, lifespan);

        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.VIEWER));

        // when.
        String token = encoder.generateToken(user);

        // header
        // language=json
        String expectedHeader = "{\"alg\": \"HS384\"}";

        assertThatJson(getHeader(token)).isEqualTo(expectedHeader);

        // payload
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": ["
                        + "    {"
                        + "      \"name\": \"VIEWER\","
                        + "      \"authorities\": [],"
                        + "      \"components\": []"
                        + "    }"
                        + "  ]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token)).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldFailWithInsufficientlyShortSecretKey() {
        String shortSecretKey = "shortKey";
        JwtAlgorithm jwtAlgorithm = JwtAlgorithm.HMAC256;
        DefaultJwtEncoderService invalidService = new DefaultJwtEncoderService(jwtAlgorithm, shortSecretKey, lifespan);

        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of(DefaultRole.EDITOR));

        assertThatThrownBy(() -> invalidService.generateToken(user)).isInstanceOf(JwtTokenGenerationException.class);
    }

    @Test
    void shouldHandleNullUser() {
        assertThatThrownBy(() -> jwtEncoderService.generateToken(null)).isInstanceOf(JwtTokenGenerationException.class);
    }

    @Test
    void shouldGenerateProperlyFormattedToken() {
        User user = new DefaultUser(USER_NAME, PASSWORD, Set.of());
        String token = jwtEncoderService.generateToken(user);

        assertThat(token.split("\\.")).hasSize(3);
    }

    private String getPayload(String token) {
        String[] parts = token.split("\\.");
        return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    }

    private String getHeader(String token) {
        String[] parts = token.split("\\.");
        return new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
    }

    @SpringBootConfiguration
    static class DefaultJwtEncoderServiceTestConfiguration {

        @Bean
        JwtEncoderService jwtEncoderService(
                final @Value("${axelix.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axelix.master.auth.jwt.lifespan}") Duration lifespan,
                final @Value("${axelix.master.auth.jwt.signing_key}") String signingKey) {
            return new DefaultJwtEncoderService(algorithm, signingKey, lifespan);
        }
    }
}
