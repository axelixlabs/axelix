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
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoint;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebAccessDenied;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebAuthenticationFailure;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebSuccessfulResult;

/**
 * Test IAM Web interceptor that records the {@link MasterWebEndpoint} each auth callback is invoked with.
 *
 * @see CapturingIamMcpInterceptor
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public class CapturingIamWebInterceptor
        implements OnWebAuthenticationFailure, OnWebAccessDenied, OnWebSuccessfulResult {

    private @Nullable MasterWebEndpoint authenticationFailureEndpoint;
    private @Nullable MasterWebEndpoint accessDeniedEndpoint;
    private @Nullable MasterWebEndpoint successfulEndpoint;
    private @Nullable User actor;

    @Override
    public void onAuthenticationFailure(MasterWebEndpoint target, HttpServletRequest request) {
        this.authenticationFailureEndpoint = target;
    }

    @Override
    public void onAccessDenied(MasterWebEndpoint target, HttpServletRequest request, User user) {
        this.accessDeniedEndpoint = target;
        this.actor = user;
    }

    @Override
    public void onSuccess(MasterWebEndpoint target, HttpServletRequest request, User user) {
        this.successfulEndpoint = target;
        this.actor = user;
    }

    public void reset() {
        this.authenticationFailureEndpoint = null;
        this.accessDeniedEndpoint = null;
        this.successfulEndpoint = null;
        this.actor = null;
    }

    public @Nullable MasterWebEndpoint authenticationFailureEndpoint() {
        return authenticationFailureEndpoint;
    }

    public @Nullable MasterWebEndpoint accessDeniedEndpoint() {
        return accessDeniedEndpoint;
    }

    public @Nullable MasterWebEndpoint successfulEndpoint() {
        return successfulEndpoint;
    }

    public @Nullable User actor() {
        return actor;
    }
}
