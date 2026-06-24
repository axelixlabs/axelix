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
package com.axelixlabs.axelix.master.service.auth.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.SuperAdminConfigurationProperties;
import com.axelixlabs.axelix.master.service.auth.encoder.SuperAdminPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SuperAdminUserAuthenticator}.
 *
 * @author Ilya Naumov
 */
@ExtendWith(MockitoExtension.class)
class SuperAdminUserAuthenticatorTest {
    private static final String USERNAME = "admin";
    private static final String PLAIN_PASSWORD = "password";

    @Mock
    private SuperAdminPasswordEncoder encoder;

    @Mock
    private SuperAdminConfigurationProperties superAdminConfiguration;

    @InjectMocks
    private SuperAdminUserAuthenticator authenticator;

    @Test // GH-1004
    void shouldInvokePasswordEncoderValidation() {
        // given.
        when(superAdminConfiguration.getPassword()).thenReturn(PLAIN_PASSWORD);

        // when.
        authenticator.validate();

        // then.
        verify(encoder, times(1)).validatePasswordFormat(PLAIN_PASSWORD);
    }

    @Test // GH-1004
    void shouldAuthenticate_whenCredentialsMatch() {
        // given.
        when(superAdminConfiguration.getUsername()).thenReturn(USERNAME);
        when(superAdminConfiguration.getPassword()).thenReturn(PLAIN_PASSWORD);
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(encoder.extractEncodedPassword(anyString())).thenReturn(PLAIN_PASSWORD);

        // when.
        User user = authenticator.authenticate(USERNAME, PLAIN_PASSWORD);

        // then.
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getPassword()).isEqualTo(PLAIN_PASSWORD);
        assertThat(user.getRoles()).containsExactly(DefaultRole.SUPER_ADMIN);
    }

    @Test // GH-1004
    void shouldNotAuthenticate_whenUsernameDoesNotMatch() {
        // given.
        when(superAdminConfiguration.getUsername()).thenReturn(USERNAME);

        // when.
        User user = authenticator.authenticate("wrong-username", PLAIN_PASSWORD);

        // then.
        assertThat(user).isNull();
    }

    @Test // GH-1004
    void shouldNotAuthenticate_whenPasswordDoesNotMatch() {
        // given.
        when(superAdminConfiguration.getUsername()).thenReturn(USERNAME);
        when(superAdminConfiguration.getPassword()).thenReturn(PLAIN_PASSWORD);
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        // when.
        User user = authenticator.authenticate(USERNAME, "wrong-password");

        // then.
        assertThat(user).isNull();
    }
}
