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
package com.axelixlabs.axelix.master.service.auth.provider;

import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.jdbc.Sql;

import com.axelixlabs.axelix.common.auth.core.ExternalAuthority;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.UserNotFoundException;
import com.axelixlabs.axelix.master.service.auth.jwt.JdbcAuthConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link JdbcUserProvider} using Testcontainers with PostgreSQL.
 *
 * @author Nikita Kirillov
 * @since 17.07.2025
 */
@Sql(scripts = {"/db/jdbc-user-provider-test-schema.sql", "/db/jdbc-user-provider-test-data.sql"})
@Import({JdbcUserProviderTestcontainersTest.JdbcAuthTestConfig.class})
class JdbcUserProviderTestcontainersTest extends BaseTestcontainersIntegrationTest {

    @Autowired
    private UserProvider userProvider;

    @Test
    void shouldLoadAdminUserWithRolesAndAuthorities() {
        String adminUser = "adminUser";
        User user = userProvider.load(adminUser);
        assertThat(user.getUsername()).isNotNull().isEqualTo(adminUser);
        assertThat(user.getRoles()).hasSize(1);

        assertThat(user.getRoles())
                .filteredOn(role -> role.getName().equals("ROLE_ADMIN"))
                .hasSize(1)
                .first()
                .satisfies(role -> assertThat(role.getAuthorities())
                        .hasSize(1)
                        .containsOnly(ExternalAuthority.PROFILE_MANAGEMENT));

        // ROLE_ADMIN -> ROLE_ENGINEER, ROLE_CACHE_DISPATCHER
        Role adminRole = user.getRoles().iterator().next();
        Set<Role> adminComponents = adminRole.getComponents();

        String roleEngineer = "ROLE_ENGINEER";
        String roleCacheDispatcher = "ROLE_CACHE_DISPATCHER";

        assertThat(adminComponents)
                .hasSize(2)
                .extracting(Role::getName)
                .containsOnly(roleEngineer, roleCacheDispatcher);

        // ROLE_ADMIN -> ROLE_ENGINEER
        Role engineerRole = adminComponents.stream()
                .filter(role -> role.getName().equals(roleEngineer))
                .findFirst()
                .orElseThrow();

        assertThat(engineerRole.getAuthorities()).hasSize(1).containsOnly(ExternalAuthority.ENV);

        // ROLE_ADMIN -> ROLE_ENGINEER -> ROLE_USER
        assertThat(engineerRole.getComponents())
                .filteredOn(role -> role.getName().equals("ROLE_USER"))
                .hasSize(1)
                .first()
                .satisfies(role -> assertThat(role.getAuthorities()).hasSize(1).containsOnly(ExternalAuthority.INFO))
                .satisfies(role -> assertThat(role.getComponents()).isEmpty());

        // ROLE_ADMIN -> ROLE_CACHE_DISPATCHER
        Role cacheDispatcherRole = adminComponents.stream()
                .filter(role -> role.getName().equals(roleCacheDispatcher))
                .findFirst()
                .orElseThrow();

        assertThat(cacheDispatcherRole.getAuthorities()).hasSize(1).containsOnly(ExternalAuthority.CACHE_DISPATCHER);

        // ROLE_ADMIN -> ROLE_CACHE_DISPATCHER -> ROLE_CACHE_ACCESS
        assertThat(cacheDispatcherRole.getComponents())
                .filteredOn(role -> role.getName().equals("ROLE_CACHE_ACCESS"))
                .hasSize(1)
                .first()
                .satisfies(role -> assertThat(role.getAuthorities()).hasSize(1).containsOnly(ExternalAuthority.CACHES))
                .satisfies(role -> assertThat(role.getComponents()).isEmpty());
    }

    @Test
    void shouldLoadBasicUserWithRolesAndAuthorities() {
        String basicUser = "basicUser";
        User user = userProvider.load(basicUser);

        assertThat(user.getUsername()).isNotNull().isEqualTo(basicUser);
        assertThat(user.getRoles())
                .hasSize(1)
                .filteredOn(role -> role.getName().equals("ROLE_BEANS_ACCESS"))
                .first()
                .satisfies(role -> assertThat(role.getAuthorities()).containsOnly(ExternalAuthority.BEANS));
    }

    @Test
    void shouldLoadUserWithNonexistentAuthoritiesOnly() {
        String nonexistentAuthorityUser = "nonexistentAuthorityUser";
        User user = userProvider.load(nonexistentAuthorityUser);

        assertThat(user.getUsername()).isNotNull().isEqualTo(nonexistentAuthorityUser);
        assertThat(user.getRoles())
                .hasSize(1)
                .filteredOn(role -> role.getName().equals("ROLE_WITH_NONEXISTENT_AUTHORITY"))
                .first()
                .satisfies(role -> assertThat(role.getAuthorities()).isEmpty());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotExists() {
        assertThatThrownBy(() -> userProvider.load("nonexistent")).isInstanceOf(UserNotFoundException.class);
    }

    /**
     * Test configuration for {@link JdbcUserProvider} tests.
     *
     * <ul>
     *     <li>A {@link JdbcAuthConfig} bean loaded from properties prefixed with {@code axelix.config.auth.tables}.</li>
     *     <li>A {@link JdbcUserProvider} bean configured with the test DataSource and JdbcAuthConfig.</li>
     * </ul>
     *
     * <p>Note: The default table names defined in {@link JdbcAuthConfig} have the prefix "axelix_",
     * such as {@code axelix_user_table}, {@code axelix_role_table}, etc.</p>
     *
     * <p>In the {@code application.yaml}, these default table names are explicitly overridden with
     * table names like {@code users}, {@code roles}, etc. to verify that the system works
     * correctly with custom table names.
     * </p>
     */
    @TestConfiguration
    @EnableConfigurationProperties
    public static class JdbcAuthTestConfig {

        @Bean
        @ConfigurationProperties(prefix = "axelix.config.auth.tables")
        public JdbcAuthConfig authTablesConfig() {
            return new JdbcAuthConfig();
        }

        @Bean
        public DataSource dataSource(DataSourceProperties dataSourceProperties) {
            return new DriverManagerDataSource(
                    dataSourceProperties.getUrl(),
                    dataSourceProperties.getUsername(),
                    dataSourceProperties.getPassword());
        }

        @Bean
        public UserProvider userProvider(DataSource dataSource, JdbcAuthConfig jdbcAuthConfig) {
            return new JdbcUserProvider(dataSource, jdbcAuthConfig);
        }
    }
}
