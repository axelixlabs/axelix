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
package com.nucleonforge.axelix.master.service.auth.jwt;

/**
 * Configuration properties for database table names used by the authentication module.
 *
 * <p>By default, table names have the "axelix_" prefix.</p>
 *
 * <p>These defaults can be overridden via external configuration properties
 * (e.g., in application.yml) to customize the actual table names used in the database.</p>
 *
 * @since 17.07.2025
 * @author Nikita Kirillov
 */
public class JdbcAuthConfig {

    /**
     * Table name where users are stored.
     */
    private String userTable = "axelix_user_table";

    /**
     * Table name where roles are defined.
     */
    private String roleTable = "axelix_role_table";

    /**
     * Table name for storing authorities.
     */
    private String authorityTable = "axelix_authority_table";

    /**
     * Join table name linking users to their assigned roles.
     */
    private String userRoleTable = "axelix_user_role_table";

    /**
     * Join table name linking roles to their assigned authorities.
     */
    private String roleAuthorityTable = "axelix_role_authority_table";

    /**
     * Join table name for defining role hierarchies.
     */
    private String roleComponentsTable = "axelix_role_components_table";

    public String getUserTable() {
        return userTable;
    }

    public void setUserTable(String userTable) {
        this.userTable = userTable;
    }

    public String getRoleTable() {
        return roleTable;
    }

    public void setRoleTable(String roleTable) {
        this.roleTable = roleTable;
    }

    public String getAuthorityTable() {
        return authorityTable;
    }

    public void setAuthorityTable(String authorityTable) {
        this.authorityTable = authorityTable;
    }

    public String getUserRoleTable() {
        return userRoleTable;
    }

    public void setUserRoleTable(String userRoleTable) {
        this.userRoleTable = userRoleTable;
    }

    public String getRoleAuthorityTable() {
        return roleAuthorityTable;
    }

    public void setRoleAuthorityTable(String roleAuthorityTable) {
        this.roleAuthorityTable = roleAuthorityTable;
    }

    public String getRoleComponentsTable() {
        return roleComponentsTable;
    }

    public void setRoleComponentsTable(String roleComponentsTable) {
        this.roleComponentsTable = roleComponentsTable;
    }
}
