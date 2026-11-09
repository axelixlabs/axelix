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
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.AuthorizationRequest;
import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;
import com.axelixlabs.axelix.master.domain.auth.PasswordlessUser;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.exception.auth.AuthorizationException;
import com.axelixlabs.axelix.master.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.testfixtures.UserUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultAuthorizer}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class DefaultAuthorizerTest {
    private Authority envValuesRead;
    private Authority configPropsValuesRead;
    private Authority scheduledTasksModify;
    private Authority cachesClear;
    private Authority cachesToggle;
    private Authority garbageCollector;
    private Authority usersView;
    private Authority usersManagement;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void insertAuthorities() {
        envValuesRead = authority("environment.values:read");
        configPropsValuesRead = authority("config-properties.values:read");
        scheduledTasksModify = authority("scheduled-tasks:write");
        cachesClear = authority("caches:clear");
        cachesToggle = authority("caches:toggle");
        garbageCollector = authority("gc:manage");
        usersView = authority("users:read");
        usersManagement = authority("users:manage");
    }

    @Autowired
    protected TestRoles testRoles;

    private static final String USER_NAME = "testUser";

    private Authorizer authorizer;

    @BeforeEach
    void setUp() {
        authorizer = new DefaultAuthorizer();
    }

    @Test
    void shouldAuthorize_UserAdminHasRequiredAuthorities() {
        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of(testRoles.admin()));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                scheduledTasksModify,
                cachesClear,
                cachesToggle,
                garbageCollector,
                configPropsValuesRead,
                envValuesRead));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldAuthorize_UserEditorHasRequiredAuthorities() {
        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of(testRoles.editor()));

        AuthorizationRequest request =
                new AuthorizationRequest(Set.of(scheduledTasksModify, cachesClear, cachesToggle, garbageCollector));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @ParameterizedTest
    @MethodSource("allRoles")
    void shouldAuthorizeEmptyRequestWithRole(Role role) {
        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of(role));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, new AuthorizationRequest(Set.of())));
    }

    @Test
    void shouldAuthorizeEmptyRequestWithNoRoles() {
        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of());

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, new AuthorizationRequest(Set.of())));
    }

    @Test
    void shouldThrowAuthorizationException_UserEditorRequiredAuthorities() {
        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of(testRoles.editor()));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                scheduledTasksModify,
                cachesClear,
                cachesToggle,
                garbageCollector,
                configPropsValuesRead,
                envValuesRead,
                usersManagement,
                usersView));

        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @Test
    void shouldThrowAuthorizationException_UserViewerRequiredAuthorities() {
        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of(testRoles.viewer()));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                scheduledTasksModify,
                cachesClear,
                cachesToggle,
                garbageCollector,
                configPropsValuesRead,
                envValuesRead,
                usersManagement,
                usersView));

        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @Test
    void shouldThrowAuthorizationException_WhenRequestContainsUnrecognizedAuthority() {
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params("authorizer-test-unknown", "test:unrecognized", "Test authority")
                .update();
        Authority unrecognizedAuthority = authority("test:unrecognized");

        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of(testRoles.admin()));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                scheduledTasksModify,
                cachesClear,
                cachesToggle,
                garbageCollector,
                configPropsValuesRead,
                envValuesRead,
                unrecognizedAuthority));

        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @ParameterizedTest
    @MethodSource("adminAuthorities")
    void shouldAuthorize_UserWithMultipleRoles_WhenAuthorityPresentInAnyRole(Authority authority) {
        PasswordlessUser user = UserUtils.passwordless(USER_NAME, Set.of(testRoles.admin(), testRoles.editor()));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(user, new AuthorizationRequest(Set.of(authority))));
    }

    Stream<Authority> adminAuthorities() {
        return testRoles.admin().authorities().stream();
    }

    Stream<Role> allRoles() {
        return Stream.of(testRoles.admin(), testRoles.editor(), testRoles.viewer());
    }

    @AfterEach
    void deleteAuthorities() {
        jdbcClient
                .sql("DELETE FROM authorities WHERE id = 'authorizer-test-unknown'")
                .update();
    }

    private Authority authority(String codename) {
        return jdbcClient
                .sql("SELECT id, codename FROM authorities WHERE codename = ?")
                .param(codename)
                .query((rs, n) -> new DefaultAuthority(rs.getString("id"), rs.getString("codename")))
                .single();
    }
}
