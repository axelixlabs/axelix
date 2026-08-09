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

    Optional<RoleEntity> findByName(@Param("name") String name);

    /**
     * Reads the names of the authorities granted to the role with the given name.
     *
     * @param name Name of the role.
     * @return Names of the granted authorities, empty if the role grants none or does not exist.
     */
    @Query("""
            SELECT a.name
            FROM roles r
            JOIN roles_authorities ra ON ra.role_id = r.id
            JOIN authorities a ON a.id = ra.authority_id
            WHERE r.name = :name
            """)
    List<String> findAuthorityNamesByRoleName(@Param("name") String name);
}
