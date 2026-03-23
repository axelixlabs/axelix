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
package com.axelixlabs.axelix.master.api.external.response.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.axelixlabs.axelix.common.utils.Lazy;

/**
 * Authentication settings for OAuth2/OIDC provider.
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class OidcAuthenticationOption implements AuthenticationOption {

    private final String scope;
    private final String clientId;
    private final String redirectUri;
    private final Lazy<String> authorizationEndpointResolver;

    public OidcAuthenticationOption(
            String scope, String clientId, String redirectUri, Lazy<String> authorizationEndpointResolver) {
        this.scope = scope;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.authorizationEndpointResolver = authorizationEndpointResolver;
    }

    @Override
    @JsonProperty("type")
    public String type() {
        return "oidc";
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpointResolver.require();
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getScope() {
        return scope;
    }
}
