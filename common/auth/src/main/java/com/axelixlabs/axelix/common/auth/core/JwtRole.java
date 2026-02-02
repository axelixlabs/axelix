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
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a simplified, serializable role model used within JWT tokens.
 * <p>
 * This DTO is used to store role information in the form of a role name and
 * a set of authority names, making it suitable for inclusion in JWT claims.
 * </p>
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public final class JwtRole {

    private final String name;
    private final Set<String> authorities;
    private final List<JwtRole> components;

    /**
     * Creates a new JwtRole.
     *
     * @param name the name of the role (e.g., the class name of the original role object)
     * @param authorities the set of authority names assigned to the role
     * @param components the list of nested roles that are part of this role (i.e. its components)
     */
    public JwtRole(String name, Set<String> authorities, List<JwtRole> components) {
        this.name = name;
        this.authorities = authorities != null ? authorities : Collections.emptySet();
        this.components = components != null ? components : Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public List<JwtRole> getComponents() {
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
        JwtRole jwtRole = (JwtRole) o;
        return Objects.equals(name, jwtRole.name)
                && Objects.equals(authorities, jwtRole.authorities)
                && Objects.equals(components, jwtRole.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, authorities, components);
    }

    @Override
    public String toString() {
        return "JwtRole[" + "name=" + name + ", authorities=" + authorities + ", components=" + components + ']';
    }
}
