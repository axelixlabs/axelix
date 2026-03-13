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

import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;

/**
 * Provider for OpenID Connect (OIDC) provider metadata discovered via the
 * OpenID Connect Discovery endpoint ({@code /.well-known/openid-configuration}).
 *
 * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html"> OpenID Connect Discovery 1.0</a>
 * @author Nikita Kirillov
 */
public class OidcMetadataProvider {

    private static final Logger log = LoggerFactory.getLogger(OidcMetadataProvider.class);

    private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";

    private final RestClient restClient;

    private final String issuerUri;

    @Nullable
    private String jwksUri;

    @Nullable
    private String tokenEndpoint;

    @Nullable
    private String authorizationEndpoint;

    public OidcMetadataProvider(RestClient restClient, String issuerUri) {
        this.restClient = restClient;
        this.issuerUri = issuerUri;
        fetchOidcMetadata();
    }

    @NonNull
    public String getJwksUri() {
        if (jwksUri == null) {
            fetchOidcMetadata();
        }
        if (jwksUri == null) {
            throw new OidcMetadataUnavailableException(issuerUri);
        }
        return jwksUri;
    }

    @NonNull
    public String getTokenEndpoint() {
        if (tokenEndpoint == null) {
            fetchOidcMetadata();
        }
        if (tokenEndpoint == null) {
            throw new OidcMetadataUnavailableException(issuerUri);
        }
        return tokenEndpoint;
    }

    /**
     * Returns the authorization endpoint URL, or {@code null} if the OIDC provider is unavailable.
     * This method does not throw an exception when the endpoint is unavailable — it is used for UI purposes only.
     */
    @Nullable
    public String getAuthorizationEndpoint() {
        if (authorizationEndpoint == null) {
            fetchOidcMetadata();
        }

        return authorizationEndpoint;
    }

    private void fetchOidcMetadata() {
        try {
            Map<String, Object> body = restClient
                    .get()
                    .uri(issuerUri + OIDC_DISCOVERY_PATH)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (body == null) {
                log.info("OIDC discovery endpoint returned empty response from {}", issuerUri);
                return;
            }

            applyMetadata(body);

        } catch (Exception e) {
            log.info("Failed to fetch OIDC metadata from {}", issuerUri, e);
        }
    }

    /**
     * Needs additional validation required fields per OpenID Connect Discovery 1.0
     * See: <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">...</a>
     */
    private void applyMetadata(Map<String, Object> body) {
        String issuer = getStringValue(body, "issuer");
        String jwksUri = getStringValue(body, "jwks_uri");
        String tokenEndpoint = getStringValue(body, "token_endpoint");
        String authorizationEndpoint = getStringValue(body, "authorization_endpoint");

        if (issuer == null || jwksUri == null || tokenEndpoint == null || authorizationEndpoint == null) {
            log.info(
                    "OIDC discovery response from {} is missing required fields: "
                            + "issuer={}, jwks_uri={}, token_endpoint={}, authorization_endpoint={}",
                    issuerUri,
                    issuer,
                    jwksUri,
                    tokenEndpoint,
                    authorizationEndpoint);
            return;
        }

        if (!issuer.equals(issuerUri)) {
            log.info("OIDC issuer mismatch: expected '{}' but got '{}'", issuerUri, issuer);
            return;
        }

        this.jwksUri = jwksUri;
        this.tokenEndpoint = tokenEndpoint;
        this.authorizationEndpoint = authorizationEndpoint;
    }

    @Nullable
    private String getStringValue(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value instanceof String ? (String) value : null;
    }
}
