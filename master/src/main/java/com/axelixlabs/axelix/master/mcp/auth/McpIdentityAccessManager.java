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

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.WebIdentityAccessManager;

/**
 * The main entrypoint for evaluating the possibility of processing requests came from the AI Agent (both Authentication
 * and Authorization). So essentially this service is the entrypoint for IAM checks for all requests made by AI Agents to
 * Axelix Master MCP.
 *
 * @see WebIdentityAccessManager Similar abstraction but for handling web requests.
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
     * @param jsonRpcRequest      the body of the http request (in case of mcp client, this is a JSON-RPC request).
     * @param authorizationHeader the contents of the incoming http authorization header.
     *
     * @return the authenticated and authorized user.
     *
     * @throws AuthorizationException in case the user is not authorized to access the given API.
     * @throws JwtProcessingException in case the implementation is unable to verify the validity
     *                                of the token or if the token is deemed invalid.
     */
    User verifyAccess(String jsonRpcRequest, AuthorizationHeader authorizationHeader)
            throws AuthorizationException, JwtProcessingException;
}
