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

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;

/**
 * The main entrypoint for evaluating the possibility of processing requests that come from the AI Agent (both Authentication
 * and Authorization). So essentially this service is the entrypoint for IAM checks for all requests made by AI Agents to
 * Axelix Master MCP.
 *
 * @author Mikhail Polivakha
 */
public interface McpIdentityAccessManager {

    /**
     * Main entrypoint for MCP requests IAM. In case any problem is encountered, then the corresponding exception is thrown.
     * In case access is granted, the method returns the user identified by the bearer access token has been granted access.
     * <p>
     * Please note that the user that is returned by this call, is the user that is using the AI Agent, i.e. it is not the user
     * that represents the AI Agent itself.
     *
     * @param mcpEndpoint         the {@link McpEndpoint} that the user attempts to access. It might be {@code null},
     *                            in which case it means the request was for the MCP protocol-related endpoint (like
     *                            initializeSession and so on).
     * @param authorizationHeader the contents of the incoming http authorization header.
     *
     * @return the authenticated and authorized user.
     *
     * @throws AuthorizationException in case the user is not authorized to access the given API.
     * @throws JwtProcessingException in case the implementation is unable to verify the validity
     *                                of the token or if the token is deemed invalid.
     */
    User verifyAccess(@Nullable McpEndpoint mcpEndpoint, AuthorizationHeader authorizationHeader)
            throws AuthorizationException, JwtProcessingException;
}
