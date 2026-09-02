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
package com.axelixlabs.axelix.master.repository;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.RoleEntity;

/**
 * Repository for {@link RoleEntity} aggregate.
 *
 * @author Sergey Cherkasov
 */
public interface RoleRepository extends ListCrudRepository<RoleEntity, String> {

    @Query("""
            SELECT r.id AS role_id, r.name AS role_name, a.name AS authority_name
            FROM roles r
            LEFT JOIN roles_authorities ra ON ra.role_id = r.id
            LEFT JOIN authorities a ON a.id = ra.authority_id
            """)
    List<RoleWithAuthorityName> findAllWithAuthorities();

    @Query("SELECT role_id AS role_id, parent_role_id AS parent_role_id FROM roles_parents")
    List<RoleParentBond> findAllParentBonds();

    @Query("SELECT role_id FROM users_roles WHERE user_id = :userId")
    List<String> findRoleIdsOfUser(@Param("userId") String userId);

    @Query("SELECT id FROM roles WHERE name = :roleName")
    Optional<String> findIdByName(@Param("roleName") String roleName);

    record RoleWithAuthorityName(
            String roleId, String roleName, @Nullable String authorityName) {}

    record RoleParentBond(String roleId, String parentRoleId) {}
}
