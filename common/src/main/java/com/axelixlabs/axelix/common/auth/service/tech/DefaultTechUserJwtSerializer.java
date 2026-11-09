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
package com.axelixlabs.axelix.common.auth.service.tech;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.JwtTokenGenerationException;
import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategy;
import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategyFactory;
import com.axelixlabs.axelix.common.contract.jwt.TechUserJwtToken;

/**
 * Default {@link TechUserJwtSerializer} implementation.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public class DefaultTechUserJwtSerializer implements TechUserJwtSerializer {

    /**
     * Token is extremely short-lived since it is issued on per-request basis.
     * It also addresses the security concern.
     */
    private static final Duration LIFESPAN = Duration.ofMinutes(1);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JwtSigningStrategy signingStrategy;

    private final String signingKey;

    public DefaultTechUserJwtSerializer(JwtAlgorithm algorithm, String signingKey) {
        this.signingStrategy = JwtSigningStrategyFactory.createSigningStrategy(algorithm);
        this.signingKey = signingKey;
    }

    @Override
    public String serialize(TechUser techUser) throws JwtTokenGenerationException {
        try {
            Instant now = Instant.now();
            TechUserJwtToken token = new TechUserJwtToken(
                    techUser.getId(), now.getEpochSecond(), now.plus(LIFESPAN).getEpochSecond());
            return signingStrategy
                    .signToken(Jwts.builder().content(OBJECT_MAPPER.writeValueAsBytes(token)), signingKey)
                    .compact();
        } catch (JwtTokenGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Failed to generate technical user JWT token", e);
        }
    }
}
