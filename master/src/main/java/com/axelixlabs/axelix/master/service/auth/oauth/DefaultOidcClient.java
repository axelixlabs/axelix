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

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.master.autoconfiguration.auth.OAuth2Properties;
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

    public DefaultOidcClient(
            RestClient restClient, OAuth2Properties oAuth2Properties, OidcMetadataProvider oidcMetadataProvider) {
        this.restClient = restClient;
        this.oAuth2Properties = oAuth2Properties;
        this.oidcMetadataProvider = oidcMetadataProvider;
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
    public String exchangeCodeForIdToken(String code) throws OidcTokenExchangeException {
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

            if (response == null) {
                throw new OidcTokenExchangeException("OIDC token endpoint returned empty response");
            }

            Object idTokenObj = response.get("id_token");
            if (!(idTokenObj instanceof String idToken)) {
                throw new OidcTokenExchangeException("Invalid or missing id_token in response from OIDC provider");
            }

            return idToken;

        } catch (OidcTokenExchangeException e) {
            throw e;
        } catch (Exception e) {
            throw new OidcTokenExchangeException("Failed to exchange authorization code for token", e);
        }
    }

    @Override
    public PublicKey fetchPublicKey(String keyId) throws JwtParsingException {
        String jwksUri = oidcMetadataProvider.getJwksUri();

        Map<String, Object> body = restClient.get().uri(jwksUri).retrieve().body(new ParameterizedTypeReference<>() {});

        if (body == null) {
            throw new JwtParsingException("JWKS response is empty from " + jwksUri);
        }

        List<Map<String, Object>> keys = getKeysList(body);

        Map<String, Object> jwk = keys.stream()
                .filter(key -> keyId.equals(key.get("kid")))
                .findFirst()
                .orElseThrow(() -> new JwtParsingException("Public key not found for kid: " + keyId));

        return buildPublicKey(jwk);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getKeysList(Map<String, Object> body) {
        Object keysObj = body.get("keys");

        if (!(keysObj instanceof List<?> keysList)) {
            throw new JwtParsingException("JWKS response does not contain a valid 'keys' list");
        }

        for (Object item : keysList) {
            if (!(item instanceof Map)) {
                throw new JwtParsingException("Invalid JWKS key format: key is not a Map");
            }
        }

        return (List<Map<String, Object>>) keysList;
    }

    private PublicKey buildPublicKey(Map<String, Object> jwk) {

        String keyType = (String) jwk.get("kty");

        return switch (keyType) {
            case "RSA" -> buildRsaPublicKey(jwk);
            case "EC" -> buildEcPublicKey(jwk);
            default -> throw new JwtParsingException("Unsupported key type: " + keyType);
        };
    }

    /**
     * Builds an RSA PublicKey from a JSON Web Key (JWK) map.
     * <ul>
     *  <li> - "n" (Modulus)  — the Base64urlUInt-encoded modulus of the RSA public key</li>
     *  <li> - "e" (Exponent) — the Base64urlUInt-encoded public exponent of the RSA public key </li>
     * </ul>
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7518#section-6.3.1">RFC 7518 Section 6.3.1</a>
     */
    private PublicKey buildRsaPublicKey(Map<String, Object> jwk) {
        try {
            String n = (String) jwk.get("n");
            String e = (String) jwk.get("e");

            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception ex) {
            throw new JwtParsingException("Failed to build RSA public key", ex);
        }
    }

    /**
     * Builds an EC PublicKey from a JSON Web Key (JWK) map.
     * <ul>
     *  <li> - "crv" (Curve)   — the cryptographic curve used </li>
     *  <li> - "x"  (X)        — the Base64urlUInt-encoded x coordinate </li>
     *  <li> - "y"  (Y)        — the Base64urlUInt-encoded y coordinate</li>
     * </ul>
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7518#section-6.2.1">RFC 7518 Section 6.2.1</a>
     */
    private PublicKey buildEcPublicKey(Map<String, Object> jwk) {
        try {
            String x = (String) jwk.get("x");
            String y = (String) jwk.get("y");
            String crv = (String) jwk.get("crv");

            ECPoint ecPoint = new ECPoint(
                    new BigInteger(1, Base64.getUrlDecoder().decode(x)),
                    new BigInteger(1, Base64.getUrlDecoder().decode(y)));

            String algorithm =
                    switch (crv) {
                        case "P-256" -> "secp256r1";
                        case "P-384" -> "secp384r1";
                        case "P-521" -> "secp521r1";
                        default -> throw new JwtParsingException("Unsupported EC curve: " + crv);
                    };

            AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("EC");
            algorithmParameters.init(new ECGenParameterSpec(algorithm));

            ECParameterSpec ecParameterSpec = algorithmParameters.getParameterSpec(ECParameterSpec.class);

            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);
            return KeyFactory.getInstance("EC").generatePublic(keySpec);
        } catch (Exception ex) {
            throw new JwtParsingException("Failed to build EC public key", ex);
        }
    }
}
