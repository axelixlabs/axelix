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
import java.util.Set;

/**
 * Represents a simplified, serializable role model used within JWT tokens.
 * <p>
 * This DTO is used to store role information in the form of a role name and
 * a set of authority names, making it suitable for inclusion in JWT claims.
 * </p>
 *
 * @param name the name of the role (e.g., the class name of the original role object)
 * @param authorities the set of authority names assigned to the role
 * @param components the list of nested roles that are part of this role (i.e. its components)
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public record JwtRole(String name, Set<String> authorities, List<JwtRole> components) {

    public JwtRole {
        if (authorities == null) {
            authorities = Collections.emptySet();
        }
        if (components == null) {
            components = Collections.emptyList();
        }
    }
}
