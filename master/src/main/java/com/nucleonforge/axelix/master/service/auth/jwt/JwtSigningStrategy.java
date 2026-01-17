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

import io.jsonwebtoken.JwtBuilder;

import com.nucleonforge.axelix.master.exception.auth.JwtTokenGenerationException;

/**
 * Strategy interface for signing JWT tokens with a specific signing key.
 *
 * <p>Implementations of this interface are responsible for applying digital signatures
 * to JWT tokens using cryptographic algorithms and key types.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public interface JwtSigningStrategy {

    /**
     * Applies a digital signature to the JWT token using the provided signing key.
     *
     * @param builder the JWT builder to apply the signature to
     * @param signingKey the key used for signing the token
     * @return the signed JwtBuilder instance
     * @throws JwtTokenGenerationException if signing fails due to invalid key,
     *         weak key, or other cryptographic issues
     */
    JwtBuilder signToken(JwtBuilder builder, String signingKey) throws JwtTokenGenerationException;
}
