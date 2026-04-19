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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link RequiredAuthorityCheckService}
 *
 * @author Nikita Kirillov
 */
class RequiredAuthorityCheckServiceTest {

    private static RequiredAuthorityCheckService subject;

    private static ThreadLocalSecurityContextExecutor securityContextExecutor;

    private SecurityContext securityContext;

    @BeforeAll
    static void setUp() {
        securityContextExecutor = new ThreadLocalSecurityContextExecutor();
        subject = new RequiredAuthorityCheckService(securityContextExecutor);
    }

    @Test
    void hasAuthority_ShouldReturnTrue_WhenSecurityContextHasUserWithAuthority() {
        securityContext = new DefaultSecurityContext(
                createUserWithAuthorities(DefaultAuthority.CONFIG_PROPS_VALUES_READ), "testToken");

        securityContextExecutor.runWithinSecurityContext(
                () -> {
                    boolean result = subject.hasAuthority(DefaultAuthority.CONFIG_PROPS_VALUES_READ);
                    assertThat(result).isTrue();
                },
                securityContext);
    }

    @Test
    void hasAuthority_ShouldReturnFalse_WhenSecurityContextHasUserWithoutAuthority() {
        securityContext = new DefaultSecurityContext(createUserWithAuthorities(), "testToken");

        securityContextExecutor.runWithinSecurityContext(
                () -> {
                    boolean result = subject.hasAuthority(DefaultAuthority.ENV_VALUES_READ);
                    assertThat(result).isFalse();
                },
                securityContext);
    }

    @Test
    void hasAuthority_ShouldThrowException_WhenNoSecurityContext() {
        assertThatThrownBy(() -> subject.hasAuthority(DefaultAuthority.CONFIG_PROPS_VALUES_READ))
                .isInstanceOf(AuthorizationException.class);
    }

    private static User createUserWithAuthorities(DefaultAuthority... authorities) {
        Set<Authority> authoritySet = Set.of(authorities);
        Role role = new DefaultRole("testRole", authoritySet);
        return new PasswordlessUser("testUser", Set.of(role));
    }
}
