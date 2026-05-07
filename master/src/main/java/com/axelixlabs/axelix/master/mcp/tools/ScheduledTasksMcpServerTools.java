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

import java.nio.charset.StandardCharsets;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpTool.McpAnnotations;
import org.springaicommunity.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * MCP Tools for working with scheduled tasks.
 *
 * @since 19.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Service
public class ScheduledTasksMcpServerTools {

    private final EndpointInvoker endpointInvoker;

    public ScheduledTasksMcpServerTools(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @McpTool(
            name = McpEndpoints.SCHEDULED_TASKS_FEED_TOOL_NAME,
            title = "Scheduled Tasks",
            description = """
            Get all scheduled tasks (i.e. typically created via @Scheduled) for a specific instance.
            Returns cron tasks, fixed-rate tasks, fixed-delay tasks and custom tasks.
            Use this when user asks about scheduled or cron tasks of an instance.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
        """,
            annotations =
                    @McpAnnotations(
                            title = "List of all scheduled tasks inside this Spring Boot app",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceScheduledTasks(@McpToolParam(description = "The instance ID") String instanceId) {
        return new String(
                endpointInvoker.invoke(
                        InstanceId.of(instanceId), ActuatorEndpoints.GET_SCHEDULED_TASKS, NoHttpPayload.INSTANCE),
                StandardCharsets.UTF_8);
    }
}
