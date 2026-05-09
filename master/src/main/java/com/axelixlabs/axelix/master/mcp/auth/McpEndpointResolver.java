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

import java.util.Optional;

import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;

/**
 * Component that is capable to resolve the {@link McpEndpoint} by the incoming JSON-RPC request from the AI Agent.
 *
 * @author Mikhail Polivakha
 */
public interface McpEndpointResolver {

    /**
     * Resolves the JSON-RPC request to the endpoint that we're trying to access.
     *
     * @param mcpJsonRpcRequest the JSON-RPC request sent by AI Agent.
     *
     * @return the resolved {@link McpEndpoint}, or {@link Optional#empty()} if the provided json-rpc
     *         call is correct and just does not represent the "tool/call".
     *
     * @throws AuthenticationException in case it is impossible to understand what exactly resource
     *                                 the AI Agent wants to access.
     */
    Optional<McpEndpoint> resolve(String mcpJsonRpcRequest) throws AuthenticationException;
}
