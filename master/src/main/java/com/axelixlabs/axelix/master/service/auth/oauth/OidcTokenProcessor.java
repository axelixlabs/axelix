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
package com.axelixlabs.axelix.master.service.auth.oauth;

import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;

/**
 * Processor for validating and extracting user information from OIDC ID Tokens.
 * <p>
 * Validates the ID Token according to the OpenID Connect Core specification
 * Section 3.1.3.7 and extracts the username from the token claims.
 *
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation">
 *      OpenID Connect Core 1.0 - ID Token Validation</a>
 *
 * @since 27.02.2026
 * @author Nikita Kirillov
 */
public interface OidcTokenProcessor {

    /**
     * Validates the given OIDC ID Token and extracts the username from its claims.
     *
     * <p>Needs additional validation of the {@code iss} (issuer) and {@code aud} (audience) claims
     * as required by OpenID Connect Core 1.0 Section 3.1.3.7 ID Token Validation (points 2 and 3).</p>
     *
     * <p>The {@code aud} claim must equal the {@code client_id}.</p>
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation">
     *      OpenID Connect Core 1.0 - ID Token Validation</a>
     *
     * @param token the OIDC ID Token to validate and process
     * @return the extracted username
     * @throws ExpiredJwtTokenException if the token has expired
     * @throws InvalidJwtTokenException if the token signature is invalid or tampered
     * @throws JwtParsingException      if the token cannot be parsed
     */
    String validateOAuth2JwtTokenAndExtractUsername(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtParsingException;
}
