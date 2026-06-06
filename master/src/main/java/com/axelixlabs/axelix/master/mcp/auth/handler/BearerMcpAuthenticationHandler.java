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
package com.axelixlabs.axelix.master.mcp.auth.handler;

import java.util.Set;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.exception.auth.OAuth2AuthenticationException;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcRoleExtractor;

/**
 * {@link McpAuthenticationHandler} that is capable to authenticate {@link AuthenticationSchemes#BEARER Bearer auth} requests.
 *
 * @author Mikhail Polivakha
 */
public class BearerMcpAuthenticationHandler implements McpAuthenticationHandler {

    private final OidcClient oidcClient;
    private final OidcRoleExtractor roleExtractor;

    public BearerMcpAuthenticationHandler(OidcClient oidcClient, OidcRoleExtractor roleExtractor) {
        this.oidcClient = oidcClient;
        this.roleExtractor = roleExtractor;
    }

    @Override
    public User handleAuthentication(String credential) throws AuthenticationException {
        // credential is expected to be an access token
        try {
            String userInfoJson = oidcClient.validateAccessTokenAndExtractUserInfo(credential);
            Role role = roleExtractor.extractRole(userInfoJson);
            return new PasswordlessUser("AI_AGENT", Set.of(role));
        } catch (OAuth2AuthenticationException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public AuthenticationScheme supportedAuthScheme() {
        return AuthenticationSchemes.BEARER;
    }
}
