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
package com.axelixlabs.axelix.master.mcp.auth.handler;

import org.springframework.http.HttpHeaders;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;

/**
 * Component that is capable to authenticate the user of the MCP Server given the provided credential.
 *
 * @author Mikhail Polivakha
 */
public interface McpAuthenticationHandler {

    /**
     * Handle the MCP Authentication.
     *
     * @param credential                The whatever value of the {@link HttpHeaders#AUTHORIZATION} header.
     *                                  The Authentication Schema is supposed to be stripped out.
     *
     * @return                          The authenticated {@link User}.
     * @throws AuthenticationException  In case of the authentication failure.
     */
    User handleAuthentication(String credential) throws AuthenticationException;

    /**
     * @return the {@link AuthenticationScheme} supported by this {@link McpAuthenticationHandler}.
     */
    AuthenticationScheme supportedAuthScheme();
}
