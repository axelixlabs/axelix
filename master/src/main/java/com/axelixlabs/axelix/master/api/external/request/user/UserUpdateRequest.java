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
 * @param id       Unique identifier of the user to update.
 * @param username New username. Must be unique when provided.
 * @param email    New email address. Must be unique when provided.
 * @param roles    New set of role names.
 * @param password Plain-text new password. Hashed server-side before persistence.
 *
 * @author Sergey Cherkasov
 */
public record UserUpdateRequest(
        String id,
        String username,
        @Nullable String email,
        Set<String> roles,
        @Nullable String password) {

    @Override
    public String toString() {
        return "UserUpdateRequest[id=" + id + ", username=[REDACTED], email=[REDACTED], roles=" + roles
                + ", password=[REDACTED]" + ']';
    }
}
