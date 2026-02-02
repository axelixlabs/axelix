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

/**
 * The user that gets decoded from JWT token.
 *
 * @author Mikhail Polivakha
 */
public final class DecodedUser {

    private final String username;
    private final Set<Role> roles;

    public DecodedUser(String username, Set<Role> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
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
        DecodedUser that = (DecodedUser) o;
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
