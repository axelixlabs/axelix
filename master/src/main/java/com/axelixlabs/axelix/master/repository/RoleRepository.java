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

    /**
     * Reads the role with the given name together with the names of the authorities it grants, in a single query.
     *
     * <p>The join is a {@code LEFT} join, so a role that grants no authority still yields a single row (with a
     * {@code null} authority name). An empty result therefore means no role with such a name exists.</p>
     *
     * @param name Name of the role.
     * @return One row per granted authority, a single {@code null}-authority row if the role grants none, or an empty
     *         list if no role with such a name exists.
     */
    @Query("""
            SELECT r.name AS role_name, a.name AS authority_name
            FROM roles r
            LEFT JOIN roles_authorities ra ON ra.role_id = r.id
            LEFT JOIN authorities a ON a.id = ra.authority_id
            WHERE r.name = :name
            """)
    List<RoleWithAuthorityName> findWithAuthoritiesByName(@Param("name") String name);

    /**
     * A single (role, granted-authority-name) row of {@link #findWithAuthoritiesByName(String)}.
     *
     * @param roleName      Name of the role.
     * @param authorityName Name of one authority the role grants, or {@code null} if the role grants none.
     */
    record RoleWithAuthorityName(String roleName, @Nullable String authorityName) {}
}
