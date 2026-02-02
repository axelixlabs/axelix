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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of the {@link User} interface.
 * Represents a user with a username and a set of roles.
 *
 * @since 16.07.2025
 * @author Nikita Kirillov
 */
public final class DefaultUser implements User {

    private final String username;
    private final String password;
    private final Set<Role> roles;

    public DefaultUser(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles != null ? roles : Collections.emptySet();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
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
        DefaultUser that = (DefaultUser) o;
        return Objects.equals(username, that.username)
                && Objects.equals(password, that.password)
                && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, roles);
    }

    @Override
    public String toString() {
        return "DefaultUser[" + "username=" + username + ", password=" + password + ", roles=" + roles + ']';
    }
}
