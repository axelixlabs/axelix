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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration;

import static com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration.SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SuperAdminUserAuthenticator}.
 *
 * @author Ilya Naumov
 */
class SuperAdminUserAuthenticatorTest {
    private static final String USERNAME = "admin";
    private static final String PLAIN_PASSWORD = "password";
    private static final String BCRYPT_PASSWORD = "$2a$10$KjkxE0Tt8L4B2kDYlSWcme0o/AjKE7LqyDaTqPr0sESbF85e3bDTC";

    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setup() {
        this.contextRunner = new ApplicationContextRunner()
                .withBean(BCryptPasswordEncoder.class, BCryptPasswordEncoder::new)
                .withConfiguration(
                        AutoConfigurations.of(SecurityAutoConfiguration.SuperAdminLoginAutoConfiguration.class));
    }

    @Nested
    class ValidationTests {

        @BeforeEach
        void setup() {
            contextRunner = contextRunner.withPropertyValues(
                    SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX + ".credentials.username=" + USERNAME);
        }

        @ParameterizedTest // GH-1004
        @ValueSource(
                strings = {
                    "{unsupported}" + PLAIN_PASSWORD,
                    "{bcrypt" + BCRYPT_PASSWORD,
                    "{}" + PLAIN_PASSWORD,
                })
        void shouldFailContextLaunch_whenPasswordFormatIsInvalid(String invalidPassword) {
            // given.
            contextRunner = contextRunner
                    .withPropertyValues(
                            SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX + ".credentials.password=" + invalidPassword)
                    // when.
                    .run(context -> {
                        // then.
                        assertThat(context).hasFailed();
                        assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalArgumentException.class);
                    });
        }

        @ParameterizedTest // GH-1004
        @ValueSource(
                strings = {
                    PLAIN_PASSWORD,
                    "{noop}" + PLAIN_PASSWORD,
                    "{bcrypt}" + BCRYPT_PASSWORD,
                })
        void shouldCreateBean_whenPasswordIsCorrectlyFormatted(String password) {
            // given.
            contextRunner
                    .withPropertyValues(SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX + ".credentials.password=" + password)
                    // when.
                    .run(context -> {
                        // then.
                        assertThat(context).hasNotFailed();
                        assertThat(context).hasSingleBean(SuperAdminUserAuthenticator.class);
                    });
        }
    }

    @SpringBootTest
    @TestPropertySource(
            properties = {
                SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX + ".credentials.username=admin",
                SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX + ".credentials.password={bcrypt}" + BCRYPT_PASSWORD
            })
    @Nested
    class AuthenticationTest {
        private static final String USERNAME = "admin";
        private static final String PLAIN_PASSWORD = "password";

        @Autowired
        private SuperAdminUserAuthenticator authenticator;

        @Test // GH-1004
        void shouldAuthenticate_whenCredentialsMatch() {
            // when.
            User user = authenticator.authenticate(USERNAME, PLAIN_PASSWORD);

            // then.
            assertThat(user).isNotNull();
            assertThat(user.getUsername()).isEqualTo(USERNAME);
            assertThat(user.getRoles()).containsExactly(DefaultRole.SUPER_ADMIN);
        }

        @Test // GH-1004
        void shouldNotAuthenticate_whenUsernameDoesNotMatch() {
            // when.
            User user = authenticator.authenticate("wrong-username", PLAIN_PASSWORD);

            // then.
            assertThat(user).isNull();
        }

        @Test // GH-1004
        void shouldNotAuthenticate_whenPasswordDoesNotMatch() {
            // when.
            User user = authenticator.authenticate(USERNAME, "wrong-password");

            // then.
            assertThat(user).isNull();
        }
    }
}
