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

import java.util.Arrays;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.api.external.endpoint.BeansApi;
import com.axelixlabs.axelix.master.api.external.endpoint.ConditionsApi;
import com.axelixlabs.axelix.master.api.external.endpoint.ConfigPropsApi;
import com.axelixlabs.axelix.master.api.external.endpoint.EnvironmentApi;
import com.axelixlabs.axelix.master.api.external.endpoint.ScheduledTasksApi;
import com.axelixlabs.axelix.master.api.external.endpoint.WallboardApi;
import com.axelixlabs.axelix.master.api.external.response.BeansFeedResponse;
import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse;

/**
 * Provides a collection of MCP tools for inspecting Spring Boot instances.
 *
 * @since 19.02.2026
 * @author Nikita Kirillov
 */
@SuppressWarnings("NullAway")
@Service
public class McpServerTools {

    private final BeansApi beansApi;

    private final EnvironmentApi environmentApi;

    private final ConfigPropsApi configPropsApi;

    private final ConditionsApi conditionsApi;

    private final ScheduledTasksApi scheduledTasksApi;

    private final WallboardApi wallboardApi;

    public McpServerTools(
            BeansApi beansApi,
            EnvironmentApi environmentApi,
            ConfigPropsApi configPropsApi,
            ConditionsApi conditionsApi,
            ScheduledTasksApi scheduledTasksApi,
            WallboardApi wallboardApi) {
        this.beansApi = beansApi;
        this.environmentApi = environmentApi;
        this.configPropsApi = configPropsApi;
        this.conditionsApi = conditionsApi;
        this.scheduledTasksApi = scheduledTasksApi;
        this.wallboardApi = wallboardApi;
    }

    @McpTool(
            description =
                    """
        Get all Spring beans information for a specific instance.
        Returns details about bean names, types, and dependencies.
        Use this when the user asks about application context, specific beans,
        dependencies, or services of an instance.
        """)
    public BeansFeedResponse getInstanceBeans(@McpToolParam(description = "The instance ID") String instanceId) {
        return beansApi.getBeansProfile(instanceId);
    }

    @McpTool(
            description =
                    """
    Get all environment properties for a specific instance.
    Returns application properties, system properties and environment variables.
    Use this when user asks about configuration, properties or environment of an instance.
    """)
    public String getInstanceEnvironment(@McpToolParam(description = "The instance ID") String instanceId) {
        return String.valueOf(environmentApi.getAllEnvironmentProperties(instanceId));
    }

    @McpTool(
            description =
                    """
    Get all configuration properties for a specific instance.
    Returns @ConfigurationProperties beans with their values.
    Use this when user asks about configuration properties or settings of an instance.
    """)
    public String getInstanceConfigProps(@McpToolParam(description = "The instance ID") String instanceId) {
        return String.valueOf(configPropsApi.getConfigpropsFeed(instanceId));
    }

    @McpTool(
            description =
                    """
    Get conditions evaluation report for a specific instance.
    Returns which auto-configurations were applied or skipped and why.
    Use this when user asks about auto-configuration, conditions or why a bean is missing.
    """)
    public String getInstanceConditions(@McpToolParam(description = "The instance ID") String instanceId) {
        return String.valueOf(conditionsApi.getConditionsFeed(instanceId));
    }

    @McpTool(
            description =
                    """
        Get all scheduled tasks for a specific instance registered in master.
        Returns cron tasks, fixed-rate tasks, fixed-delay tasks and custom tasks.
        Use this when user asks about scheduled or cron tasks of an instance.
        """)
    public String getInstanceScheduledTasks(@McpToolParam(description = "The instance ID") String instanceId) {

        return Arrays.toString(
                scheduledTasksApi.getAllScheduledTasks(instanceId).getBody());
    }

    @McpTool(
            description =
                    """
        Fetch the comprehensive snapshot of all managed instances (also known as 'Wallboard', 'Grid', 'Instances List').

        Use this tool as a STARTING POINT to:
        1. Find a mapping between human-readable service names and their technical 'instanceId'.
        2. Get a 'Grid' view of the system to check real-time statuses (UP, DOWN, RELOAD).
        3. Identify technical metadata: service versions, Spring Boot and Java versions.
        4. Check deployment duration ('deployedFor') and Git commit SHAs.

        NOTE: This is a dynamic 'Wallboard' state. If a user asks about 'the grid', 'active services',
        or 'current instances', call this tool.
        If you suspect an ID is stale or a service just restarted, refresh by calling this again.
        """)
    public InstancesGridResponse getWallboard() {
        return wallboardApi.getInstancesGrid();
    }
}
