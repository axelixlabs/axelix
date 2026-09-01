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
 * Request payload to create a new managed user via the Users Management API.
 *
 * @param username   Login name of the user to create.
 * @param firstName  The user's first name.
 * @param lastName   The user's last name.
 * @param email      The user email address, which may be {@code null}.
 * @param jobTitle   The user's job title, which may be {@code null}.
 * @param organizationalUnit The user's organizational unit, which may be {@code null}.
 * @param password   Plain-text password.
 * @param roleIds    Ids of the roles to grant to the user.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public record UserCreateRequest(
        String username,
        String firstName,
        String lastName,
        @Nullable String email,
        @Nullable String jobTitle,
        @Nullable String organizationalUnit,
        String password,
        Set<String> roleIds) {

    @Override
    public String toString() {
        return "UserCreateRequest[username=[%s], firstName=[REDACTED], lastName=[REDACTED],"
                + " email=[REDACTED], jobTitle=[REDACTED], organizationalUnit=[REDACTED],"
                + " password=[REDACTED], roleIds=%s]".formatted(username, roleIds);
    }
}
