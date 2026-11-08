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
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtDeserializer;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.common.auth.service.tech.TechUserJwtDeserializer;
import com.axelixlabs.axelix.common.auth.service.tech.TechUserJwtSerializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultTechUserJwtDeserializer}.
 *
 * @author Mikhail Polivakha
 */
class DefaultTechUserJwtDeserializerTest {

    private static final String SIGNING_KEY = "79912c6adb2a4f6c78a859807b072ce2a2c1140ac578f324cca983db22868b14";

    private final TechUserJwtSerializer serializer =
            new DefaultTechUserJwtSerializer(JwtAlgorithm.HMAC256, SIGNING_KEY);

    private final TechUserJwtDeserializer deserializer =
            new DefaultTechUserJwtDeserializer(JwtAlgorithm.HMAC256, SIGNING_KEY);

    @ParameterizedTest
    @EnumSource(TechUser.class)
    void shouldDeserializeTechnicalUser(TechUser techUser) {
        // given.
        String token = serializer.serialize(techUser);

        // when.
        TechUser deserialized = deserializer.deserialize(token);

        // then.
        assertThat(deserialized).isEqualTo(techUser);
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        // given.
        TechUserJwtSerializer anotherSerializer = new DefaultTechUserJwtSerializer(
                JwtAlgorithm.HMAC256,
                "bfa30eb1f16c07ba0a6a19a60f7c4bc02e1e10670411ae7a2f206b2bfe8801e2bb40741469d95fbbf4c86ae4b4a68437");
        String token = anotherSerializer.serialize(TechUser.MASTER);

        // when / then.
        assertThatThrownBy(() -> deserializer.deserialize(token)).isInstanceOf(InvalidJwtTokenException.class);
    }

    @Test
    void shouldRejectUnknownTechnicalUserSubject() {
        // given.
        String token = Jwts.builder()
                .subject("UNKNOWN")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        // when / then.
        assertThatThrownBy(() -> deserializer.deserialize(token)).isInstanceOf(JwtParsingException.class);
    }

    @Test
    void shouldRejectMissingIssuedAtClaim() {
        // given.
        String token = Jwts.builder()
                .subject(TechUser.MASTER.getId())
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        // when / then.
        assertThatThrownBy(() -> deserializer.deserialize(token)).isInstanceOf(JwtParsingException.class);
    }

    @Test
    void shouldRejectMissingExpirationClaim() {
        // given.
        String token = Jwts.builder()
                .subject(TechUser.MASTER.getId())
                .issuedAt(Date.from(Instant.now()))
                .signWith(Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        // when / then.
        assertThatThrownBy(() -> deserializer.deserialize(token)).isInstanceOf(JwtParsingException.class);
    }

    @Test
    void shouldRejectMissingLifetimeClaims() {
        // given.
        String token = Jwts.builder()
                .subject(TechUser.MASTER.getId())
                .signWith(Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        // when / then.
        assertThatThrownBy(() -> deserializer.deserialize(token)).isInstanceOf(JwtParsingException.class);
    }

    @Test
    void shouldRejectExpiredToken() {
        // given.
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(TechUser.MASTER.getId())
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SIGNING_KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        // when / then.
        assertThatThrownBy(() -> deserializer.deserialize(token)).isInstanceOf(ExpiredJwtTokenException.class);
    }
}
