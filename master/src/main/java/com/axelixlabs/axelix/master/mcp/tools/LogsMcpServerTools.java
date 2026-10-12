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
import java.util.List;
import java.util.Map;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.SingleValueQueryParameter;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * MCP Tools for working with the logs of an instance.
 *
 * @author Mikhail Polivakha
 */
@Service
public class LogsMcpServerTools {

    private final EndpointInvoker endpointInvoker;

    public LogsMcpServerTools(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @McpTool(
            name = McpEndpoints.LOG_ENTRIES_TAIL_TOOL_NAME,
            title = "Logs",
            description = """
            Get the most recent log entries of a specific instance, in chronological order (the last
            entry is the most recent one). Each entry has the timestamp (milliseconds since the Unix epoch),
            level, logger name, thread name, message and, if an exception was logged, its stack trace.
            Use this when user asks about the logs, errors, exceptions or the recent activity of an instance.

            The instance keeps only a bounded amount of the most recent entries in memory (since its startup),
            so fewer entries than requested may be returned.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
        """,
            annotations =
                    @McpAnnotations(
                            title = "The most recent log entries of this Spring Boot app",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceLogs(
            @McpToolParam(description = "The instance ID") String instanceId,
            @McpToolParam(description = "The maximum amount of the most recent log entries to return") int limit) {
        return new String(
                endpointInvoker.invoke(
                        InstanceId.of(instanceId),
                        ActuatorEndpoints.GET_LOG_ENTRIES_TAIL,
                        new DefaultHttpPayload(
                                List.of(new SingleValueQueryParameter("limit", String.valueOf(limit))), Map.of())),
                StandardCharsets.UTF_8);
    }
}
