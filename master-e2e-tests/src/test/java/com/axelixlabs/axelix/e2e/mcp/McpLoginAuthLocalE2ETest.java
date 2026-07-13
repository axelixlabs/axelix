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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.e2e.client.AxelixMasterApiClient;
import com.axelixlabs.axelix.e2e.client.McpClient;
import com.axelixlabs.axelix.e2e.config.E2ETestConfig;

import static com.axelixlabs.axelix.e2e.utils.UserUtils.PASSWORD;
import static com.axelixlabs.axelix.e2e.utils.UserUtils.generateUniqueUsername;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * E2E tests validating local user authentication specifically through the MCP server interface.
 * Requires both the MCP server and local authentication mode to be explicitly enabled.
 *
 * @author Nikita Kirillov
 */
public class McpLoginAuthLocalE2ETest {

    private AxelixMasterApiClient client;

    @BeforeEach
    void setUp() {
        assumeTrue(E2ETestConfig.isMcpEnabled(), "MCP server is disabled in configuration, skipping test.");
        assumeTrue(
                E2ETestConfig.authModeLocalEnabled(), "Auth Local mode is disabled in configuration, skipping test.");

        client = new AxelixMasterApiClient(E2ETestConfig.masterBaseUrl());
        client.login(E2ETestConfig.superAdminUsername(), E2ETestConfig.superAdminPassword());
    }

    @Test
    void shouldLoginLocalUser() {
        // given.
        String username = generateUniqueUsername();
        client.registerLocalUser(username, null, PASSWORD, "EDITOR");

        // when.
        McpClient mcpClient = new McpClient(E2ETestConfig.masterBaseUrl(), username, PASSWORD);

        // then.
        mcpClient.initializeSession();
    }
}
