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
package com.axelixlabs.axelix.master.service.auth.encoder;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SuperAdminPasswordEncoder}.
 *
 * @author Ilya Naumov
 */
@ExtendWith(OutputCaptureExtension.class)
public class SuperAdminPasswordEncoderTest {
    private static final String PLAIN_PASSWORD = "password";
    private static final String BCRYPT_PASSWORD = "$2a$10$KjkxE0Tt8L4B2kDYlSWcme0o/AjKE7LqyDaTqPr0sESbF85e3bDTC";

    private SuperAdminPasswordEncoder encoder;

    @BeforeEach
    void setup() {
        this.encoder = new SuperAdminPasswordEncoder(new BCryptPasswordEncoder());
    }

    @Nested
    class ValidationTests {
        @Test // GH-1004
        void shouldNotWarn_whenPasswordHasSupportedFormat(CapturedOutput capturedOutput) {
            // given.
            String bcryptPassword = "{bcrypt}" + BCRYPT_PASSWORD;

            // when.
            encoder.validatePasswordFormat(bcryptPassword);

            // then.
            assertThat(capturedOutput).doesNotContainIgnoringCase("not hashed");
        }

        @ParameterizedTest // GH-1004
        @ValueSource(
                strings = {
                    PLAIN_PASSWORD,
                    "{noop}" + PLAIN_PASSWORD,
                })
        void shouldWarn_whenPasswordIsNotHashed(String password, CapturedOutput capturedOutput) {
            // when.
            encoder.validatePasswordFormat(password);

            // then.
            assertThat(capturedOutput).containsIgnoringCase("not hashed");
        }

        @ParameterizedTest // GH-1004
        @ValueSource(
                strings = {
                    "{}" + PLAIN_PASSWORD,
                    "{unsupported}" + PLAIN_PASSWORD,
                    "{bcrypt" + BCRYPT_PASSWORD,
                })
        void shouldThrowIllegalArgumentException_whenPasswordFormatIsInvalid(String invalidPassword) {
            ThrowableAssert.ThrowingCallable callable = () -> encoder.validatePasswordFormat(invalidPassword);

            // when.
            assertThatThrownBy(callable)
                    // then.
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class MatchTests {
        @ParameterizedTest // GH-1004
        @ValueSource(
                strings = {
                    PLAIN_PASSWORD,
                    "{noop}" + PLAIN_PASSWORD,
                    "{bcrypt}" + BCRYPT_PASSWORD,
                })
        void shouldMatch_whenEncodedPasswordUsesSupportedFormat(String encodedPassword) {
            // when.
            boolean result = encoder.matches(PLAIN_PASSWORD, encodedPassword);

            // then.
            assertThat(result).isTrue();
        }

        @ParameterizedTest // GH-1004
        @ValueSource(
                strings = {
                    "different_password",
                    "{noop}different_password",
                    "{bcrypt}$2a$10$differentBcryptHashValueHere1234567890abcdefghijk",
                })
        void shouldNotMatch_whenEncodedPasswordRepresentsDifferentPassword(String encodedPassword) {
            // when.
            boolean result = encoder.matches(PLAIN_PASSWORD, encodedPassword);

            // then.
            assertThat(result).isFalse();
        }
    }

    @Nested
    class ExtractEncodedPasswordTests {
        @Test // GH-1004
        void shouldExtract_whenPasswordHasEncodingPrefix() {
            // when.
            String extractedPassword = encoder.extractEncodedPassword("{noop}" + PLAIN_PASSWORD);

            // then.
            assertThat(extractedPassword).isEqualTo(PLAIN_PASSWORD);
        }

        @Test // GH-1004
        void shouldReturnSameValue_whenPasswordDoesNotHaveEncodingPrefix() {
            // when.
            String extractedPassword = encoder.extractEncodedPassword(PLAIN_PASSWORD);

            // then.
            assertThat(extractedPassword).isEqualTo(PLAIN_PASSWORD);
        }
    }
}
