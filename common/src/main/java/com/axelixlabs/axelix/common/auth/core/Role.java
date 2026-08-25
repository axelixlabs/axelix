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

import java.util.HashSet;
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
     * Authorities this role grants on its own, without the ones it reaches through its {@link #getComponents()
     * components}. Meant for whoever stores or serializes the role as it stands; whoever decides what the holder of
     * the role may do wants {@link #getEffectiveAuthorities()} instead.
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

    /**
     * Everything this role grants: its own {@link #getAuthorities() authorities} united with the ones of every role
     * reachable through its {@link #getComponents() components}, however deep. An authority is not copied into the
     * deriving role, so granting one to a component shows up here at once.
     *
     * <p>No visited set is kept on the way down: a composite is assembled bottom-up out of immutable roles, so the
     * graph of objects cannot close a cycle whatever the data it was assembled from said.</p>
     *
     * @return immutable set of every {@link Authority} the role grants, directly or through a component
     */
    default Set<Authority> getEffectiveAuthorities() {
        Set<Authority> all = new HashSet<>(getAuthorities());

        for (Role component : getComponents()) {
            all.addAll(component.getEffectiveAuthorities());
        }

        return Set.copyOf(all);
    }
}
