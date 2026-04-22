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
package com.axelixlabs.axelix.master.api.external.request.user;

import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * Request payload to update an existing managed user via the Users Management API.
 *
 * @param id       Unique identifier of the user to update
 * @param username The new username must be unique
 * @param email    The user’s email address. {@code null} if no email address is associated with the account. Must be unique.
 * @param roles    Names of the roles granted to this user.
 * @param password Plain-text password. Hashed server-side before persistence.
 *
 * @author Sergey Cherkasov
 */
public record UserUpdateRequest(
        String id, String username, @Nullable String email, Set<String> roles, String password) {

    @Override
    public String toString() {
        return "UserUpdateRequest[id=" + id + ", username=" + username + ", email=" + email + ", roles=" + roles
                + ", password=[REDACTED]" + ']';
    }
}
