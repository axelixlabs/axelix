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
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.service.auth.oauth.OidcMetadataProvider;

/**
 * Authentication settings for OAuth2/OIDC provider.
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 */
public final class AuthSettingsOAuth2 implements AuthSettings {

    private final String scope;
    private final String clientId;
    private final String redirectUri;
    private final OidcMetadataProvider metadataProvider;

    public AuthSettingsOAuth2(
            String scope, String clientId, String redirectUri, OidcMetadataProvider metadataProvider) {
        this.scope = scope;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.metadataProvider = metadataProvider;
    }

    @Override
    @JsonProperty("type")
    public String type() {
        return "oauth2";
    }

    @Nullable
    public String getAuthorizationEndpoint() {
        return metadataProvider.getAuthorizationEndpoint();
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
