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
package com.axelixlabs.axelix.e2e.mcp;

import java.util.List;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.e2e.client.McpClient;
import com.axelixlabs.axelix.e2e.config.E2ETestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * End-to-end tests for the Axelix MCP-server endpoints and capabilities.
 * <p>
 * All test cases execute under the <b>super-admin authorization</b> context to bypass
 * deployment-specific authentication providers (such as Local or OAuth2 auth).
 *
 * @author Nikita Kirillov
 */
class McpToolsE2ETest {

    private McpClient mcpClient;

    @BeforeEach
    void setUp() {
        assumeTrue(E2ETestConfig.isMcpEnabled(), "MCP server is disabled in configuration, skipping test.");

        mcpClient = new McpClient(
                E2ETestConfig.masterBaseUrl(), E2ETestConfig.superAdminUsername(), E2ETestConfig.superAdminPassword());
    }

    /**
     * Exercises the MCP endpoint ({@code /api/mcp}) via its JSON-RPC protocol:
     * <ul>
     *   <li>The {@code initialize} handshake.</li>
     *   <li>The {@code tools/list} call.</li>
     * </ul>
     */
    @Test
    void shouldReturnListTool() {
        String sessionId = mcpClient.initializeSession();

        JsonPath toolsResponse = mcpClient.listTools(sessionId);
        List<String> toolNames = toolsResponse.getList("result.tools.name", String.class);

        assertThat(toolNames)
                .containsExactlyInAnyOrder(
                        "getInstanceBeans",
                        "getInstanceEnvironment",
                        "getInstanceConfigProps",
                        "getInstanceConditions",
                        "getInstanceScheduledTasks",
                        "getWallboard",
                        "getAllCaches",
                        "clearAllCaches",
                        "clearSpecificCacheEntity",
                        "getAvailableLoggingLevels",
                        "getLoggerGroupsFeed",
                        "findLoggersByName",
                        "findGroupsByName",
                        "changeLoggingLevelByLoggerName",
                        "changeLoggingLevelByGroupName",
                        "resetLoggingLevelByLoggerName");
    }
}
