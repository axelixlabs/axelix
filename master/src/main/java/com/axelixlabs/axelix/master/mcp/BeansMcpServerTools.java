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

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpTool.McpAnnotations;
import org.springaicommunity.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * MCP Tools for working with Spring beans.
 *
 * @since 19.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway")
@Service
public class BeansMcpServerTools {

    private final EndpointInvoker endpointInvoker;

    public BeansMcpServerTools(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @McpTool(
            title = "Beans Feed",
            description = """
            Get all Spring beans information for a specific instance.
            Returns details about bean names, types, and dependencies.
            Use this when the user asks about application context, specific beans,
            dependencies, or services of a given instance.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpAnnotations(
                            title = "List of all the beans available in the ApplicationContext",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceBeans(@McpToolParam(description = "The instance ID") String instanceId) {
        byte[] body =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_BEANS, NoHttpPayload.INSTANCE);
        return new String(Objects.requireNonNull(body), StandardCharsets.UTF_8);
    }
}
