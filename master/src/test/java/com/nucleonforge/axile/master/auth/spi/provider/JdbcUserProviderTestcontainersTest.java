package com.nucleonforge.axile.master.auth.spi.provider;

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

import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.exception.auth.UserNotFoundException;
import com.nucleonforge.axile.master.service.auth.jwt.JdbcAuthConfig;
import com.nucleonforge.axile.master.service.auth.provider.JdbcUserProvider;
import com.nucleonforge.axile.master.service.auth.provider.UserProvider;

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
        assertThat(user.username()).isNotNull().isEqualTo(adminUser);
        assertThat(user.roles()).hasSize(1);

        assertThat(user.roles())
                .filteredOn(role -> role.name().equals("ROLE_ADMIN"))
                .hasSize(1)
                .first()
                .satisfies(role ->
                        assertThat(role.authorities()).hasSize(1).containsOnly(DefaultAuthority.PROFILE_MANAGEMENT));

        // ROLE_ADMIN -> ROLE_ENGINEER, ROLE_CACHE_DISPATCHER
        Role adminRole = user.roles().iterator().next();
        Set<Role> adminComponents = adminRole.components();

        String roleEngineer = "ROLE_ENGINEER";
        String roleCacheDispatcher = "ROLE_CACHE_DISPATCHER";

        assertThat(adminComponents).hasSize(2).extracting(Role::name).containsOnly(roleEngineer, roleCacheDispatcher);

        // ROLE_ADMIN -> ROLE_ENGINEER
        Role engineerRole = adminComponents.stream()
                .filter(role -> role.name().equals(roleEngineer))
                .findFirst()
                .orElseThrow();

        assertThat(engineerRole.authorities()).hasSize(1).containsOnly(DefaultAuthority.ENV);

        // ROLE_ADMIN -> ROLE_ENGINEER -> ROLE_USER
        assertThat(engineerRole.components())
                .filteredOn(role -> role.name().equals("ROLE_USER"))
                .hasSize(1)
                .first()
                .satisfies(role -> assertThat(role.authorities()).hasSize(1).containsOnly(DefaultAuthority.INFO))
                .satisfies(role -> assertThat(role.components()).isEmpty());

        // ROLE_ADMIN -> ROLE_CACHE_DISPATCHER
        Role cacheDispatcherRole = adminComponents.stream()
                .filter(role -> role.name().equals(roleCacheDispatcher))
                .findFirst()
                .orElseThrow();

        assertThat(cacheDispatcherRole.authorities()).hasSize(1).containsOnly(DefaultAuthority.CACHE_DISPATCHER);

        // ROLE_ADMIN -> ROLE_CACHE_DISPATCHER -> ROLE_CACHE_ACCESS
        assertThat(cacheDispatcherRole.components())
                .filteredOn(role -> role.name().equals("ROLE_CACHE_ACCESS"))
                .hasSize(1)
                .first()
                .satisfies(role -> assertThat(role.authorities()).hasSize(1).containsOnly(DefaultAuthority.CACHES))
                .satisfies(role -> assertThat(role.components()).isEmpty());
    }

    @Test
    void shouldLoadBasicUserWithRolesAndAuthorities() {
        String basicUser = "basicUser";
        User user = userProvider.load(basicUser);

        assertThat(user.username()).isNotNull().isEqualTo(basicUser);
        assertThat(user.roles())
                .hasSize(1)
                .filteredOn(role -> role.name().equals("ROLE_BEANS_ACCESS"))
                .first()
                .satisfies(role -> assertThat(role.authorities()).containsOnly(DefaultAuthority.BEANS));
    }

    @Test
    void shouldLoadUserWithNonexistentAuthoritiesOnly() {
        String nonexistentAuthorityUser = "nonexistentAuthorityUser";
        User user = userProvider.load(nonexistentAuthorityUser);

        assertThat(user.username()).isNotNull().isEqualTo(nonexistentAuthorityUser);
        assertThat(user.roles())
                .hasSize(1)
                .filteredOn(role -> role.name().equals("ROLE_WITH_NONEXISTENT_AUTHORITY"))
                .first()
                .satisfies(role -> assertThat(role.authorities()).isEmpty());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotExists() {
        assertThatThrownBy(() -> userProvider.load("nonexistent")).isInstanceOf(UserNotFoundException.class);
    }

    /**
     * Test configuration for {@link JdbcUserProvider} tests.
     *
     * <ul>
     *     <li>A {@link JdbcAuthConfig} bean loaded from properties prefixed with {@code axile.config.auth.tables}.</li>
     *     <li>A {@link JdbcUserProvider} bean configured with the test DataSource and JdbcAuthConfig.</li>
     * </ul>
     *
     * <p>Note: The default table names defined in {@link JdbcAuthConfig} have the prefix "axile_",
     * such as {@code axile_user_table}, {@code axile_role_table}, etc.</p>
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
        @ConfigurationProperties(prefix = "axile.config.auth.tables")
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
