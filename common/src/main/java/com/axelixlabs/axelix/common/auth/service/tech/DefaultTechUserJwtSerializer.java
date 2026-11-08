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

import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategy;
import com.axelixlabs.axelix.common.auth.service.JwtSigningStrategyFactory;
import io.jsonwebtoken.Jwts;
import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.JwtAlgorithm;
import com.axelixlabs.axelix.common.auth.core.TechUser;
import com.axelixlabs.axelix.common.auth.exception.JwtTokenGenerationException;

/**
 * Default {@link TechUserJwtSerializer} implementation.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public class DefaultTechUserJwtSerializer implements TechUserJwtSerializer {

    private final JwtSigningStrategy signingStrategy;

    private final String signingKey;

    public DefaultTechUserJwtSerializer(JwtAlgorithm algorithm, String signingKey) {
        this.signingStrategy = JwtSigningStrategyFactory.createSigningStrategy(algorithm);
        this.signingKey = signingKey;
    }

    @Override
    public String serialize(TechUser techUser) throws JwtTokenGenerationException {
        try {
            return signingStrategy
                    .signToken(Jwts.builder().subject(techUser.getId()), signingKey)
                    .compact();
        } catch (JwtTokenGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Failed to generate technical user JWT token", e);
        }
    }
}
