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
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.master.autoconfiguration.auth.OAuth2Properties;

/**
 * Default implementation of {@link OidcTokenProcessor}
 *
 * @since 27.02.2026
 * @author Nikita Kirillov
 */
public class DefaultOidcTokenProcessor implements OidcTokenProcessor {

    private final OidcClient oidcClient;
    private final OAuth2Properties oAuth2Properties;
    private final ObjectMapper objectMapper;

    public DefaultOidcTokenProcessor(OidcClient oidcClient, OAuth2Properties oAuth2Properties) {
        this.oidcClient = oidcClient;
        this.oAuth2Properties = oAuth2Properties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Needs additional validation the {@code iss} (issuer) and {@code aud} (audience) claims
     * as required by OpenID Connect Core 1.0 Section 3.1.3.7 ID Token Validation (points 2 and 3).
     * <p>
     * The {@code aud} claim must equal the {@code clientId}.
     * The {@code iss} claim must equal the {@code issuerUri}.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation"> OpenID Connect Core 1.0 - ID Token Validation</a>
     */
    @Override
    public String validateOAuth2JwtTokenAndExtractUsername(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtParsingException {
        try {
            String kid = extractKid(token);
            PublicKey publicKey = oidcClient.fetchPublicKey(kid);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(oAuth2Properties.issuerUri())
                    .requireAudience(oAuth2Properties.clientId())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return extractUsername(claims);

        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtTokenException("OAuth2Jwt token has expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("OAuth2Jwt token is invalid or tampered", e);
        } catch (Exception e) {
            throw new JwtParsingException("Unexpected error while decoding OAuth2Jwt token", e);
        }
    }

    private String extractKid(String token) {
        try {
            String headerPart = token.split("\\.")[0];
            String headerJson = new String(Base64.getUrlDecoder().decode(headerPart));
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            String kid = (String) header.get("kid");
            if (kid == null) {
                throw new JwtParsingException("kid is missing in OAuth2Jwt header");
            }
            return kid;
        } catch (JsonProcessingException e) {
            throw new JwtParsingException("Failed to parse OAuth2Jwt header", e);
        }
    }

    /**
     * Extracts the username from JWT claims.
     * Falls back to {@code sub} (subject) as a last resort it is always present per OIDC spec.
     */
    private String extractUsername(Claims claims) {
        if (oAuth2Properties.usernameClaim() != null) {
            String username = claims.get(oAuth2Properties.usernameClaim(), String.class);
            if (username != null) {
                return username;
            }
        }

        String preferredUsername = claims.get("preferred_username", String.class);
        if (preferredUsername != null) {
            return preferredUsername;
        }

        String name = claims.get("name", String.class);
        if (name != null) {
            return name;
        }

        return claims.getSubject();
    }
}
