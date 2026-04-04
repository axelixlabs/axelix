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
package com.axelixlabs.axelix.common.auth.core;

import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * The representation of the user for which we do not know his password. That may happen
 * because of different things, like when we decode user from JWT, or when we authenticate
 * the user via OIDC for example.
 *
 * @author Mikhail Polivakha
 */
public final class PasswordlessUser implements User {

    private final String username;
    private final Set<Role> roles;

    public PasswordlessUser(String username, Set<Role> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordlessUser that = (PasswordlessUser) o;
        return Objects.equals(username, that.username) && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, roles);
    }

    @Override
    public String toString() {
        return "DecodedUser[" + "username=" + username + ", roles=" + roles + ']';
    }
}
