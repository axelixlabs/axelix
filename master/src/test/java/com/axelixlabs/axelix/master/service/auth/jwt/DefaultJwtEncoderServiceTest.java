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
import java.util.Base64;
import java.util.Collections;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.ExternalAuthority;
import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration;
import com.axelixlabs.axelix.master.exception.auth.JwtTokenGenerationException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link JwtEncoderService}, verifying correct JWT generation logic.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@EnableConfigurationProperties
@SpringBootTest(classes = SecurityAutoConfiguration.JwtAutoConfiguration.class)
class DefaultJwtEncoderServiceTest {

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @Value("${axelix.master.auth.jwt.signing_key}")
    private String signingKey;

    @Value("${axelix.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Test
    void shouldGenerateTokenWithRequiredClaims() {
        User user = new DefaultUser("testUser", "testPassword", Collections.emptySet());
        String token = jwtEncoderService.generateToken(user);

        Jws<Claims> claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getBytes()))
                .build()
                .parseSignedClaims(token);

        assertThat(claims.getPayload().getSubject()).isEqualTo("testUser");
        assertThat(claims.getPayload().getExpiration()).isNotNull();
        assertThat(claims.getPayload().getIssuedAt()).isNotNull();
    }

    @Test
    void shouldGenerateValidJwtToken() {
        Role role = new DefaultRole(
                "testRole", Set.of(ExternalAuthority.ENV, ExternalAuthority.BEANS), Collections.emptySet());
        User user = new DefaultUser("testUser", "testPassword", Set.of(role));

        String token = jwtEncoderService.generateToken(user);
        String responsePayload = getPayload(token);

        // language=json
        String expectedPayload =
                """
            {
              "sub": "testUser",
              "roles": [{
                "name": "testRole",
                "authorities": ["ENV", "BEANS"],
                "components": []
              }]
            }
            """;

        assertThatJson(responsePayload)
                .whenIgnoringPaths("exp", "iat")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtTokenMultipleRoles() {
        Role role1 = new DefaultRole(
                "firstTestRole", Set.of(ExternalAuthority.HEALTH, ExternalAuthority.INFO), Collections.emptySet());
        Role role2 = new DefaultRole("secondTestRole", Set.of(ExternalAuthority.BEANS), Collections.emptySet());

        User user = new DefaultUser("multiRoleUser", "testPassword", Set.of(role1, role2));

        String token = jwtEncoderService.generateToken(user);
        String responsePayload = getPayload(token);

        // language=json
        String expectedPayload =
                """
            {
              "sub": "multiRoleUser",
              "roles": [
                {
                  "name": "firstTestRole",
                  "authorities": ["HEALTH", "INFO"],
                  "components": []
                },
                {
                  "name": "secondTestRole",
                  "authorities": ["BEANS"],
                  "components": []
                }
              ]
            }
            """;

        assertThatJson(responsePayload)
                .whenIgnoringPaths("exp", "iat")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateValidJwtTokenWithRoleHierarchy() {
        Role adminRole = createAdminRoleHierarchy();
        Role rootRole = new DefaultRole("rootRole", Set.of(ExternalAuthority.HEALTH), Set.of(adminRole));
        User user = new DefaultUser("multiRoleUser", "testPassword", Set.of(rootRole));

        String token = jwtEncoderService.generateToken(user);
        String responsePayload = getPayload(token);

        // language=json
        String expectedPayload =
                """
            {
              "sub": "multiRoleUser",
              "roles": [
                {
                  "name": "rootRole",
                  "authorities": ["HEALTH"],
                  "components": [
                    {
                      "name": "admin",
                      "authorities": ["PROFILE_MANAGEMENT"],
                      "components": [
                        {
                          "name": "engineer",
                          "authorities": ["ENV"],
                          "components": [
                            {
                              "name": "user",
                              "authorities": ["INFO"],
                              "components": []
                            }
                          ]
                        },
                        {
                          "name": "cacheDispatcherRole",
                          "authorities": ["CACHE_DISPATCHER"],
                          "components": [
                            {
                              "name": "cacheAccessRole",
                              "authorities": ["CACHES"],
                              "components": []
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

        assertThatJson(responsePayload)
                .whenIgnoringPaths("exp", "iat")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldContainCorrectExpirationTime() throws JsonProcessingException {
        User user = new DefaultUser("expUser", "testPassword", Set.of());

        String token = jwtEncoderService.generateToken(user);
        String payload = getPayload(token);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(payload);

        long actualExpiration = node.get("exp").asLong() - node.get("iat").asLong();

        assertThat(actualExpiration).isEqualTo(lifespan.toSeconds());
    }

    @Test
    void shouldHandleNullUser() {
        assertThatThrownBy(() -> jwtEncoderService.generateToken(null)).isInstanceOf(JwtTokenGenerationException.class);
    }

    @Test
    void shouldThrowWhenUsernameIsNull() {
        User user = new DefaultUser(null, "testPassword", Set.of());

        assertThatThrownBy(() -> jwtEncoderService.generateToken(user)).isInstanceOf(JwtTokenGenerationException.class);
    }

    @Test
    void shouldGenerateTokenForUserWithoutRoles() {
        User user = new DefaultUser("userWithoutRoles", "testPassword", null);

        String token = jwtEncoderService.generateToken(user);
        String responsePayload = getPayload(token);

        // language=json
        String expectedPayload =
                """
            {
              "sub": "userWithoutRoles",
              "roles": []
            }
            """;

        assertThatJson(responsePayload).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateTokenForUserWithRoleThatHasNoAuthoritiesAndNoComponents() {
        Role roleWithoutAuthorities =
                new DefaultRole("roleWithoutAuthorities", Collections.emptySet(), Collections.emptySet());
        User user = new DefaultUser("userWithEmptyAuthorities", "testPassword", Set.of(roleWithoutAuthorities));

        String token = jwtEncoderService.generateToken(user);
        String responsePayload = getPayload(token);

        // language=json
        String expectedPayload =
                """
            {
            "sub": "userWithEmptyAuthorities",
            "roles": [{
              "name": "roleWithoutAuthorities",
              "authorities": [],
              "components": []
            }]
            }
            """;

        assertThatJson(responsePayload).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldFailWithInsufficientlyShortSecretKey() {
        String shortSecretKey = "shortKey";
        JwtAlgorithm jwtAlgorithm = JwtAlgorithm.HMAC256;
        DefaultJwtEncoderService invalidService = new DefaultJwtEncoderService(jwtAlgorithm, shortSecretKey, lifespan);

        User user = new DefaultUser("invalidKeyUser", "testPassword", Set.of());

        assertThatThrownBy(() -> invalidService.generateToken(user)).isInstanceOf(JwtTokenGenerationException.class);
    }

    @Test
    void shouldGenerateTokenWithHs256() {
        String hs256Key = "6f0ac45fa8c1358a9c6acf6af78ec7bbd984af99c7fd1e9220304624d29105b3";
        JwtAlgorithm algorithm = JwtAlgorithm.HMAC256;
        JwtEncoderService encoder = new DefaultJwtEncoderService(algorithm, hs256Key, lifespan);

        Role role = new DefaultRole("role", Set.of(ExternalAuthority.HEALTH, ExternalAuthority.INFO), null);
        User user = new DefaultUser("hs256User", "testPassword", Set.of(role));

        String token = encoder.generateToken(user);
        String responseHeader = getHeader(token);
        String responsePayload = getPayload(token);

        // language=json
        String expectedHeader = """
            {
              "alg": "HS256"
            }
            """;

        // language=json
        String expectedPayload =
                """
            {
              "sub": "hs256User",
              "roles": [{
                "name": "role",
                "authorities": ["HEALTH", "INFO"],
                "components": []
              }]
            }
            """;

        assertThatJson(responseHeader).isEqualTo(expectedHeader);
        assertThatJson(responsePayload)
                .whenIgnoringPaths("exp", "iat")
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateTokenWithHs384() {
        String hs384Key =
                "4eff557c362950836cd5685c80e82197e914811d7589da1248477a22423665c546ab3700b424587576d4b20180d7234b";
        JwtAlgorithm algorithm = JwtAlgorithm.HMAC384;
        JwtEncoderService encoder = new DefaultJwtEncoderService(algorithm, hs384Key, lifespan);

        User user = new DefaultUser("hs384User", "testPassword", Set.of());

        String token = encoder.generateToken(user);
        String responseHeader = getHeader(token);
        String responsePayload = getPayload(token);

        // language=json
        String expectedHeader = """
            {
              "alg": "HS384"
            }
            """;
        // language=json
        String expectedPayload =
                """
            {
              "sub": "hs384User",
              "roles": []
            }
            """;

        assertThatJson(responseHeader).isEqualTo(expectedHeader);
        assertThatJson(responsePayload).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateProperlyFormattedToken() {
        User user = new DefaultUser("formatTest", "testPassword", Set.of());
        String token = jwtEncoderService.generateToken(user);

        assertThat(token.split("\\.")).hasSize(3);
    }

    private Role createAdminRoleHierarchy() {
        Role cacheAccessRole =
                new DefaultRole("cacheAccessRole", Set.of(ExternalAuthority.CACHES), Collections.emptySet());
        Role cacheDispatcherRole = new DefaultRole(
                "cacheDispatcherRole", Set.of(ExternalAuthority.CACHE_DISPATCHER), Set.of(cacheAccessRole));
        Role userRole = new DefaultRole("user", Set.of(ExternalAuthority.INFO), Collections.emptySet());
        Role engineerRole = new DefaultRole("engineer", Set.of(ExternalAuthority.ENV), Set.of(userRole));
        return new DefaultRole(
                "admin", Set.of(ExternalAuthority.PROFILE_MANAGEMENT), Set.of(engineerRole, cacheDispatcherRole));
    }

    private String getPayload(String token) {
        String[] parts = token.split("\\.");
        return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    }

    private String getHeader(String token) {
        String[] parts = token.split("\\.");
        return new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
    }
}
