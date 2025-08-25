package com.nucleonforge.axile.master.auth.spi.provider;

import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link JdbcUserProvider} using Testcontainers with PostgreSQL.
 *
 * @author Nikita Kirillov
 * @since 17.07.2025
 */
@Sql(scripts = {"/db/jdbc-user-provider-test-schema.sql", "/db/jdbc-user-provider-test-data.sql"})
@Import(JdbcUserProviderTestcontainersTest.JdbcAuthTestConfig.class)
class JdbcUserProviderTestcontainersTest extends BaseTestcontainersIntegrationTest {

    @Autowired
    private UserProvider userProvider;

    @Test
    void shouldLoadAdminUserWithRolesAndAuthorities() {
        User user = userProvider.load("adminUser");
        assertNotNull(user);
        assertEquals("adminUser", user.username());

        assertEquals(1, user.roles().size());
        Role adminRole = user.roles().iterator().next();
        assertEquals("ROLE_ADMIN", adminRole.name());
        assertEquals(Set.of(DefaultAuthority.PROFILE_MANAGEMENT), adminRole.authorities());

        // ROLE_ADMIN -> ROLE_ENGINEER, ROLE_CACHE_DISPATCHER
        Set<Role> adminComponents = adminRole.components();
        assertEquals(2, adminComponents.size());

        Role engineerRole = adminComponents.stream()
                .filter(r -> r.name().equals("ROLE_ENGINEER"))
                .findFirst()
                .orElseThrow();
        assertEquals(Set.of(DefaultAuthority.ENV), engineerRole.authorities());

        Role cacheDispatcherRole = adminComponents.stream()
                .filter(r -> r.name().equals("ROLE_CACHE_DISPATCHER"))
                .findFirst()
                .orElseThrow();
        assertEquals(Set.of(DefaultAuthority.CACHE_DISPATCHER), cacheDispatcherRole.authorities());

        // ROLE_ADMIN -> ROLE_ENGINEER -> ROLE_USER
        Set<Role> engineerComponents = engineerRole.components();
        assertEquals(1, engineerComponents.size());

        Role userRole = engineerComponents.iterator().next();
        assertEquals("ROLE_USER", userRole.name());
        assertEquals(Set.of(DefaultAuthority.INFO), userRole.authorities());
        assertTrue(userRole.components().isEmpty());

        // ROLE_ADMIN -> ROLE_CACHE_DISPATCHER -> ROLE_CACHE_ACCESS
        Set<Role> cacheDispatcherComponents = cacheDispatcherRole.components();
        assertEquals(1, cacheDispatcherComponents.size());

        Role cacheAccessRole = cacheDispatcherComponents.iterator().next();
        assertEquals("ROLE_CACHE_ACCESS", cacheAccessRole.name());
        assertEquals(Set.of(DefaultAuthority.CACHES), cacheAccessRole.authorities());
        assertTrue(cacheAccessRole.components().isEmpty());
    }

    @Test
    void shouldLoadBasicUserWithRolesAndAuthorities() {
        User user = userProvider.load("basicUser");

        assertNotNull(user);
        assertEquals("basicUser", user.username());
        assertEquals(1, user.roles().size());

        Role role = user.roles().iterator().next();
        assertEquals(1, role.authorities().size());
        assertTrue(role.authorities().stream().anyMatch(a -> a.getName().equals("BEANS")));
        assertFalse(role.authorities().stream().anyMatch(a -> a.getName().equals("ENV")));
    }

    @Test
    void shouldLoadUserWithNonexistentAuthoritiesOnly() {
        User user = userProvider.load("nonexistentAuthorityUser");
        assertNotNull(user);
        assertEquals("nonexistentAuthorityUser", user.username());
        assertEquals(1, user.roles().size());

        Role role = user.roles().iterator().next();
        assertEquals("ROLE_WITH_NONEXISTENT_AUTHORITY", role.name());
        assertTrue(role.authorities().isEmpty());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotExists() {
        assertThrows(
                UserNotFoundException.class,
                () -> userProvider.load("nonexistent"),
                "User not found: " + "nonexistent");
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
     * <p>In the {@code application.yml}, these default table names are explicitly overridden with
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
        public UserProvider userProvider(DataSource dataSource, JdbcAuthConfig jdbcAuthConfig) {
            return new JdbcUserProvider(dataSource, jdbcAuthConfig);
        }
    }
}
