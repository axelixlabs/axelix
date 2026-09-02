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
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.exception.auth.OAuth2AuthenticationException;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcSubjectHash;
import com.axelixlabs.axelix.master.service.auth.oauth.UserInfoJsonAccessor;
import com.axelixlabs.axelix.master.service.state.auth.UserService;

/**
 * {@link McpAuthenticationHandler} that is capable to authenticate {@link AuthenticationSchemes#BEARER Bearer auth} requests.
 *
 * @author Mikhail Polivakha
 */
public class BearerMcpAuthenticationHandler implements McpAuthenticationHandler {

    private final OidcClient oidcClient;
    private final UserInfoJsonAccessor userInfoJsonAccessor;
    private final UserService userService;
    private final OAuth2Properties oAuth2Properties;

    public BearerMcpAuthenticationHandler(
            OidcClient oidcClient,
            UserInfoJsonAccessor userInfoJsonAccessor,
            UserService userService,
            OAuth2Properties oAuth2Properties) {
        this.oidcClient = oidcClient;
        this.userInfoJsonAccessor = userInfoJsonAccessor;
        this.userService = userService;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public User handleAuthentication(String credential) throws AuthenticationException {
        // credential is expected to be an access token
        try {
            String userInfoJson = oidcClient.validateAccessTokenAndExtractUserInfo(credential);
            Role role = userInfoJsonAccessor.extractRole(userInfoJson);

            // Correlate the agent's identity with the same account the user logged in with via the web UI.
            // Both paths derive the identical key OidcSubjectHash.of(issuerUri, sub) - here 'sub' comes from the
            // userinfo response - so we resolve the very same persisted user and reuse its stable id and username.
            String subject = userInfoJsonAccessor.extractSubject(userInfoJson);
            if (subject == null) {
                throw new AuthenticationException("The 'sub' claim is missing in the userinfo response from the OIDC "
                        + "provider, so the user behind the AI agent cannot be identified");
            }

            String oidcSubject = OidcSubjectHash.of(oAuth2Properties.issuerUri(), subject);

            UserEntity user = userService
                    .findByOidcSubject(oidcSubject)
                    .orElseThrow(
                            () -> new AuthenticationException("The user behind the AI agent has no Axelix account; "
                                    + "sign in via the web UI once before using the MCP server"));

            return new PasswordlessUser(user.id(), user.username(), Set.of(role));
        } catch (OAuth2AuthenticationException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public AuthenticationScheme supportedAuthScheme() {
        return AuthenticationSchemes.BEARER;
    }
}
