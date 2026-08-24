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
package com.axelixlabs.axelix.master.service.auth.intercept.mcp;

import jakarta.servlet.http.HttpServletRequest;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebAuthenticationFailure;

/**
 * {@link OnMcpIamEventInterceptor} to be called when authentication failure has occurred.
 *
 * @see OnWebAuthenticationFailure
 *
 * @author Mikhail Polivakha
 */
public interface OnMcpAuthenticationFailure extends OnMcpIamEventInterceptor {

    /**
     * Actual callback.
     *
     * @param target the mcp endpoint that was attempted to be accessed/executed.
     *               it might be {@code null} in case unauthenticated access was
     *               attempted at MCP protocol-related endpoint.
     * @param request the overall http request as provided by the servlet container
     */
    void onAuthenticationFailure(@Nullable McpEndpoint target, HttpServletRequest request);
}
