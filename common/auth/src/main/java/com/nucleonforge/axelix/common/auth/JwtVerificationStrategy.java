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
package com.nucleonforge.axelix.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

/**
 * Strategy interface for verifying and parsing JWT tokens with a specific signing key.
 *
 * <p>Implementations of this interface are responsible for validating JWT tokens,
 * verifying their digital signatures, and returning the parsed claims if verification succeeds.</p>
 *
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
public interface JwtVerificationStrategy {

    /**
     * Verifies and parses a JWT token using the provided signing key.
     *
     * @param token the JWT token to verify and parse
     * @param signingKey the key used to verify the token's signature
     * @return verified JWT claims in JWS format
     * @throws JwtException if verification fails (invalid signature, expired token, etc.)
     * @throws IllegalArgumentException if the token or key is malformed
     */
    Jws<Claims> verifyAndParse(String token, String signingKey) throws JwtException;
}
