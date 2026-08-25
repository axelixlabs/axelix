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
package com.axelixlabs.axelix.master.utils;

import jakarta.servlet.http.HttpServletRequest;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.service.auth.intercept.mcp.OnMcpAccessDenied;
import com.axelixlabs.axelix.master.service.auth.intercept.mcp.OnMcpAuthenticationFailure;
import com.axelixlabs.axelix.master.service.auth.intercept.mcp.OnMcpSuccessfulResult;

/**
 * Test IAM Web interceptor that records the {@link McpEndpoint} each auth callback is invoked with.
 *
 * @see CapturingIamWebInterceptor
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public class CapturingIamMcpInterceptor
        implements OnMcpAccessDenied, OnMcpAuthenticationFailure, OnMcpSuccessfulResult {

    private @Nullable McpEndpoint authenticationFailureEndpoint;
    private @Nullable McpEndpoint accessDeniedEndpoint;
    private @Nullable McpEndpoint successfulEndpoint;
    private @Nullable User actor;

    @Override
    public void onAccessDenied(McpEndpoint target, HttpServletRequest request, User user) {
        this.accessDeniedEndpoint = target;
        this.actor = user;
    }

    @Override
    public void onAuthenticationFailure(McpEndpoint target, HttpServletRequest request) {
        this.authenticationFailureEndpoint = target;
    }

    @Override
    public void onSuccess(McpEndpoint target, HttpServletRequest request, User user) {
        this.successfulEndpoint = target;
        this.actor = user;
    }

    public void reset() {
        this.authenticationFailureEndpoint = null;
        this.accessDeniedEndpoint = null;
        this.successfulEndpoint = null;
        this.actor = null;
    }

    public @Nullable McpEndpoint authenticationFailureEndpoint() {
        return authenticationFailureEndpoint;
    }

    public @Nullable McpEndpoint accessDeniedEndpoint() {
        return accessDeniedEndpoint;
    }

    public @Nullable McpEndpoint successfulEndpoint() {
        return successfulEndpoint;
    }

    public @Nullable User actor() {
        return actor;
    }
}
