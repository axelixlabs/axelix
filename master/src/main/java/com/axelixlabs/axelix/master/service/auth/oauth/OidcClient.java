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

import java.security.PublicKey;

import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;

/**
 *
 */
public interface OidcClient {

    /**
     * Exchanges an authorization code for an ID Token using the
     * Authorization Code Flow as defined in RFC 6749 Section 4.1.3.
     *
     * @param code the authorization code received from the OIDC provider
     * @return the ID Token string
     * @throws OidcTokenExchangeException if the exchange fails or the response is invalid
     */
    String exchangeCodeForIdToken(String code) throws OidcTokenExchangeException;

    /**
     * Fetches and constructs a public key from the OIDC provider's JWKS endpoint
     * that corresponds to the given key ID.
     * <p>
     * The key is located by matching the provided {@code kid} against the keys
     * returned from the JWKS URI as defined in RFC 7517.
     *
     * @param keyId the key ID ({@code kid}) extracted from the JWT header
     * @return the public key used to verify the JWT signature
     * @throws JwtParsingException if the JWKS response is empty, the key is not found,
     *                             or the key cannot be constructed
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7517">RFC 7517 - JSON Web Key (JWK)</a>
     */
    PublicKey fetchPublicKey(String keyId) throws JwtParsingException;
}
