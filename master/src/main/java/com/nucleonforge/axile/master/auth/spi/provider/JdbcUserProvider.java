package com.nucleonforge.axile.master.auth.spi.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.nucleonforge.axile.common.auth.core.Authority;
import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.DefaultUser;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;

/**
 * {@link UserProvider} that is capable to load user from RDBMS database.
 *
 * @author Mikhail Polivakha
 * @since 16.07.25
 */
public class JdbcUserProvider implements UserProvider {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcAuthConfig jdbcAuthConfig;

    public JdbcUserProvider(DataSource dataSource, JdbcAuthConfig jdbcAuthConfig) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcAuthConfig = jdbcAuthConfig;
    }

    @Override
    @Transactional
    public User load(String username) throws UserNotFoundException {
        String sql = buildUserRolesHierarchyQuery();
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("username", username);

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

            if (rows.isEmpty()) {
                throw new UserNotFoundException("User not found: " + username);
            }

            Map<Integer, RoleBuilder> roleBuilders = new HashMap<>();
            Map<Integer, Set<Integer>> parentToChildren = new HashMap<>();

            extractRolesFromRows(rows, roleBuilders, parentToChildren);

            linkParentChildRoles(roleBuilders, parentToChildren);

            Set<Role> rootRoles = buildRootRoles(roleBuilders);

            return new DefaultUser(username, rootRoles);

        } catch (Exception e) {
            throw new UserNotFoundException("Failed to load user: " + username, e);
        }
    }

    private String buildUserRolesHierarchyQuery() {
        return """
            WITH RECURSIVE role_hierarchy AS (
                SELECT r.role_id, r.role_name, CAST(NULL AS INTEGER) AS parent_role_id
                FROM %s ur
                JOIN %s r ON ur.role_id = r.role_id
                JOIN %s u ON u.user_id = ur.user_id
                WHERE u.username = :username

                UNION ALL

                SELECT rc.component_role_id, r2.role_name, rc.parent_role_id
                FROM %s rc
                JOIN %s r2 ON rc.component_role_id = r2.role_id
                JOIN role_hierarchy rh ON rc.parent_role_id = rh.role_id
            )
            SELECT rh.role_id, rh.role_name, rh.parent_role_id, a.authority_name
            FROM role_hierarchy rh
            LEFT JOIN %s ra ON rh.role_id = ra.role_id
            LEFT JOIN %s a ON ra.authority_id = a.authority_id
            """
                .formatted(
                        jdbcAuthConfig.getUserRoleTable(),
                        jdbcAuthConfig.getRoleTable(),
                        jdbcAuthConfig.getUserTable(),
                        jdbcAuthConfig.getRoleComponentsTable(),
                        jdbcAuthConfig.getRoleTable(),
                        jdbcAuthConfig.getRoleAuthorityTable(),
                        jdbcAuthConfig.getAuthorityTable());
    }

    @SuppressWarnings("NullAway") // Suppress because ID fields in our DB schema can never be null.
    private void extractRolesFromRows(
            List<Map<String, Object>> rows,
            Map<Integer, RoleBuilder> roleBuilders,
            Map<Integer, Set<Integer>> parentToChildren) {

        for (Map<String, Object> row : rows) {
            Integer roleId = ((Number) row.get("role_id")).intValue();
            String roleName = (String) row.get("role_name");
            Integer parentRoleId = (Integer) row.get("parent_role_id");
            String authorityName = (String) row.get("authority_name");

            RoleBuilder rb =
                    roleBuilders.computeIfAbsent(roleId, id -> new RoleBuilder(roleId, roleName, parentRoleId));

            DefaultAuthority authority = safeAuthoritiesFromString(authorityName);
            if (authority != null) {
                rb.authorities.add(authority);
            }

            if (parentRoleId != null) {
                parentToChildren
                        .computeIfAbsent(parentRoleId, k -> new HashSet<>())
                        .add(roleId);
            }
        }
    }

    private void linkParentChildRoles(
            Map<Integer, RoleBuilder> roleBuilders, Map<Integer, Set<Integer>> parentToChildren) {

        for (Map.Entry<Integer, Set<Integer>> entry : parentToChildren.entrySet()) {
            RoleBuilder parent = roleBuilders.get(entry.getKey());
            if (parent != null) {
                for (Integer childId : entry.getValue()) {
                    RoleBuilder child = roleBuilders.get(childId);
                    if (child != null) {
                        parent.components.add(child);
                    }
                }
            }
        }
    }

    private Set<Role> buildRootRoles(Map<Integer, RoleBuilder> roleBuilders) {
        return roleBuilders.values().stream()
                .filter(rb -> rb.parentId == null)
                .map(RoleBuilder::build)
                .collect(Collectors.toSet());
    }

    private static class RoleBuilder {
        final Integer roleId;
        final String name;
        final Integer parentId;
        final Set<Authority> authorities = new HashSet<>();
        final Set<RoleBuilder> components = new HashSet<>();

        RoleBuilder(Integer roleId, String name, Integer parentId) {
            this.roleId = roleId;
            this.name = name;
            this.parentId = parentId;
        }

        DefaultRole build() {
            Set<Role> builtComponents =
                    components.stream().map(RoleBuilder::build).collect(Collectors.toSet());
            return new DefaultRole(name, Set.copyOf(authorities), builtComponents);
        }
    }

    @SuppressWarnings("NullAway")
    private DefaultAuthority safeAuthoritiesFromString(String name) {
        try {
            return DefaultAuthority.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
