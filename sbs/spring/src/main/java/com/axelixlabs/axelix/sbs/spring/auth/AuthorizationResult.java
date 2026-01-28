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
package com.axelixlabs.axelix.sbs.spring.auth;

import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;

/**
 * Result of the {@link Authorizer} analysis of the given {@link AuthorizationRequest}.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface AuthorizationResult {

    /**
     * @return whether the access is authorized
     */
    boolean accessAuthorized();
}
