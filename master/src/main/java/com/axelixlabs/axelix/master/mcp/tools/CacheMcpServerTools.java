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
import java.util.Map;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * MCP Tools for working with caches.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Service
public class CacheMcpServerTools {

    private final EndpointInvoker endpointInvoker;

    public CacheMcpServerTools(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @McpTool(
            name = McpEndpoints.CACHES_FEED_TOOL_NAME,
            title = "Caches Feed",
            description = """
            Get the full list of configured caches for a specific instance.
            Returns a list of cache managers and the caches that belong to a specific cache manager.

            Use this tool as a STARTING POINT when mentioning caches, so that:
            1. Find mappings of the target to caches and cache manager.
            2. Find mappings of the cache name to the cache manager. If the cache name exists in multiple
               cache managers, it is important to provide the user with the ability to select the required
               cache manager where the necessary cache is present, or the option where the cache is present
               in all cache managers.
            3. Check the status of the requested cache ("enabled": true/false), which indicates whether the
               cache is enabled and performing its necessary work, or is disabled, which effectively means
               the cache always produces a miss.

            NOTE: If the user asks to show caches, cache managers, enabled caches, or disabled caches, call this tool.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title =
                                    "Provides the list of all caches (along with Cache Managers) inside this Spring Boot app",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getAllCaches(@McpToolParam(description = "The instance ID") String instanceId) {
        return new String(
                endpointInvoker.invoke(
                        InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_CACHES, NoHttpPayload.INSTANCE),
                StandardCharsets.UTF_8);
    }

    @McpTool(
            name = McpEndpoints.CLEAR_ALL_CACHES_TOOL_NAME,
            title = "Clear All Cache",
            description = """
            Clears ALL caches across ALL cache managers for a specific instance.

            Use this ONLY when the user explicitly asks to clear ALL caches in the application.
            DO NOT use this tool if the user specifies a particular cache manager or a particular cache name
            — use the more specific tools instead.

            This is a broad destructive operation. If there is any ambiguity in the user's request, confirm
            with the user before proceeding.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Clears all caches inside the Spring Boot app",
                            readOnlyHint = false,
                            destructiveHint = true,
                            idempotentHint = true,
                            openWorldHint = false))
    public void clearAllCaches(@McpToolParam(description = "The instance ID") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId), ActuatorEndpoints.CLEAR_ALL_CACHES, NoHttpPayload.INSTANCE);
    }

    @McpTool(
            name = McpEndpoints.CLEAR_SPECIFIC_CACHE_TOOL_NAME,
            title = "Clear Specific Cache",
            description = """
            Clears a specific cache entry identified by cache name (key) within a specific cache manager
            for a particular instance. Only the entry matching the given cache name is evicted, all other
            entries remain intact.

            Use this when the user asks to clear a specific cache by name, or to evict a specific entry by key
            the cache name IS the key).

            PREREQUISITE: If the cache manager name is unknown, call "Caches Feed" first.
            IF THE REQUESTED CACHE NAME IS PRESENT IN MULTIPLE CACHE MANAGERS, YOU MUST ASK THE USER
            TO SELECT THE SPECIFIC CACHE MANAGER BEFORE PROCEEDING.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Clears a specific cache inside a given Cache Manager",
                            readOnlyHint = false,
                            destructiveHint = true,
                            idempotentHint = true,
                            openWorldHint = false))
    public void clearSpecificCacheEntity(
            @McpToolParam(description = "The instance ID") String instanceId,
            @McpToolParam(description = "The Cache Manager Name") String cacheManager,
            @McpToolParam(description = "The Cache Name (also serves as the entry key)") String cacheName) {

        HttpPayload payload = new DefaultHttpPayload(Map.of("cacheName", cacheName, "cacheManagerName", cacheManager));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.CLEAR_SINGLE_CACHE, payload);
    }
}
