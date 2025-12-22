/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import com.nucleonforge.axile.common.auth.JwtAlgorithm;
import com.nucleonforge.axile.common.auth.rbac.core.DefaultUser;
import com.nucleonforge.axile.common.auth.rbac.core.User;
import com.nucleonforge.axile.master.exception.auth.JwtTokenGenerationException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link BasicJwtEncoderService}, verifying correct JWT generation logic.
 *
 * @author Nikita Kirillov
 * @since 22.12.2025
 */
@EnableConfigurationProperties
@SpringBootTest
class BasicJwtEncoderServiceTest {

    @Autowired
    private JwtEncoderService jwtEncoderService;

    @Value("${axile.master.auth.jwt.lifespan}")
    private Duration lifespan;

    @Test
    void shouldGenerateValidJwtToken() {
        User user = new DefaultUser("testUser", "testPassword", Collections.emptySet());

        String token = jwtEncoderService.generateToken(user);
        String responsePayload = getPayload(token);

        // language=json
        String expectedPayload = """
        {
          "sub": "testUser"
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
    void shouldFailWithInsufficientlyShortSecretKey() {
        String shortSecretKey = "shortKey";
        JwtAlgorithm jwtAlgorithm = JwtAlgorithm.HMAC256;
        JwtEncoderService invalidService = new BasicJwtEncoderService(jwtAlgorithm, shortSecretKey, lifespan);

        User user = new DefaultUser("invalidKeyUser", "testPassword", Set.of());

        assertThatThrownBy(() -> invalidService.generateToken(user)).isInstanceOf(JwtTokenGenerationException.class);
    }

    @Test
    void shouldGenerateTokenWithHs256() {
        String hs256Key = "6f0ac45fa8c1358a9c6acf6af78ec7bbd984af99c7fd1e9220304624d29105b3";
        JwtAlgorithm algorithm = JwtAlgorithm.HMAC256;
        JwtEncoderService encoder = new BasicJwtEncoderService(algorithm, hs256Key, lifespan);

        User user = new DefaultUser("hs256User", "testPassword", Collections.emptySet());

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
        String expectedPayload = """
        {
          "sub": "hs256User"
        }
        """;

        assertThatJson(responseHeader).isEqualTo(expectedHeader);
        assertThatJson(responsePayload).whenIgnoringPaths("exp", "iat").isEqualTo(expectedPayload);
    }

    @Test
    void shouldGenerateTokenWithHs384() {
        String hs384Key =
                "4eff557c362950836cd5685c80e82197e914811d7589da1248477a22423665c546ab3700b424587576d4b20180d7234b";
        JwtAlgorithm algorithm = JwtAlgorithm.HMAC384;
        JwtEncoderService encoder = new BasicJwtEncoderService(algorithm, hs384Key, lifespan);

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
        String expectedPayload = """
        {
          "sub": "hs384User"
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

    private String getPayload(String token) {
        String[] parts = token.split("\\.");
        return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    }

    private String getHeader(String token) {
        String[] parts = token.split("\\.");
        return new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
    }
}
