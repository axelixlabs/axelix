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
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.JwtTokenGenerationException;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.common.auth.service.tech.TechUserJwtSerializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultTechUserJwtSerializer}.
 *
 * @author Mikhail Polivakha
 */
class DefaultTechUserJwtSerializerTest {

    private static final String SIGNING_KEY = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";

    private final TechUserJwtSerializer serializer =
            new DefaultTechUserJwtSerializer(JwtAlgorithm.HMAC256, SIGNING_KEY, Duration.ofMinutes(5));

    @ParameterizedTest
    @EnumSource(TechUser.class)
    void shouldSerializeTechnicalUserWithIssuedAtAndExpiration(TechUser techUser) {
        // given.
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        // when.
        String token = serializer.serialize(techUser);

        // then.
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token);
        assertThat(claims.getPayload()).containsOnlyKeys("sub", "iat", "exp");
        assertThat(claims.getPayload().getSubject()).isEqualTo(techUser.getId());
        assertThat(claims.getPayload().getIssuedAt().toInstant()).isBetween(before, Instant.now());
        assertThat(Duration.between(
                        claims.getPayload().getIssuedAt().toInstant(),
                        claims.getPayload().getExpiration().toInstant()))
                .isEqualTo(Duration.ofMinutes(5));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(longs = {60, 300, 3600})
    void shouldUseConfiguredLifespan(long seconds) {
        // given.
        TechUserJwtSerializer configuredSerializer =
                new DefaultTechUserJwtSerializer(JwtAlgorithm.HMAC256, SIGNING_KEY, Duration.ofSeconds(seconds));

        // when.
        String token = configuredSerializer.serialize(TechUser.MASTER);

        // then.
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertThat(Duration.between(
                        claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()))
                .isEqualTo(Duration.ofSeconds(seconds));
    }

    @Test
    void shouldFailWhenTokenCannotBeSigned() {
        // given.
        TechUserJwtSerializer invalidSerializer =
                new DefaultTechUserJwtSerializer(JwtAlgorithm.HMAC256, "shortKey", Duration.ofMinutes(5));

        // when / then.
        assertThatThrownBy(() -> invalidSerializer.serialize(TechUser.MASTER))
                .isInstanceOf(JwtTokenGenerationException.class);
    }
}
