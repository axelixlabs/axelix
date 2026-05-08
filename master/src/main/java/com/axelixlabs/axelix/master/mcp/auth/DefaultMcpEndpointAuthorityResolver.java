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
package com.axelixlabs.axelix.master.mcp.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;

/**
 * Default implementation of {@link McpEndpointAuthorityResolver} that hosts the mapping in-memory.
 *
 * @author Mikhail Polivakha
 */
public class DefaultMcpEndpointAuthorityResolver implements McpEndpointAuthorityResolver {

    private static final Map<McpEndpoint, Authority> MAPPING;

    static {
        MAPPING = new HashMap<>(2);
        MAPPING.put(McpEndpoints.CLEAR_ALL_CACHES, DefaultAuthority.CACHES_CLEAR);
        MAPPING.put(McpEndpoints.CLEAR_SPECIFIC_CACHE, DefaultAuthority.CACHES_CLEAR);
    }

    @Override
    public Optional<Authority> resolve(McpEndpoint endpoint) {
        return Optional.ofNullable(MAPPING.get(endpoint));
    }
}
