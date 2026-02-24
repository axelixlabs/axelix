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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;

/**
 * The main entrypoint for evaluating the possibility of processing the request (both Authentication
 * and Authorization).
 *
 * @author Mikhail Polivakha
 */
public interface SecurityManager {

    boolean shouldAuthorize(String requestPath);

    default void authorize(String requestPath, @Nullable String token)
            throws AuthorizationException, JwtProcessingException {
        if (shouldAuthorize(requestPath)) {
            authorizeInternal(requestPath, token);
        }
    }

    void authorizeInternal(String requestPath, @Nullable String token)
            throws AuthorizationException, JwtProcessingException;
}
