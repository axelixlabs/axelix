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

import java.util.List;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.api.external.response.McpToolFeedResponse;

/**
 * Default implementation of {@link McpToolsService}.
 *
 * @author Sergey Cherksov
 */
@Service
public class DefaultMcpToolsService implements McpToolsService {

    private final McpSyncServer mcpSyncServer;

    public DefaultMcpToolsService(@Lazy McpSyncServer mcpSyncServer) {
        this.mcpSyncServer = mcpSyncServer;
    }

    public McpToolFeedResponse getMcpToolsFeed() {

        List<McpToolFeedResponse.Tool> mcpTools =
                mcpSyncServer.listTools().stream().map(this::createTool).toList();

        return new McpToolFeedResponse(mcpTools);
    }

    public McpToolFeedResponse.Tool createTool(McpSchema.Tool mcpTool) {

        return new McpToolFeedResponse.Tool(
                mcpTool.title(),
                mcpTool.description(),
                createAnnotations(mcpTool.annotations()),
                McpToolFeedResponse.ToolStatus.UP);
    }

    public McpToolFeedResponse.ToolAnnotations createAnnotations(McpSchema.ToolAnnotations annotations) {
        return new McpToolFeedResponse.ToolAnnotations(
                annotations.readOnlyHint(),
                annotations.destructiveHint(),
                annotations.idempotentHint(),
                annotations.openWorldHint());
    }
}
