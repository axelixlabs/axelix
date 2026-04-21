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

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;

/**
 * OIDC client for authorization code exchange and public key retrieval.
 *
 * @author Nikita Kirillov
 * @since 27.02.2026
 */
public interface OidcClient {

    /**
     * Exchanges an authorization code for an ID Token using the
     * Authorization Code Flow as defined in RFC 6749 Section 4.1.3.
     *
     * @param code the authorization code received from the OIDC provider
     * @return the Tokens object containing id token and access token
     * @throws OidcTokenExchangeException if the exchange fails or the response is invalid
     */
    Tokens exchangeCodeForTokens(String code) throws OidcTokenExchangeException;

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
     * @param idToken the OIDC ID Token to validate and process
     * @return the extracted username
     * @throws ExpiredJwtTokenException if the token has expired
     * @throws InvalidJwtTokenException if the token signature is invalid or tampered
     * @throws JwtProcessingException   if the token cannot be parsed
     */
    String validateIdTokenAndExtractUsername(String idToken)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtProcessingException;

    /**
     * Verifies the given OAuth2 access token by calling the OIDC provider's userinfo_endpoint.
     *
     * <p><b>Note:</b> This method requires the provider to support the userinfo_endpoint,
     * which is RECOMMENDED but not mandatory in OpenID Connect Discovery 1.0.
     * If the endpoint is not available, a {@link OidcTokenExchangeException} will be thrown.</p>
     *
     * @param accessToken access token to verify
     * @return userInfo json
     * @throws OidcTokenExchangeException       if the token is invalid, expired or malformed
     * @throws OidcMetadataUnavailableException if userinfo_endpoint is unavailable
     */
    @Nullable
    String validateAccessTokenAndExtractUserInfo(String accessToken) throws OidcTokenExchangeException;
}
