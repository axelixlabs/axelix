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

import com.axelixlabs.axelix.common.utils.Lazy;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;

/**
 * Provider for OpenID Connect (OIDC) metadata discovered via the
 * OpenID Connect Discovery endpoint ({@code /.well-known/openid-configuration}).
 *
 * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html"> OpenID Connect Discovery 1.0</a>
 * @author Nikita Kirillov
 */
public class OidcMetadataProvider {

    private static final Logger log = LoggerFactory.getLogger(OidcMetadataProvider.class);

    private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";

    private Lazy<OidcMetadata> lazyOidcMetadata;

    private final RestClient restClient;

    private final String issuerUri;

    public OidcMetadataProvider(RestClient restClient, String issuerUri) {
        this.restClient = restClient;
        this.issuerUri = issuerUri;
        this.lazyOidcMetadata = Lazy.of(this::fetchOidcMetadata);
    }

    @NonNull
    public String getJwksUri() {
        OidcMetadata metadata = resolveOidcMetadata();
        if (metadata == null) {
            throw new OidcMetadataUnavailableException(issuerUri);
        }
        return metadata.jwksUri();
    }

    @NonNull
    public String getTokenEndpoint() {
        OidcMetadata metadata = resolveOidcMetadata();
        if (metadata == null) {
            throw new OidcMetadataUnavailableException(issuerUri);
        }
        return metadata.tokenEndpoint();
    }

    /**
     * Returns the authorization endpoint URL, or {@code null} if the OIDC provider is unavailable.
     * This method does not throw an exception when the endpoint is unavailable — it is used for UI purposes only.
     */
    @Nullable
    public String getAuthorizationEndpoint() {
        OidcMetadata metadata = resolveOidcMetadata();
        return metadata != null ? metadata.authorizationEndpoint() : null;
    }

    /**
     * Resolves the OIDC provider metadata, allowing recovery if the provider was previously unavailable.
     * If the previous resolution attempt failed (returned {@code null}), a new attempt will be made on the next call.
     *
     * @return the resolved {@link OidcMetadata}, or {@code null} if the provider is unavailable
     */
    @Nullable
    private OidcMetadata resolveOidcMetadata() {
        OidcMetadata metadata = lazyOidcMetadata.get();
        if (metadata == null) {
            lazyOidcMetadata = Lazy.of(this::fetchOidcMetadata);
        }
        return metadata;
    }

    @Nullable
    private OidcMetadata fetchOidcMetadata() {
        try {
            Map<String, Object> body = restClient
                    .get()
                    .uri(issuerUri + OIDC_DISCOVERY_PATH)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (body == null) {
                log.error("OIDC discovery endpoint returned empty response from {}", issuerUri);
                return null;
            }

            return applyMetadata(body);

        } catch (Exception e) {
            log.error("Failed to fetch OIDC metadata from {}", issuerUri, e);
        }

        return null;
    }

    /**
     * Needs additional validation required fields per OpenID Connect Discovery 1.0
     * See: <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenID Provider Metadata</a>
     */
    @Nullable
    private OidcMetadata applyMetadata(Map<String, Object> body) {
        String issuer = getStringValue(body, "issuer");
        String jwksUri = getStringValue(body, "jwks_uri");
        String tokenEndpoint = getStringValue(body, "token_endpoint");
        String authorizationEndpoint = getStringValue(body, "authorization_endpoint");

        if (issuer == null || jwksUri == null || tokenEndpoint == null || authorizationEndpoint == null) {
            log.error(
                    "OIDC discovery response from {} is missing required fields: "
                            + "issuer={}, jwks_uri={}, token_endpoint={}, authorization_endpoint={}",
                    issuerUri,
                    issuer,
                    jwksUri,
                    tokenEndpoint,
                    authorizationEndpoint);
            return null;
        }

        if (!issuer.equals(issuerUri)) {
            log.error("OIDC issuer mismatch: expected '{}' but got '{}'", issuerUri, issuer);
            return null;
        }

        return new OidcMetadata(jwksUri, tokenEndpoint, authorizationEndpoint);
    }

    @Nullable
    private String getStringValue(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value instanceof String ? (String) value : null;
    }

    /**
     * DTO holds the resolved endpoints from the OIDC provider's discovery document.
     *
     * @param jwksUri               URL of the OIDC provider's JWK Set, used to fetch public keys for token verification
     * @param tokenEndpoint         URL of the token endpoint, used to exchange the authorization code for an ID Token
     * @param authorizationEndpoint URL of the authorization endpoint, used to initiate the OAuth2 login flow.
     *                              May be {@code null} if the provider is unavailable
     */
    public record OidcMetadata(
            String jwksUri, String tokenEndpoint, @Nullable String authorizationEndpoint) {}
}
