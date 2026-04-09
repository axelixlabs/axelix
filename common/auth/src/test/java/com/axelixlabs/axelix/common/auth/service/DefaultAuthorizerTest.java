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

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.GlobalAuthority;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultAuthorizer}.
 *
 * @author Mikhail Polivakha
 */
class DefaultAuthorizerTest {

    private final Authorizer authorizer = new DefaultAuthorizer();

    @Test
    void shouldAuthorize_UserHasRequiredAuthorities() {
        Role role = new DefaultRole(
                "testRole", Set.of(GlobalAuthority.BEANS, GlobalAuthority.HEALTH), Collections.emptySet());
        PasswordlessUser user = new PasswordlessUser("testUser", Set.of(role));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(GlobalAuthority.HEALTH));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldAuthorize_UserWithMultipleRoles_WhenAuthorityPresentInAnyRole() {
        Role role1 = new DefaultRole("firstTestRole", Set.of(GlobalAuthority.CACHE_DISPATCHER), Collections.emptySet());
        Role role2 =
                new DefaultRole("secondTestRole", Set.of(GlobalAuthority.PROFILE_MANAGEMENT), Collections.emptySet());
        PasswordlessUser user = new PasswordlessUser("testUser", Set.of(role1, role2));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(
                        user, new AuthorizationRequest(Set.of(GlobalAuthority.PROFILE_MANAGEMENT))));

        assertThatNoException()
                .isThrownBy(() ->
                        authorizer.authorize(user, new AuthorizationRequest(Set.of(GlobalAuthority.CACHE_DISPATCHER))));
    }

    @Test
    void shouldAuthorize_UserWithMultipleRoles_WhenAuthorityPresentInInnerRole() {
        Role innerRole1 = new DefaultRole("firstInnerTestRole", Set.of(GlobalAuthority.PROPERTY_MANAGEMENT), Set.of());
        Role role1 = new DefaultRole("firstTestRole", null, Set.of(innerRole1));

        Role innerRole2 = new DefaultRole("secondInnerTestRole", Set.of(GlobalAuthority.PROFILE_MANAGEMENT), Set.of());
        Role role2 = new DefaultRole("secondTestRole", null, Set.of(innerRole2));

        PasswordlessUser user = new PasswordlessUser("testUser", Set.of(role1, role2));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(
                        user, new AuthorizationRequest(Set.of(GlobalAuthority.PROPERTY_MANAGEMENT))));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(
                        user, new AuthorizationRequest(Set.of(GlobalAuthority.PROFILE_MANAGEMENT))));
    }

    @Test
    void shouldAuthorize_UserWithEmptyAndValidRole_WhenValidRoleHasRequiredAuthority() {
        Role emptyRole = new DefaultRole("emptyRole", Set.of(), Collections.emptySet());
        Role role = new DefaultRole("testRole", Set.of(GlobalAuthority.HEALTH), Collections.emptySet());
        PasswordlessUser user = new PasswordlessUser("testUser", Set.of(emptyRole, role));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(GlobalAuthority.HEALTH));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldThrowAuthorizationException_UserWithoutRequiredAuthorities() {
        Role role = new DefaultRole("testRole", Set.of(GlobalAuthority.BEANS), Set.of());
        PasswordlessUser user = new PasswordlessUser("testUser", Set.of(role));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(GlobalAuthority.METRICS));

        assertThatThrownBy(() -> authorizer.authorize(user, request))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("Access denied: missing required authorities " + Set.of(GlobalAuthority.METRICS));
    }

    @Test
    void shouldThrowAuthorizationException_WhenUserHasNoAuthoritiesAndRequestRequiresThem() {
        PasswordlessUser user = new PasswordlessUser("testUserWithEmptyAuthorities", Set.of());

        AuthorizationRequest request = new AuthorizationRequest(Set.of(GlobalAuthority.CACHE_DISPATCHER));

        assertThatThrownBy(() -> authorizer.authorize(user, request))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining(
                        "Access denied: missing required authorities " + Set.of(GlobalAuthority.CACHE_DISPATCHER));
    }
}
