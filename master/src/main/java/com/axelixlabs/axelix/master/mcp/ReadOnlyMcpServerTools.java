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
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpTool.McpAnnotations;
import org.springaicommunity.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * Provides a collection of MCP tools for inspecting Spring Boot instances.
 *
 * @since 19.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway")
@Service
public class ReadOnlyMcpServerTools {

    private final EndpointInvoker endpointInvoker;
    private final ObjectMapper objectMapper;
    private final InstanceRegistry instanceRegistry;

    public ReadOnlyMcpServerTools(
            ObjectMapper objectMapper, InstanceRegistry instanceRegistry, EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
        this.objectMapper = objectMapper;
        this.instanceRegistry = instanceRegistry;
    }

    @McpTool(
            description = """
            Get all Spring beans information for a specific instance.
            Returns details about bean names, types, and dependencies.
            Use this when the user asks about application context, specific beans,
            dependencies, or services of an instance.
            """,
            annotations =
                    @McpAnnotations(
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceBeans(@McpToolParam(description = "The instance ID") String instanceId) {
        byte[] body =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_BEANS, NoHttpPayload.INSTANCE);
        return new String(Objects.requireNonNull(body), StandardCharsets.UTF_8);
    }

    @McpTool(
            description = """
            Get all environment properties for a specific instance.
            Returns application properties, system properties and environment variables.
            Use this when user asks about configuration, properties or environment of an instance.
        """,
            annotations =
                    @McpAnnotations(
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceEnvironment(@McpToolParam(description = "The instance ID") String instanceId) {
        return String.valueOf(endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_ENV_PROPERTIES, NoHttpPayload.INSTANCE));
    }

    @McpTool(
            description = """
            Get all configuration properties for a specific instance.
            Returns @ConfigurationProperties beans with their values.
            Use this when user asks about configuration properties or settings of an instance.
        """,
            annotations =
                    @McpAnnotations(
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceConfigProps(@McpToolParam(description = "The instance ID") String instanceId) {
        return new String(
                endpointInvoker.invoke(
                        InstanceId.of(instanceId), ActuatorEndpoints.GET_CONFIG_PROPS, NoHttpPayload.INSTANCE),
                StandardCharsets.UTF_8);
    }

    @McpTool(
            description = """
            Get @Conditional conditions evaluation report for a specific instance.
            This endpoint returns which Spring Boot and custom auto-configurations were applied or skipped with explanation why.
            Use this when user asks about auto-configuration, conditions or why a bean is either missing and user expects it to
            be there, or the bean is present, but the user expects this bean to not be bootstrapped.
        """,
            annotations =
                    @McpAnnotations(
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getInstanceConditions(@McpToolParam(description = "The instance ID") String instanceId) {
        return new String(
                endpointInvoker.invoke(
                        InstanceId.of(instanceId), ActuatorEndpoints.GET_CONDITIONS, NoHttpPayload.INSTANCE),
                StandardCharsets.UTF_8);
    }

    @McpTool(
            description = """
            Get all scheduled tasks (i.e. typically created via @Scheduled) for a specific instance.
            Returns cron tasks, fixed-rate tasks, fixed-delay tasks and custom tasks.
            Use this when user asks about scheduled or cron tasks of an instance.
        """,
            annotations =
                    @McpAnnotations(
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

    @McpTool(
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
            """) String query)
            throws JsonProcessingException {
        Set<Instance> instancesFeed = getInstancesFeed(query);
        return objectMapper.writeValueAsString(instancesFeed);
    }

    private Set<Instance> getInstancesFeed(String query) {
        if (StringUtils.hasText(query)) {
            return instanceRegistry.findByQuery(query);
        } else {
            return instanceRegistry.getAll();
        }
    }
}
