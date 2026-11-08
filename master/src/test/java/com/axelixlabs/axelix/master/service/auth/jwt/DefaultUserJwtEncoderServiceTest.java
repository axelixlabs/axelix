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
package com.axelixlabs.axelix.master.service.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Set;

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
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.exception.JwtTokenGenerationException;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.auth.TokenClaim;
import com.axelixlabs.axelix.master.domain.auth.User;
import com.axelixlabs.axelix.master.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.testfixtures.UserUtils;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Integration tests for {@link UserJwtEncoderService}, verifying correct JWT generation logic.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = DefaultUserJwtEncoderServiceTest.DefaultJwtEncoderServiceTestConfiguration.class)
@TestPropertySource("classpath:jwt-services-test.properties")
class DefaultUserJwtEncoderServiceTest {

    private static final String USER_NAME = "testUser";
    private static final String PASSWORD = "testPassword";

    @Value("${axelix.master.auth.jwt.signing_key}")
    private String signingKey;

    @Value("${axelix.master.auth.jwt.algorithm}")
    private String algorithm;

    @Value("${axelix.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Autowired
    private UserJwtEncoderService userJwtEncoderService;

    @Test
    void shouldGenerateTokenWithRequiredClaims() {
        // given.
        Role viewer = TestRoles.VIEWER;
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(viewer));

        // when.
        String token = userJwtEncoderService.generateToken(user);

        // then.
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token);

        // header
        assertThat(claims.getHeader().getAlgorithm())
                .isEqualTo(JwtAlgorithm.parse(algorithm).getAlgorithmName());

        // payload
        assertThat(claims.getPayload().getSubject()).isEqualTo(USER_NAME);
        assertThat(claims.getPayload().get(TokenClaim.USER_ID.getEncoding(), String.class))
                .isEqualTo(user.getId());
        // iat is a JWT NumericDate (whole seconds), so the parsed value can trail Instant.now() by almost 2s.
        assertThat(claims.getPayload().getIssuedAt().toInstant())
                .isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
        assertThat(claims.getPayload().getExpiration())
                .isEqualTo(claims.getPayload().getIssuedAt().toInstant().plus(lifespan));

        // signature
        assertThat(claims.getDigest()).isNotNull();
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithRoleAdmin() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.ADMIN));

        // when.
        String token = userJwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": [{"
                        + "    \"name\": \"ADMIN\","
                        + "    \"authorities\": ["
                        + "      \"scheduled-tasks:write\","
                        + "      \"caches:clear\","
                        + "      \"gc:manage\","
                        + "      \"environment.values:read\","
                        + "      \"config-properties.values:read\","
                        + "      \"caches:toggle\""
                        + "    ],"
                        + "    \"components\": []"
                        + "  }]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token))
                .whenIgnoringPaths("exp", "iat", "uid")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithRoleEditor() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.EDITOR));

        // when.
        String token = userJwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": ["
                        + "    {"
                        + "      \"name\": \"EDITOR\","
                        + "      \"authorities\": ["
                        + "        \"scheduled-tasks:write\","
                        + "        \"caches:clear\","
                        + "        \"gc:manage\","
                        + "        \"caches:toggle\""
                        + "      ],"
                        + "      \"components\": []"
                        + "    }"
                        + "  ]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token))
                .whenIgnoringPaths("exp", "iat", "uid")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithRoleViewer() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.VIEWER));

        // when.
        String token = userJwtEncoderService.generateToken(user);

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

        assertThatJson(getPayload(token))
                .whenIgnoringPaths("exp", "iat", "uid")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_MultipleRoles() {
        User user = UserUtils.withPassword(
                USER_NAME, PASSWORD, Set.of(TestRoles.ADMIN, TestRoles.EDITOR, TestRoles.VIEWER));

        // when.
        String token = userJwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format(
                "{" + "  \"sub\": \"%s\","
                        + "  \"roles\": ["
                        + "    {"
                        + "      \"name\": \"ADMIN\","
                        + "      \"authorities\": ["
                        + "        \"scheduled-tasks:write\","
                        + "        \"caches:clear\","
                        + "        \"gc:manage\","
                        + "        \"environment.values:read\","
                        + "        \"config-properties.values:read\","
                        + "        \"caches:toggle\""
                        + "      ],"
                        + "      \"components\": []"
                        + "    },"
                        + "    {"
                        + "      \"name\": \"EDITOR\","
                        + "      \"authorities\": ["
                        + "        \"scheduled-tasks:write\","
                        + "        \"caches:clear\","
                        + "        \"gc:manage\","
                        + "        \"caches:toggle\""
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
                .whenIgnoringPaths("exp", "iat", "uid")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_ForUserWithoutRoles() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of());

        // when.
        String token = userJwtEncoderService.generateToken(user);

        // then.
        // language=json
        String expectedPayload = String.format("{" + "  \"sub\": \"%s\"," + "  \"roles\": []" + "}", USER_NAME);

        assertThatJson(getPayload(token)).whenIgnoringPaths("exp", "iat", "uid").isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_WithHs256() {
        String key256 = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";
        UserJwtEncoderService encoder = new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC256, key256, lifespan);

        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.EDITOR));

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
                        + "        \"scheduled-tasks:write\","
                        + "        \"caches:clear\","
                        + "        \"gc:manage\","
                        + "        \"caches:toggle\""
                        + "      ],"
                        + "      \"components\": []"
                        + "    }"
                        + "  ]"
                        + "}",
                USER_NAME);

        assertThatJson(getPayload(token))
                .whenIgnoringPaths("exp", "iat", "uid")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtToken_WithHs384() {
        String key384 =
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437";
        UserJwtEncoderService encoder = new DefaultUserJwtEncoderService(JwtAlgorithm.HMAC384, key384, lifespan);

        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.VIEWER));

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

        assertThatJson(getPayload(token)).whenIgnoringPaths("exp", "iat", "uid").isEqualTo(expectedPayload);
    }

    @Test
    void shouldFailWithInsufficientlyShortSecretKey() {
        String shortSecretKey = "shortKey";
        JwtAlgorithm jwtAlgorithm = JwtAlgorithm.HMAC256;
        DefaultUserJwtEncoderService invalidService =
                new DefaultUserJwtEncoderService(jwtAlgorithm, shortSecretKey, lifespan);

        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of(TestRoles.EDITOR));

        assertThatThrownBy(() -> invalidService.generateToken(user)).isInstanceOf(JwtTokenGenerationException.class);
    }

    @Test
    void shouldGenerateProperlyFormattedToken() {
        User user = UserUtils.withPassword(USER_NAME, PASSWORD, Set.of());
        String token = userJwtEncoderService.generateToken(user);

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
        UserJwtEncoderService jwtEncoderService(
                final @Value("${axelix.master.auth.jwt.algorithm}") JwtAlgorithm algorithm,
                final @Value("${axelix.master.auth.jwt.lifespan}") Duration lifespan,
                final @Value("${axelix.master.auth.jwt.signing_key}") String signingKey) {
            return new DefaultUserJwtEncoderService(algorithm, signingKey, lifespan);
        }
    }
}
