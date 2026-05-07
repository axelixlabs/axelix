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
package com.axelixlabs.axelix.master.mcp.tools;

import java.util.Collection;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpTool.McpAnnotations;
import org.springaicommunity.mcp.annotation.McpToolParam;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * MCP Tools for working with instance wallboard data.
 *
 * @since 19.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Service
public class WallboardMcpServerTools {

    private final ObjectMapper objectMapper;
    private final InstanceRegistry instanceRegistry;

    public WallboardMcpServerTools(ObjectMapper objectMapper, InstanceRegistry instanceRegistry) {
        this.objectMapper = objectMapper;
        this.instanceRegistry = instanceRegistry;
    }

    @McpTool(
            name = McpEndpoints.WALLBOARD_TOOL_NAME,
            title = "Instances Feed",
            description = """
            Fetch the comprehensive snapshot of all managed instances (also known as 'Wallboard', 'Grid', 'Instances List').

            Use this tool as a STARTING POINT to:
            1. Find a mapping between human-readable service names and their technical 'instanceId'.
            2. Get a 'Grid' view of the system to check real-time statuses (UP, DOWN, RELOAD).
            3. Identify technical metadata: service versions, Spring Boot and Java versions.
            4. Check deployment duration ('deployedFor') and Git commit SHAs.

            NOTE: This is a dynamic 'Wallboard' state. If a user asks about 'the grid', 'active services',
            or 'current instances', call this tool.
            If you suspect an ID is stale or a service just restarted, refresh by calling this again.
        """,
            annotations =
                    @McpAnnotations(
                            title = "List of all Spring Boot applications instances currently deployed",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getWallboard(@McpToolParam(required = false, description = """
            Query string by which to search for an instance insides the instances list.
            This query string MUST be a part of the service name, for instance if the service
            name is 'invoice-internal-process', then the query string may be 'invoice', 'InVoiCe'
            or 'invoice-internal-process'. Use this when you're confident that you know the name
            of the service. If you're not sure - just do not specify it, request the whole feed, and
            find it manually.
            """) String query) {
        Collection<Instance> instancesFeed = getInstancesFeed(query);
        return objectMapper.writeValueAsString(instancesFeed);
    }

    private Collection<Instance> getInstancesFeed(String query) {
        if (StringUtils.hasText(query)) {
            return instanceRegistry.findByQuery(query);
        } else {
            return instanceRegistry.getAll();
        }
    }
}
