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
package com.nucleonforge.axelix.common.auth.core;

import java.util.Collections;
import java.util.Set;

/**
 * Default {@link Role} backed by real {@link #authorities}.
 *
 * @see Role
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public record DefaultRole(String name, Set<Authority> authorities, Set<Role> components) implements Role {

    public DefaultRole(String name, Set<Authority> authorities) {
        this(name, authorities, Set.of());
    }

    public DefaultRole {
        if (authorities == null) {
            authorities = Collections.emptySet();
        }
        if (components == null) {
            components = Collections.emptySet();
        }
    }
}
