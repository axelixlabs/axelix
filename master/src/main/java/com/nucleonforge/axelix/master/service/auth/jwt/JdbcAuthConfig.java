/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
