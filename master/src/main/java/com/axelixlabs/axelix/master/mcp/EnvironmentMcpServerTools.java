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

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpTool.McpAnnotations;
import org.springaicommunity.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * MCP Tools for working with environment properties.
 *
 * @since 19.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Service
public class EnvironmentMcpServerTools {

    private final EndpointInvoker endpointInvoker;

    public EnvironmentMcpServerTools(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @McpTool(
            title = "Properties",
            description = """
            Get all environment properties for a specific instance.
            Returns application properties, system properties and environment variables.
            Use this when user asks about configuration, properties, environment or anything
            related to @Value/@ConditionalOnProperty usage within an instance.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
        """,
            annotations =
                    @McpAnnotations(
                            title = "List of all the properties inside the Spring Boot application",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceEnvironment(@McpToolParam(description = "The instance ID") String instanceId) {
        return String.valueOf(endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_ENV_PROPERTIES, NoHttpPayload.INSTANCE));
    }
}
