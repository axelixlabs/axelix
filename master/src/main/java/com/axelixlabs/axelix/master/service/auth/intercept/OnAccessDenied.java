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
package com.axelixlabs.axelix.master.service.auth.intercept;

import jakarta.servlet.http.HttpServletRequest;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoint;

/**
 * {@link IamEvaluationInterceptor} to be invoked when the {@link User} has no
 * permissions to access the given {@link MasterWebEndpoint}.
 *
 * @author Mikhail Polivakha
 */
public interface OnAccessDenied extends IamEvaluationInterceptor {

    /**
     * Actual callback.
     *
     * @param target the web endpoint that was attempted to be accessed/executed.
     * @param request the overall http request as provided by the servlet container
     * @param user the user (assuming the authentication has already passed at that point)
     *             that attempts to access the resource/exectue given action
     */
    void onRequest(MasterWebEndpoint target, HttpServletRequest request, User user);
}
