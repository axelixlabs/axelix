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

import org.jspecify.annotations.Nullable;

/**
 * Request payload to create a new managed user via the Users Management API.
 *
 * @param username   Login name of the user to create.
 * @param email      The user email address, which may be {@code null}.
 * @param password   Plain-text password.
 * @param role       Name of the role to grant to the user.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public record UserCreateRequest(String username, @Nullable String email, String password, String role) {

    @Override
    public String toString() {
        return "UserCreateRequest[username=[%s], email=[REDACTED], password=[REDACTED], role=%s]"
                .formatted(username, role);
    }
}
