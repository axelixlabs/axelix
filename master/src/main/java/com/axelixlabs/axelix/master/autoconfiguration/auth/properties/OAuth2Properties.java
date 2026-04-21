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
package com.axelixlabs.axelix.master.autoconfiguration.auth.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.autoconfiguration.web.WebAutoConfiguration;

import static com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration.OAUTH_PROPERTIES_PREFIX;

/**
 * Configuration properties for OAuth2/OIDC authentication.
 *
 * @param issuerUri         the base URL of the OIDC provider. Used to discover endpoints
 *                          via {@code /.well-known/openid-configuration}
 *
 * @param clientId          the client identifier issued during registration with the OIDC provider
 *
 * @param clientSecret      the client secret issued during registration.
 *
 * @param baseUrl           the base URL of this application.
 *
 * @param usernameClaim     the JWT claim to use as the username. If not specified,
 *                          falls back to {@code preferred_username}, then {@code name}, then {@code sub}.
 *
 * @param scopes            OAuth2 scopes requested during authorization code flow.
 *
 * @param roleAttributePath JMESPath expression evaluated against the ID token claims first,
 *                          and then against the userinfo endpoint response if needed. The expression
 *                          should resolve to an Axelix role name like {@code admin} or {@code editor}
 *
 * @since 27.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@ConfigurationProperties(prefix = OAUTH_PROPERTIES_PREFIX)
public record OAuth2Properties(
        String issuerUri,
        String clientId,
        String clientSecret,
        String baseUrl,
        @Nullable String usernameClaim,
        String scopes,
        @Nullable String roleAttributePath) {

    private static final String DEFAULT_SCOPE = "openid";

    public OAuth2Properties {
        Assert.notNull(issuerUri, "OAuth2 issuer-uri is required. Set " + OAUTH_PROPERTIES_PREFIX + ".issuer-uri");
        Assert.notNull(clientId, "OAuth2 client-id is required. Set " + OAUTH_PROPERTIES_PREFIX + ".client-id");
        Assert.notNull(
                clientSecret, "OAuth2 client-secret is required. Set " + OAUTH_PROPERTIES_PREFIX + ".client-secret");
        Assert.notNull(baseUrl, "OAuth2 base-url is required. Set " + OAUTH_PROPERTIES_PREFIX + ".base-url");

        if (scopes == null) {
            scopes = DEFAULT_SCOPE;
        }
    }

    public String redirectUri() {
        return baseUrl + WebAutoConfiguration.EXTERNAL_API_PATH + ApiPaths.OAuth2Api.CALLBACK;
    }
}
