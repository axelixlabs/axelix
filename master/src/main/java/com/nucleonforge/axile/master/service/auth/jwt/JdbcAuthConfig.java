package com.nucleonforge.axile.master.service.auth.jwt;

/**
 * Configuration properties for database table names used by the authentication module.
 *
 * <p>By default, table names have the "axile_" prefix.</p>
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
    private String userTable = "axile_user_table";

    /**
     * Table name where roles are defined.
     */
    private String roleTable = "axile_role_table";

    /**
     * Table name for storing authorities.
     */
    private String authorityTable = "axile_authority_table";

    /**
     * Join table name linking users to their assigned roles.
     */
    private String userRoleTable = "axile_user_role_table";

    /**
     * Join table name linking roles to their assigned authorities.
     */
    private String roleAuthorityTable = "axile_role_authority_table";

    /**
     * Join table name for defining role hierarchies.
     */
    private String roleComponentsTable = "axile_role_components_table";

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
