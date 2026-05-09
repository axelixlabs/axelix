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

import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;

/**
 * Component that is capable to resolve the {@link Authority} that is required to access the {@link McpEndpoint}.
 *
 * @see AuthorityResolver
 * @author Mikhail Polivakha
 */
public interface McpEndpointAuthorityResolver {

    /**
     * Resolve the authority that is required to gain access for the provided {@link McpEndpoint}.
     *
     * @param endpoint the endpoint to resolve hte authority for.
     *
     * @return an {@link Optional} authority that is required to access the resource.
     *         Might be {@link Optional#empty()} in case no authority is required to
     *         access the resource.
     */
    Optional<Authority> resolve(McpEndpoint endpoint);
}
