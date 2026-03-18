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

import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
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
 * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect Discovery 1.0</a>
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@NullMarked
public class OidcMetadataProvider {

    private static final Logger log = LoggerFactory.getLogger(OidcMetadataProvider.class);
    private static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";

    private final Lazy<OidcMetadata> oidcMetadata;
    private final RestClient restClient;
    private final String issuerUri;

    public OidcMetadataProvider(RestClient restClient, String issuerUri) {
        this.restClient = restClient;
        this.issuerUri = issuerUri;
        this.oidcMetadata = Lazy.of(this::fetchOidcMetadata);
    }

    public String getJwksUri() {
        return oidcMetadata.require().jwksUri();
    }

    public String getTokenEndpoint() {
        return oidcMetadata.require().tokenEndpoint();
    }

    public String getAuthorizationEndpoint() {
        return oidcMetadata.require().authorizationEndpoint();
    }

    private OidcMetadata fetchOidcMetadata() {
        try {
            Map<String, Object> body = restClient
                    .get()
                    .uri(issuerUri + OIDC_DISCOVERY_PATH)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (body == null) {
                log.error("OIDC discovery endpoint returned empty response from {}", issuerUri);
                throw new OidcMetadataUnavailableException(issuerUri);
            }

            return buildMetadata(body);

        } catch (Exception e) {
            log.error("Failed to fetch OIDC metadata from {}", issuerUri, e);
            throw new OidcMetadataUnavailableException(issuerUri);
        }
    }

    /**
     * Needs additional validation required fields per OpenID Connect Discovery 1.0
     * See: <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata">OpenID Provider Metadata</a>
     */
    private OidcMetadata buildMetadata(Map<String, Object> body) {
        String issuer = getStringValue(body, "issuer");
        String jwksUri = getStringValue(body, "jwks_uri");
        String tokenEndpoint = getStringValue(body, "token_endpoint");
        String authorizationEndpoint = getStringValue(body, "authorization_endpoint");

        if (ObjectUtils.anyNull(issuer, jwksUri, tokenEndpoint, authorizationEndpoint)) {
            log.error(
                    "OIDC discovery response from {} is missing required fields: "
                            + "issuer={}, jwks_uri={}, token_endpoint={}, authorization_endpoint={}",
                    issuerUri,
                    issuer,
                    jwksUri,
                    tokenEndpoint,
                    authorizationEndpoint);
            throw new OidcMetadataUnavailableException(issuerUri);
        }

        if (!issuer.equals(issuerUri)) {
            log.error("OIDC issuer mismatch: expected '{}' but got '{}'", issuerUri, issuer);
            throw new OidcMetadataUnavailableException(issuerUri);
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
     */
    public record OidcMetadata(
            String jwksUri, String tokenEndpoint, String authorizationEndpoint) {}
}
