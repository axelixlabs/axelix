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
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;

/**
 * Default implementation of {@link OidcClient}
 *
 * @since 27.02.2026
 * @author Nikita Kirillov
 */
public class DefaultOidcClient implements OidcClient {

    private final RestClient restClient;

    private final OAuth2Properties oAuth2Properties;

    private final OidcMetadataProvider oidcMetadataProvider;

    private final ObjectMapper objectMapper;

    public DefaultOidcClient(
            RestClient restClient,
            OAuth2Properties oAuth2Properties,
            OidcMetadataProvider oidcMetadataProvider,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.oAuth2Properties = oAuth2Properties;
        this.oidcMetadataProvider = oidcMetadataProvider;
        this.objectMapper = objectMapper;
    }

    /**
     * <ul>
     *  <li>{@code grant_type} — identifies the grant type, must be set to {@code authorization_code}</li>
     *  <li>{@code code} — the authorization code received from the authorization server</li>
     *  <li>{@code redirect_uri} — must be identical to the one used in the authorization request</li>
     *  <li>{@code client_id} — the client identifier issued during registration as per Section 3.2.1</li>
     *  <li>{@code client_secret} — used for confidential client authentication as per Section 3.2.1</li>
     * </ul>
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.3">RFC 6749 Section 4.1.3 Access Token Request</a>
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-3.2.1">RFC 6749 Section 3.2.1 Client Authentication</a>
     */
    @Override
    public Tokens exchangeCodeForTokens(String code) throws OidcTokenExchangeException {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", oAuth2Properties.clientId());
            body.add("client_secret", oAuth2Properties.clientSecret());
            body.add("redirect_uri", oAuth2Properties.redirectUri());
            body.add("code", code);

            Map<String, Object> response = restClient
                    .post()
                    .uri(oidcMetadataProvider.getTokenEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            return validateAndExtractTokens(response);

        } catch (OidcTokenExchangeException e) {
            throw e;
        } catch (Exception e) {
            throw new OidcTokenExchangeException("Failed to exchange authorization code for token", e);
        }
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
    public String validateIdTokenAndExtractUsername(String idToken)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtParsingException {
        try {
            String kid = extractPublicKeyId(idToken);
            PublicKey publicKey = fetchPublicKey(kid);

            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(oAuth2Properties.issuerUri())
                    .requireAudience(oAuth2Properties.clientId())
                    .build()
                    .parseSignedClaims(idToken)
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

    @Override
    @Nullable
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public String validateAccessTokenAndExtractUserInfo(String accessToken)
            throws OidcTokenExchangeException, OidcMetadataUnavailableException {

        try {
            String userInfoEndpoint = oidcMetadataProvider.getUserInfoEndpoint();

            String userInfoBody = restClient
                    .get()
                    .uri(userInfoEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            if (userInfoBody == null) {
                throw new OidcMetadataUnavailableException(
                        "Failed to decode the response from user_info OIDC endpoint");
            }

            return userInfoBody;
        } catch (HttpClientErrorException e) {
            if (Set.of((HttpStatusCode) HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN)
                    .contains(e.getStatusCode())) {
                throw new OidcTokenExchangeException(
                        "Failed to validate access token via user_info endpoint: %s".formatted(e.getMessage()), e);
            }

            throw new OidcMetadataUnavailableException("Failed to decode the response from user_info OIDC endpoint", e);
        } catch (OidcMetadataUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new OidcMetadataUnavailableException("Failed to decode the response from user_info OIDC endpoint", e);
        }
    }

    private Tokens validateAndExtractTokens(@Nullable Map<String, Object> response) {
        if (response == null) {
            throw new OidcTokenExchangeException("OIDC token endpoint returned empty response");
        }

        if (!(response.get("id_token") instanceof String idToken)) {
            throw new OidcTokenExchangeException("Invalid or missing id_token in response from OIDC provider");
        }

        if (!(response.get("access_token") instanceof String accessToken)) {
            throw new OidcTokenExchangeException("Invalid or missing access_token in response from OIDC provider");
        }

        return new Tokens(idToken, accessToken);
    }

    private String extractPublicKeyId(String token) {
        try {
            String headerPart = token.split("\\.")[0];
            String headerJson = new String(Base64.getUrlDecoder().decode(headerPart));
            Map<String, Object> header = objectMapper.readValue(headerJson, new TypeReference<>() {});
            String kid = (String) header.get("kid");
            if (kid == null) {
                throw new JwtParsingException("kid is missing in OAuth2Jwt header");
            }
            return kid;
        } catch (JacksonException e) {
            throw new JwtParsingException("Failed to parse OAuth2Jwt header", e);
        }
    }

    /**
     * Fetches and constructs a public key from the OIDC provider's JWKS endpoint
     * that corresponds to the given key ID.
     * <p>
     * The key is located by matching the provided {@code kid} against the keys
     * returned from the JWKS URI as defined in RFC 7517.
     *
     * @param keyId the key ID ({@code kid}) extracted from the JWT header
     * @return the public key used to verify the JWT signature
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7517">RFC 7517 - JSON Web Key (JWK)</a>
     */
    private PublicKey fetchPublicKey(String keyId) throws JwtParsingException {
        String jwksUri = oidcMetadataProvider.getJwksUri();

        Map<String, Object> body = restClient.get().uri(jwksUri).retrieve().body(new ParameterizedTypeReference<>() {});

        if (body == null) {
            throw new JwtParsingException("JWKS response is empty from " + jwksUri);
        }

        try {
            JWKSet jwkSet = JWKSet.parse(body);
            JWK jwk = jwkSet.getKeyByKeyId(keyId);

            if (jwk == null) {
                throw new JwtParsingException("Public key not found for kid: " + keyId);
            }

            return buildPublicKey(jwk);

        } catch (ParseException e) {
            throw new JwtParsingException("Failed to parse JWKS response from " + jwksUri + ": " + e.getMessage(), e);
        }
    }

    private PublicKey buildPublicKey(JWK jwk) throws JwtParsingException {
        try {
            return switch (jwk.getKeyType().getValue()) {
                case "RSA" -> jwk.toRSAKey().toPublicKey();
                case "EC" -> jwk.toECKey().toPublicKey();
                case "OKP" ->
                    throw new JwtParsingException(
                            "EdDSA/OKP keys are not supported yet. Currently supported: RSA, EC.");
                default -> throw new JwtParsingException("Unsupported key type: " + jwk.getKeyType());
            };
        } catch (JOSEException e) {
            throw new JwtParsingException("Failed to build public key for kid: " + jwk.getKeyID(), e);
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
