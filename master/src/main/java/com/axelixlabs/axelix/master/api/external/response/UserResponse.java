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
package com.axelixlabs.axelix.master.api.external.response;

import java.time.Instant;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.User;

/**
 * Public view of a managed user.
 *
 * @param id          Unique identifier of the user.
 * @param username    Login name of the user.
 * @param email       Email address of the user.
 * @param roles       The roles granted to this user.
 * @param provider    Origin of the user account (e.g. OIDC/OAuth2 provider name, or {@code LOCAL} for login/password).
 * @param lastLoginAt Timestamp of the most recent successful login. {@code null} if the user has never logged in.
 *
 * @author Sergey Cherkasov
 */
public record UserResponse(
        String id,
        String username,
        String email,
        Set<String> roles,
        String provider,
        @Nullable Instant lastLoginAt) {

    /**
     * Create a UserResponse from a domain User by copying its public properties.
     *
     * @param user the domain User whose id, username, email, roles, provider and lastLoginAt are used
     * @return a UserResponse containing the user's id, username, email, roles, provider, and lastLoginAt
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(), user.username(), user.email(), user.roles(), user.provider(), user.lastLoginAt());
    }
}
