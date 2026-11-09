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

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Queries roles, their authorities, and their relationships.
 *
 * @author Sergey Cherkasov
 */
@Repository
public class RoleRepository {

    private final JdbcClient jdbcClient;

    public RoleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<RoleWithAuthorityName> findAllWithAuthorities() {
        return jdbcClient
                .sql("""
            SELECT r.id AS role_id, r.name AS role_name, a.codename AS authority_name
            FROM roles r
            LEFT JOIN roles_authorities ra ON ra.role_id = r.id
            LEFT JOIN authorities a ON a.id = ra.authority_id
            """)
                .query((rs, rowNum) -> new RoleWithAuthorityName(
                        rs.getString("role_id"), rs.getString("role_name"), rs.getString("authority_name")))
                .list();
    }

    public List<RoleParentBond> findAllParentBonds() {
        return jdbcClient
                .sql("SELECT role_id AS role_id, parent_role_id AS parent_role_id FROM roles_parents")
                .query((rs, rowNum) -> new RoleParentBond(rs.getString("role_id"), rs.getString("parent_role_id")))
                .list();
    }

    public List<String> findRoleIdsOfUser(String userId) {
        return jdbcClient
                .sql("SELECT role_id FROM users_roles WHERE user_id = :userId")
                .param("userId", userId)
                .query((rs, rowNum) -> rs.getString("role_id"))
                .list();
    }

    /**
     * Returns every role together with how many users it is assigned to. A role that nobody holds is returned
     * with a members count of zero.
     */
    public List<RoleProfile> findRolesFeed() {
        return jdbcClient
                .sql("""
            SELECT r.id AS id, r.name AS name, COUNT(ur.user_id) AS members_count, r.description AS description
            FROM roles r
            LEFT JOIN users_roles ur ON ur.role_id = r.id
            GROUP BY r.id, r.name, r.description
            """)
                .query((rs, rowNum) -> new RoleProfile(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("members_count"),
                        rs.getString("description")))
                .list();
    }

    public record RoleWithAuthorityName(
            String roleId, String roleName, @Nullable String authorityName) {}

    public record RoleParentBond(String roleId, String parentRoleId) {}

    /**
     * One role as returned for the roles feed.
     *
     * @param id           Unique identifier of the role.
     * @param name         Unique name of the role.
     * @param membersCount Number of users the role is assigned to.
     * @param description  Human-readable description of what the role allows.
     */
    public record RoleProfile(String id, String name, int membersCount, String description) {}
}
