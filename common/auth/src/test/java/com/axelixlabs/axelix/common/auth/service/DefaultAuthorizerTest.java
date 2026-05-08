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
package com.axelixlabs.axelix.common.auth.service;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultAuthorizer}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
class DefaultAuthorizerTest {

    private static final String USER_NAME = "testUser";

    private Authorizer authorizer;

    @BeforeEach
    void setUp() {
        authorizer = new DefaultAuthorizer();
    }

    @Test
    void shouldAuthorize_UserSuperAdminHasRequiredAuthorities() {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.SUPER_ADMIN));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                DefaultAuthority.CACHES_CLEAR,
                DefaultAuthority.CACHES_TOGGLE,
                DefaultAuthority.GARBAGE_COLLECTOR,
                DefaultAuthority.THREAD_DUMP_TOGGLE,
                DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                DefaultAuthority.ENV_VALUES_READ,
                DefaultAuthority.USERS_MANAGEMENT,
                DefaultAuthority.USERS_VIEW));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldAuthorize_UserAdminHasRequiredAuthorities() {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.ADMIN));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                DefaultAuthority.CACHES_CLEAR,
                DefaultAuthority.CACHES_TOGGLE,
                DefaultAuthority.GARBAGE_COLLECTOR,
                DefaultAuthority.THREAD_DUMP_TOGGLE,
                DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                DefaultAuthority.ENV_VALUES_READ));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldAuthorize_UserEditorHasRequiredAuthorities() {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.EDITOR));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                DefaultAuthority.CACHES_CLEAR,
                DefaultAuthority.CACHES_TOGGLE,
                DefaultAuthority.GARBAGE_COLLECTOR,
                DefaultAuthority.THREAD_DUMP_TOGGLE));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @ParameterizedTest
    @MethodSource("allRoles")
    void shouldAuthorizeEmptyRequestWithRole(Role role) {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(role));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, new AuthorizationRequest(Set.of())));
    }

    @Test
    void shouldAuthorizeEmptyRequestWithNoRoles() {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of());

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, new AuthorizationRequest(Set.of())));
    }

    @Test
    void shouldThrowAuthorizationException_UserEditorRequiredAuthorities() {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.EDITOR));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                DefaultAuthority.CACHES_CLEAR,
                DefaultAuthority.CACHES_TOGGLE,
                DefaultAuthority.GARBAGE_COLLECTOR,
                DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                DefaultAuthority.ENV_VALUES_READ,
                DefaultAuthority.USERS_MANAGEMENT,
                DefaultAuthority.USERS_VIEW));

        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @Test
    void shouldThrowAuthorizationException_UserViewerRequiredAuthorities() {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.VIEWER));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                DefaultAuthority.CACHES_CLEAR,
                DefaultAuthority.CACHES_TOGGLE,
                DefaultAuthority.GARBAGE_COLLECTOR,
                DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                DefaultAuthority.ENV_VALUES_READ,
                DefaultAuthority.USERS_MANAGEMENT,
                DefaultAuthority.USERS_VIEW));

        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @Test
    void shouldThrowAuthorizationException_WhenRequestContainsUnrecognizedAuthority() {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.ADMIN));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                DefaultAuthority.CACHES_CLEAR,
                DefaultAuthority.CACHES_TOGGLE,
                DefaultAuthority.GARBAGE_COLLECTOR,
                DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                DefaultAuthority.ENV_VALUES_READ,
                UnrecognizedAuthority.UNRECOGNIZED_AUTHORITY));

        assertThatThrownBy(() -> authorizer.authorize(user, request)).isInstanceOf(AuthorizationException.class);
    }

    @ParameterizedTest
    @MethodSource("adminAuthorities")
    void shouldAuthorize_UserWithMultipleRoles_WhenAuthorityPresentInAnyRole(DefaultAuthority authority) {
        PasswordlessUser user = new PasswordlessUser(USER_NAME, Set.of(DefaultRole.ADMIN, DefaultRole.EDITOR));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(user, new AuthorizationRequest(Set.of(authority))));
    }

    static Stream<DefaultAuthority> adminAuthorities() {
        return Stream.of(
                DefaultAuthority.ENV_VALUES_READ,
                DefaultAuthority.CONFIG_PROPS_VALUES_READ,
                DefaultAuthority.SCHEDULED_TASKS_MODIFY,
                DefaultAuthority.CACHES_CLEAR,
                DefaultAuthority.CACHES_TOGGLE,
                DefaultAuthority.GARBAGE_COLLECTOR,
                DefaultAuthority.THREAD_DUMP_TOGGLE);
    }

    static Stream<Role> allRoles() {
        return Stream.of(DefaultRole.ADMIN, DefaultRole.EDITOR, DefaultRole.MANAGED_SERVICE, DefaultRole.VIEWER);
    }

    enum UnrecognizedAuthority implements Authority {
        UNRECOGNIZED_AUTHORITY;

        @Override
        public String getName() {
            return name();
        }
    }
}
