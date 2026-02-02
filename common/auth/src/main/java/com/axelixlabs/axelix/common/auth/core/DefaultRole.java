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
 * Default {@link Role} backed by real {@link #authorities}.
 *
 * @see Role
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public final class DefaultRole implements Role {

    private final String name;
    private final Set<Authority> authorities;
    private final Set<Role> components;

    public DefaultRole(String name, Set<Authority> authorities, Set<Role> components) {
        this.name = name;
        this.authorities = authorities != null ? authorities : Collections.emptySet();
        this.components = components != null ? components : Collections.emptySet();
    }

    public DefaultRole(String name, Set<Authority> authorities) {
        this(name, authorities, Collections.emptySet());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Authority> getAuthorities() {
        return authorities;
    }

    @Override
    public Set<Role> getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultRole that = (DefaultRole) o;
        return Objects.equals(name, that.name)
                && Objects.equals(authorities, that.authorities)
                && Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, authorities, components);
    }

    @Override
    public String toString() {
        return "DefaultRole[" + "name=" + name + ", authorities=" + authorities + ", components=" + components + ']';
    }
}
