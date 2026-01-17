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
package com.nucleonforge.axelix.master.service.auth.jwt;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.WeakKeyException;

import com.nucleonforge.axelix.master.exception.auth.JwtTokenGenerationException;

/**
 * {@link JwtSigningStrategy} implementation that signs JWT tokens using HMAC-SHA algorithms.
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public class HmacSigningStrategy implements JwtSigningStrategy {

    private final MacAlgorithm algorithm;
    private final int minKeyLength;

    public HmacSigningStrategy(MacAlgorithm algorithm, int minKeyLength) {
        this.algorithm = algorithm;
        this.minKeyLength = minKeyLength;
    }

    @Override
    public JwtBuilder signToken(JwtBuilder builder, String signingKey) throws JwtTokenGenerationException {
        try {
            SecretKey key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
            return builder.signWith(key, algorithm);
        } catch (WeakKeyException e) {
            throw new JwtTokenGenerationException(
                    "The secret key is too weak for " + algorithm + " algorithm. " + "It must be at least "
                            + minKeyLength + " bytes.",
                    e);
        } catch (Exception e) {
            throw new JwtTokenGenerationException("Failed to sign JWT token", e);
        }
    }
}
