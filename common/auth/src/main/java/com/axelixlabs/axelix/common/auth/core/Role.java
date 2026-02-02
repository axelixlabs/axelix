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

import java.util.Set;

/**
 * SPI interface of a Role. A role is comprised from a set of {@link Authority authorities}.
 *
 * @see Authority
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface Role {

    /**
     * The unique name of this role.
     *
     * @return the name of the role.
     */
    String getName();

    /**
     * Authorities of a given role.
     *
     * @return immutable set of {@link Authority} objects associated with this role
     */
    Set<Authority> getAuthorities();

    /**
     * Component roles that are included in this role.
     * <p>
     * This allows defining hierarchical roles. The hierarchy must form a
     * <strong>directed acyclic graph (DAG)</strong>.
     * Implementations must ensure there are no duplicate or cyclic roles within the hierarchy.
     *
     * @return immutable set of {@link Role} objects included in this role
     */
    Set<Role> getComponents();
}
