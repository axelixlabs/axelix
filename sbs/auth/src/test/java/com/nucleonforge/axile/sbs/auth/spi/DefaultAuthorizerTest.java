package com.nucleonforge.axile.sbs.auth.spi;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.sbs.auth.AuthorizationException;
import com.nucleonforge.axile.sbs.auth.model.DecodedUser;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultAuthorizer}.
 *
 * @since 30.07.2025
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
