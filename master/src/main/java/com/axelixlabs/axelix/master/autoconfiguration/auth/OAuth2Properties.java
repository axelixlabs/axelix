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
package com.axelixlabs.axelix.master.autoconfiguration.auth;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * Configuration properties for OAuth2/OIDC authentication.
 *
 * @param issuerUri     the base URL of the OIDC provider. Used to discover endpoints
 *                      via {@code /.well-known/openid-configuration}
 * @param clientId      the client identifier issued during registration with the OIDC provider
 * @param clientSecret  the client secret issued during registration. Required for confidential clients
 * @param redirectUri   the URI to redirect to after successful authentication.
 *                      Must be the public URL of the Master,
 *                      e.g. {@code https://axelix.master.com/api/external/oauth2/callback}
 * @param usernameClaim the JWT claim to use as the username. If not specified,
 *                      falls back to {@code preferred_username}, then {@code name}, then {@code sub}
 *
 * @since 27.02.2026
 * @author Nikita Kirillov
 */
@ConfigurationProperties(prefix = "axelix.master.auth.oauth2")
public record OAuth2Properties(
        String issuerUri,
        String clientId,
        String clientSecret,
        String redirectUri,
        @Nullable String usernameClaim,
        String scopes) {

    private static final String PROPERTY_PREFIX = "axelix.master.auth.oauth2.";
    private static final String DEFAULT_SCOPE = "openid";

    public OAuth2Properties {
        Assert.notNull(issuerUri, "OAuth2 issuer-uri is required. Set " + PROPERTY_PREFIX + "issuer-uri");
        Assert.notNull(clientId, "OAuth2 client-id is required. Set " + PROPERTY_PREFIX + "client-id");
        Assert.notNull(clientSecret, "OAuth2 client-secret is required. Set " + PROPERTY_PREFIX + "client-secret");
        Assert.notNull(
                redirectUri,
                "OAuth2 redirect-uri is required. Set " + PROPERTY_PREFIX + "redirect-uri"
                        + ", e.g. https://axelix.master.com/api/external/oauth2/callback");

        if (scopes == null) {
            scopes = DEFAULT_SCOPE;
        }
    }
}
