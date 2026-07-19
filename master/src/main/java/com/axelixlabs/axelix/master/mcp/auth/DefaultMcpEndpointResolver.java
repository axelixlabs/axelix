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

import java.util.Map;
import java.util.Optional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;

/**
 * Default implementation of {@link McpEndpointResolver}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultMcpEndpointResolver implements McpEndpointResolver {

    private static final String TOOLS_CALL_METHOD = "tools/call";
    private static final String METHOD_JSON_FIELD = "method";

    private static final AuthenticationException PARSING_EXCEPTION =
            new AuthenticationException("Unable to parse the incoming JSON-RPC request from the AI Agent");

    private static final Map<String, McpEndpoint> ENDPOINT_MAPPING = Map.of(
            McpEndpoints.BEANS_FEED_TOOL_NAME, McpEndpoints.BEANS_FEED,
            McpEndpoints.ENVIRONMENT_FEED_TOOL_NAME, McpEndpoints.ENVIRONMENT,
            McpEndpoints.CONFIG_PROPS_FEED_TOOL_NAME, McpEndpoints.CONFIG_PROPS,
            McpEndpoints.CONDITIONS_FEED_TOOL_NAME, McpEndpoints.CONDITIONS,
            McpEndpoints.SCHEDULED_TASKS_FEED_TOOL_NAME, McpEndpoints.SCHEDULED_TASKS,
            McpEndpoints.WALLBOARD_TOOL_NAME, McpEndpoints.WALLBOARD,
            McpEndpoints.CACHES_FEED_TOOL_NAME, McpEndpoints.ALL_CACHES,
            McpEndpoints.CLEAR_ALL_CACHES_TOOL_NAME, McpEndpoints.CLEAR_ALL_CACHES,
            McpEndpoints.CLEAR_SPECIFIC_CACHE_TOOL_NAME, McpEndpoints.CLEAR_SPECIFIC_CACHE,
            McpEndpoints.TRANSACTIONS_PROFILE_TOOL_NAME, McpEndpoints.TRANSACTIONS_PROFILE);

    private final JsonMapper jsonMapper;

    public DefaultMcpEndpointResolver(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Optional<McpEndpoint> resolve(String mcpJsonRpcRequest) throws AuthenticationException {
        final JsonNode root;

        try {
            root = jsonMapper.readTree(mcpJsonRpcRequest);
        } catch (RuntimeException e) {
            throw PARSING_EXCEPTION;
        }

        JsonNode methodNode = root.path(METHOD_JSON_FIELD);

        if (methodNode.isMissingNode() || !methodNode.isString()) {
            throw PARSING_EXCEPTION;
        }

        String method = methodNode.asString();

        if (TOOLS_CALL_METHOD.equals(method)) {

            JsonNode endpointNameNode = root.path("params").path("name");

            if (!endpointNameNode.isString()) {
                throw PARSING_EXCEPTION;
            }

            return Optional.ofNullable(ENDPOINT_MAPPING.get(endpointNameNode.asString()));
        }

        return Optional.empty();
    }
}
