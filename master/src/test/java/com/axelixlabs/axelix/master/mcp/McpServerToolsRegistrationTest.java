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
package com.axelixlabs.axelix.master.mcp;

import java.util.List;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link McpServerTools}
 *
 * @since 24.02.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
class McpServerToolsRegistrationTest {

    @Autowired
    private McpSyncServer mcpSyncServer;

    @Test
    void shouldRegisterAllMcpTools() {
        List<McpSchema.Tool> registeredTools = mcpSyncServer.listTools();

        assertThat(registeredTools)
                .extracting(McpSchema.Tool::name)
                .containsExactlyInAnyOrder(
                        "getInstanceBeans",
                        "getInstanceEnvironment",
                        "getInstanceConfigProps",
                        "getInstanceConditions",
                        "getInstanceScheduledTasks",
                        "getWallboard");
    }
}
