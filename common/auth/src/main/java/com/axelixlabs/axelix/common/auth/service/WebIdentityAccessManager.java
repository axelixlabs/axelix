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
package com.axelixlabs.axelix.common.auth.service;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * The main entrypoint for evaluating the possibility of processing the HTTP request (both Authentication
 * and Authorization). So essentially this service is the entrypoint for IAM checks for every HTTP request,
 * presumably made from the browser.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public interface WebIdentityAccessManager {

    /**
     * Main entrypoint for IAM. In case any problem is encountered, then the corresponding exception is thrown.
     * In case access is granted, the method returns the user identified by the bearer access token has been granted access.
     *
     * @param relativeRequestPath the relative path of the request with prefix like (/api/external or /actuator) being
     *                            already stripped. e.g. it is expected to be {@code /beans/feed} or {@code /axelix-beans}.
     * @param requestHttpMethod   the HTTP method of the request.
     * @param token               the Bearer access token of the user.
     *
     * @return the user that was granted access.
     *
     * @throws AuthorizationException in case the user is not authorized to access the given API.
     * @throws JwtProcessingException in case the implementation is unable to verify the validity
     *                                of the token or if the token is deemed invalid.
     */
    User verifyAccess(String relativeRequestPath, HttpMethod requestHttpMethod, @Nullable String token)
            throws AuthorizationException, JwtProcessingException;
}
