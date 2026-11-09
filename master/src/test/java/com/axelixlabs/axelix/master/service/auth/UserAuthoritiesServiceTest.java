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

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.exception.auth.UserNotFoundException;
import com.axelixlabs.axelix.master.repository.AuthorityRepository.AuthorityProfile;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAuthoritiesServiceTest {

    private final AuthoritiesService catalog = mock(AuthoritiesService.class);
    private final RoleService roles = mock(RoleService.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UserAuthoritiesService service = new UserAuthoritiesService(catalog, roles, users);

    @Test
    void shouldPartitionInheritedAndDirectGrantsWithoutDuplicates() {
        // given.
        AuthorityProfile clear = new AuthorityProfile("1", "caches:clear", "Clear Caches", "Clear application caches");
        AuthorityProfile read = new AuthorityProfile("2", "custom:read", "Read", "Read custom data");
        AuthorityProfile write = new AuthorityProfile("3", "custom:write", "Write", "Write custom data");
        DefaultAuthority clearAuthority = new DefaultAuthority(clear.id(), clear.codename());
        DefaultAuthority readAuthority = new DefaultAuthority(read.id(), read.codename());
        Role parent = new DefaultRole("parent", Set.of(clearAuthority));
        Role child = new DefaultRole("custom", Set.of(readAuthority), Set.of(parent));
        when(users.existsById("user")).thenReturn(true);
        when(roles.findRolesOfUser("user")).thenReturn(Set.of(parent, child));
        when(catalog.getAuthoritiesFeed()).thenReturn(List.of(clear, read, write));

        // when.
        UserAuthoritiesService.UserAuthorities result = service.getAuthorities("user");

        // then.
        assertThat(result.effectiveAuthorities()).containsExactly(clear, read);
        assertThat(result.ungrantedAuthorities()).containsExactly(write);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReturnWholeCatalogAsUngrantedWithoutRoleGrants(boolean hasRole) {
        // given.
        AuthorityProfile profile = new AuthorityProfile("1", "custom:read", "Read", "Read data");
        Set<Role> assigned = hasRole ? Set.of(new DefaultRole("empty", Set.of())) : Set.of();
        when(users.existsById("user")).thenReturn(true);
        when(roles.findRolesOfUser("user")).thenReturn(assigned);
        when(catalog.getAuthoritiesFeed()).thenReturn(List.of(profile));

        // when.
        UserAuthoritiesService.UserAuthorities result = service.getAuthorities("user");

        // then.
        assertThat(result.effectiveAuthorities()).isEmpty();
        assertThat(result.ungrantedAuthorities()).containsExactly(profile);
    }

    @Test
    void shouldReturnEmptyArraysForEmptyCatalog() {
        // given.
        when(users.existsById("user")).thenReturn(true);
        when(roles.findRolesOfUser("user")).thenReturn(Set.of());
        when(catalog.getAuthoritiesFeed()).thenReturn(List.of());

        // when.
        UserAuthoritiesService.UserAuthorities result = service.getAuthorities("user");

        // then.
        assertThat(result.effectiveAuthorities()).isEmpty();
        assertThat(result.ungrantedAuthorities()).isEmpty();
    }

    @Test
    void shouldReturnWholeCatalogAsEffectiveWhenAllAuthoritiesAreGranted() {
        // given.
        AuthorityProfile profile = new AuthorityProfile("1", "custom:read", "Read", "Read data");
        Role role = new DefaultRole("custom", Set.of(new DefaultAuthority(profile.id(), profile.codename())));
        when(users.existsById("user")).thenReturn(true);
        when(roles.findRolesOfUser("user")).thenReturn(Set.of(role));
        when(catalog.getAuthoritiesFeed()).thenReturn(List.of(profile));

        // when.
        UserAuthoritiesService.UserAuthorities result = service.getAuthorities("user");

        // then.
        assertThat(result.effectiveAuthorities()).containsExactly(profile);
        assertThat(result.ungrantedAuthorities()).isEmpty();
    }

    @Test
    void shouldRejectMissingUser() {
        // when / then.
        assertThatThrownBy(() -> service.getAuthorities("missing")).isInstanceOf(UserNotFoundException.class);
    }
}
