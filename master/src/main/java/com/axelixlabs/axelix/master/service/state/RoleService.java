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
package com.axelixlabs.axelix.master.service.state;

import java.util.Optional;

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.Role;

/**
 * Service that resolves the roles Axelix Master knows about.
 *
 * <p>Roles are stored in the database: the built-in {@code ADMIN}, {@code EDITOR} and {@code VIEWER} roles are
 * inserted by a Liquibase migration, any other role is created by Axelix Enterprise. Note that the internal
 * {@code SUPER_ADMIN} and {@code MANAGED_SERVICE} roles are <strong>not</strong> resolvable through this service:
 * they are never persisted and never assigned to a managed user.</p>
 *
 * @author Sergey Cherkasov
 */
@NullMarked
public interface RoleService {

    /**
     * Looks up a role by its exact name.
     *
     * @param name Name of the role to look up.
     * @return The role, or {@link Optional#empty()} if no role with such a name exists.
     */
    Optional<Role> findByName(String name);
}
