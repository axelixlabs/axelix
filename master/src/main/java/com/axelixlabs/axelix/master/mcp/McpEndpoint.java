package com.axelixlabs.axelix.master.mcp;

import org.springaicommunity.mcp.annotation.McpTool;

/**
 * An abstraction that represent the particular MCP Endpoint inside the Axelix Master MCP server.
 *
 * @author Mikhail Polivakha
 */
public interface McpEndpoint {

    /**
     * The name of this MCP Endpoint. The "name" in this context actually means the technical identifier
     * that is returned to the AI Agent. See {@link McpTool#name()} for more information.
     */
    String name();

}
