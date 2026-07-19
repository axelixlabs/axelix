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

/**
 * Class that holds all MCP Endpoints supported by the Axelix Master.
 *
 * @author Mikhail Polivakha
 */
public final class McpEndpoints {

    private McpEndpoints() {}

    // Mcp endpoint names start
    public static final String BEANS_FEED_TOOL_NAME = "getInstanceBeans";
    public static final String ENVIRONMENT_FEED_TOOL_NAME = "getInstanceEnvironment";
    public static final String CONFIG_PROPS_FEED_TOOL_NAME = "getInstanceConfigProps";
    public static final String CONDITIONS_FEED_TOOL_NAME = "getInstanceConditions";
    public static final String SCHEDULED_TASKS_FEED_TOOL_NAME = "getInstanceScheduledTasks";
    public static final String WALLBOARD_TOOL_NAME = "getWallboard";
    public static final String CACHES_FEED_TOOL_NAME = "getAllCaches";
    public static final String CLEAR_ALL_CACHES_TOOL_NAME = "clearAllCaches";
    public static final String CLEAR_SPECIFIC_CACHE_TOOL_NAME = "clearSpecificCacheEntity";
    public static final String TRANSACTIONS_PROFILE_TOOL_NAME = "getApplicationTransactionsProfile";
    // Mcp endpoint names end

    public static final McpEndpoint BEANS_FEED = new DefaultMcpEndpoint(BEANS_FEED_TOOL_NAME);
    public static final McpEndpoint ENVIRONMENT = new DefaultMcpEndpoint(ENVIRONMENT_FEED_TOOL_NAME);
    public static final McpEndpoint CONFIG_PROPS = new DefaultMcpEndpoint(CONFIG_PROPS_FEED_TOOL_NAME);
    public static final McpEndpoint CONDITIONS = new DefaultMcpEndpoint(CONDITIONS_FEED_TOOL_NAME);
    public static final McpEndpoint SCHEDULED_TASKS = new DefaultMcpEndpoint(SCHEDULED_TASKS_FEED_TOOL_NAME);
    public static final McpEndpoint WALLBOARD = new DefaultMcpEndpoint(WALLBOARD_TOOL_NAME);
    public static final McpEndpoint ALL_CACHES = new DefaultMcpEndpoint(CACHES_FEED_TOOL_NAME);
    public static final McpEndpoint CLEAR_ALL_CACHES = new DefaultMcpEndpoint(CLEAR_ALL_CACHES_TOOL_NAME);
    public static final McpEndpoint CLEAR_SPECIFIC_CACHE = new DefaultMcpEndpoint(CLEAR_SPECIFIC_CACHE_TOOL_NAME);
    public static final McpEndpoint TRANSACTIONS_PROFILE = new DefaultMcpEndpoint(TRANSACTIONS_PROFILE_TOOL_NAME);
}
