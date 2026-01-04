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
package com.nucleonforge.axelix.sbs.spring.auth;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axelix.common.auth.core.DecodedUser;
import com.nucleonforge.axelix.common.auth.core.DefaultAuthority;
import com.nucleonforge.axelix.common.auth.core.DefaultRole;
import com.nucleonforge.axelix.common.auth.core.Role;

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
                "testRole", Set.of(DefaultAuthority.BEANS, DefaultAuthority.HEALTH), Collections.emptySet());
        DecodedUser user = new DecodedUser("testUser", Set.of(role));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(DefaultAuthority.HEALTH));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldAuthorize_UserWithMultipleRoles_WhenAuthorityPresentInAnyRole() {
        Role role1 =
                new DefaultRole("firstTestRole", Set.of(DefaultAuthority.CACHE_DISPATCHER), Collections.emptySet());
        Role role2 =
                new DefaultRole("secondTestRole", Set.of(DefaultAuthority.PROFILE_MANAGEMENT), Collections.emptySet());
        DecodedUser user = new DecodedUser("testUser", Set.of(role1, role2));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(
                        user, new AuthorizationRequest(Set.of(DefaultAuthority.PROFILE_MANAGEMENT))));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(
                        user, new AuthorizationRequest(Set.of(DefaultAuthority.CACHE_DISPATCHER))));
    }

    @Test
    void shouldAuthorize_UserWithMultipleRoles_WhenAuthorityPresentInInnerRole() {
        Role innerRole1 = new DefaultRole("firstInnerTestRole", Set.of(DefaultAuthority.PROPERTY_MANAGEMENT), Set.of());
        Role role1 = new DefaultRole("firstTestRole", null, Set.of(innerRole1));

        Role innerRole2 = new DefaultRole("secondInnerTestRole", Set.of(DefaultAuthority.PROFILE_MANAGEMENT), Set.of());
        Role role2 = new DefaultRole("secondTestRole", null, Set.of(innerRole2));

        DecodedUser user = new DecodedUser("testUser", Set.of(role1, role2));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(
                        user, new AuthorizationRequest(Set.of(DefaultAuthority.PROPERTY_MANAGEMENT))));

        assertThatNoException()
                .isThrownBy(() -> authorizer.authorize(
                        user, new AuthorizationRequest(Set.of(DefaultAuthority.PROFILE_MANAGEMENT))));
    }

    @Test
    void shouldAuthorize_UserWithEmptyAndValidRole_WhenValidRoleHasRequiredAuthority() {
        Role emptyRole = new DefaultRole("emptyRole", Set.of(), Collections.emptySet());
        Role role = new DefaultRole("testRole", Set.of(DefaultAuthority.HEALTH), Collections.emptySet());
        DecodedUser user = new DecodedUser("testUser", Set.of(emptyRole, role));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(DefaultAuthority.HEALTH));

        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldThrowAuthorizationException_UserWithoutRequiredAuthorities() {
        Role role = new DefaultRole("testRole", Set.of(DefaultAuthority.BEANS), Set.of());
        DecodedUser user = new DecodedUser("testUser", Set.of(role));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(DefaultAuthority.METRICS));

        assertThatThrownBy(() -> authorizer.authorize(user, request))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining(
                        "Access denied: missing required authorities " + Set.of(DefaultAuthority.METRICS));
    }

    @Test
    void shouldThrowAuthorizationException_WhenUserHasNoAuthoritiesAndRequestRequiresThem() {
        DecodedUser user = new DecodedUser("testUserWithEmptyAuthorities", Set.of());

        AuthorizationRequest request = new AuthorizationRequest(Set.of(DefaultAuthority.CACHE_DISPATCHER));

        assertThatThrownBy(() -> authorizer.authorize(user, request))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining(
                        "Access denied: missing required authorities " + Set.of(DefaultAuthority.CACHE_DISPATCHER));
    }
}
