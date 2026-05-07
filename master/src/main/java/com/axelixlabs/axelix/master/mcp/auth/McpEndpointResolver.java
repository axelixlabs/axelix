package com.axelixlabs.axelix.master.mcp.auth;

import java.util.Optional;

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
     * @return the resolved {@link McpEndpoint}, or {@link Optional#empty()} if none.
     */
    Optional<McpEndpoint> resolve(String mcpJsonRpcRequest);
}
