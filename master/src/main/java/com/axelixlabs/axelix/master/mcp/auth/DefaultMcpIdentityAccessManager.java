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
package com.axelixlabs.axelix.master.mcp.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.utils.Assert;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.mcp.auth.handler.McpAuthenticationHandler;

/**
 * Default implementation of the {@link McpIdentityAccessManager}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultMcpIdentityAccessManager implements McpIdentityAccessManager {

    private final Map<AuthenticationScheme, McpAuthenticationHandler> mcpAuthenticationHandlers;
    private final McpEndpointResolver mcpEndpointResolver;
    private final McpEndpointAuthorityResolver mcpEndpointAuthorityResolver;
    private final Authorizer authorizer;

    public DefaultMcpIdentityAccessManager(
            Authorizer authorizer,
            McpEndpointResolver mcpEndpointResolver,
            McpEndpointAuthorityResolver mcpEndpointAuthorityResolver,
            List<McpAuthenticationHandler> mcpAuthenticationHandlers) {
        this.authorizer = authorizer;
        this.mcpEndpointResolver = mcpEndpointResolver;
        this.mcpEndpointAuthorityResolver = mcpEndpointAuthorityResolver;
        this.mcpAuthenticationHandlers = mcpAuthenticationHandlers.stream()
                .collect(Collectors.toMap(McpAuthenticationHandler::supportedAuthSchema, Function.identity()));
    }

    @Override
    public void verifyAccess(String jsonRpcRequest, AuthorizationHeader authorizationHeader)
            throws AuthorizationException, JwtProcessingException {
        McpAuthenticationHandler mcpAuthenticationHandler =
                mcpAuthenticationHandlers.get(authorizationHeader.authSchema());

        Assert.notNull(
                mcpAuthenticationHandler,
                "Unable to find McpAuthenticationHandler to handle the authentication for this request, please report this to maintainers");

        @SuppressWarnings("NullAway") // null away does not recognize custom not null assertion
        User authenticatedUser = mcpAuthenticationHandler.handleAuthentication(authorizationHeader.credential());

        Optional<McpEndpoint> mcpEndpoint = mcpEndpointResolver.resolve(jsonRpcRequest);

        mcpEndpoint.ifPresent(endpoint -> authorizeAccess(endpoint, authenticatedUser));
    }

    private void authorizeAccess(McpEndpoint endpoint, User authenticatedUser) {
        Optional<Authority> resolvedAuthority = mcpEndpointAuthorityResolver.resolve(endpoint);

        resolvedAuthority.ifPresent(
                authority -> authorizer.authorize(authenticatedUser, new AuthorizationRequest(Set.of(authority))));
    }
}
