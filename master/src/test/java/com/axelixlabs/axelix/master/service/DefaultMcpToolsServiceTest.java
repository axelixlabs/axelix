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
package com.axelixlabs.axelix.master.service;

import io.modelcontextprotocol.server.McpSyncServer;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.axelixlabs.axelix.master.api.external.response.McpToolFeedResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link McpToolsService}
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
public class DefaultMcpToolsServiceTest {

    @Autowired
    private McpToolsService mcpToolsService;

    @Autowired
    private McpSyncServer mcpSyncServer;

    @Test
    void shouldReturnMcpToolsFeed() {
        // when.
        McpToolFeedResponse mcpTools = mcpToolsService.getMcpToolsFeed();

        // then.
        assertThat(mcpTools.tools())
                .hasSizeGreaterThan(0)
                .hasSize(mcpSyncServer.listTools().size());
        assertThat(mcpTools.tools()).isNotEmpty().allSatisfy(tool -> {
            assertThat(tool.title()).isNotBlank();
            assertThat(tool.description()).isNotBlank();
            assertThat(tool.annotations().destructiveHint()).isIn(true, false);
            assertThat(tool.annotations().idempotentHint()).isIn(true, false);
            assertThat(tool.annotations().openWorldHint()).isIn(true, false);
            assertThat(tool.annotations().readOnlyHint()).isIn(true, false);
            assertThat(tool.status()).isIn(McpToolFeedResponse.ToolStatus.UP, McpToolFeedResponse.ToolStatus.DISABLE);
        });
    }
}
