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
package com.axelixlabs.axelix.master.service.auth;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.AuthorizationRequest;
import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.exception.auth.AuthorizationException;
import com.axelixlabs.axelix.master.testfixtures.UserUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultAuthorizer}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@SpringBootTest
class DefaultAuthorizerTest {

    @Autowired
    private JdbcClient jdbcClient;

    private final Authorizer authorizer = new DefaultAuthorizer();
    private Authority readReports;
    private Authority runMaintenance;

    @BeforeEach
    void setUp() {
        readReports = insertAuthority("authorizer-test-read", "test:reports:read");
        runMaintenance = insertAuthority("authorizer-test-run", "test:maintenance:run");
    }

    @AfterEach
    void cleanUp() {
        jdbcClient
                .sql("DELETE FROM authorities WHERE id IN ('authorizer-test-read', 'authorizer-test-run')")
                .update();
    }

    @Test
    void shouldAuthorizeWhenRoleGrantsAllRequiredAuthorities() {
        // given.
        Role role = new DefaultRole("REPORT_OPERATOR", Set.of(readReports, runMaintenance));
        PasswordlessUser user = UserUtils.passwordless("testUser", Set.of(role));
        AuthorizationRequest request = new AuthorizationRequest(Set.of(readReports, runMaintenance));

        // when / then.
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldAuthorizeWhenRoleGrantsMoreThanRequired() {
        // given.
        Role role = new DefaultRole("REPORT_OPERATOR", Set.of(readReports, runMaintenance));
        PasswordlessUser user = UserUtils.passwordless("testUser", Set.of(role));
        AuthorizationRequest request = new AuthorizationRequest(Set.of(readReports));

        // when / then.
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldAuthorizeEmptyRequest(boolean hasRole) {
        // given.
        Set<Role> roles = hasRole ? Set.of(new DefaultRole("EMPTY_ROLE", Set.of())) : Set.of();
        PasswordlessUser user = UserUtils.passwordless("testUser", roles);
        AuthorizationRequest request = new AuthorizationRequest(Set.of());

        // when / then.
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldRejectWhenRoleGrantsOnlySomeRequiredAuthorities() {
        // given.
        Role role = new DefaultRole("REPORT_READER", Set.of(readReports));
        PasswordlessUser user = UserUtils.passwordless("testUser", Set.of(role));
        AuthorizationRequest request = new AuthorizationRequest(Set.of(readReports, runMaintenance));

        // when / then.
        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @Test
    void shouldRejectWhenRoleHasNoAuthorities() {
        // given.
        Role role = new DefaultRole("EMPTY_ROLE", Set.of());
        PasswordlessUser user = UserUtils.passwordless("testUser", Set.of(role));
        AuthorizationRequest request = new AuthorizationRequest(Set.of(readReports));

        // when / then.
        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @Test
    void shouldRejectWhenNoRoleGrantsRequiredAuthority() {
        // given.
        PasswordlessUser user = UserUtils.passwordless("testUser", Set.of());
        AuthorizationRequest request = new AuthorizationRequest(Set.of(runMaintenance));

        // when / then.
        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @Test
    void shouldAuthorizeWhenRequiredAuthoritiesAreSplitAcrossRoles() {
        // given.
        Role reports = new DefaultRole("REPORT_READER", Set.of(readReports));
        Role maintenance = new DefaultRole("MAINTENANCE_OPERATOR", Set.of(runMaintenance));
        PasswordlessUser user = UserUtils.passwordless("testUser", Set.of(reports, maintenance));
        AuthorizationRequest request = new AuthorizationRequest(Set.of(readReports, runMaintenance));

        // when / then.
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    private Authority insertAuthority(String id, String codename) {
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params(id, codename, "Authorizer test authority")
                .update();
        return jdbcClient
                .sql("SELECT id, codename FROM authorities WHERE id = ?")
                .param(id)
                .query((rs, n) -> new DefaultAuthority(rs.getString("id"), rs.getString("codename")))
                .single();
    }
}
