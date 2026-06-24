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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class SuperAdminPasswordEncoderTest {
    private static final String PLAIN_PASSWORD = "password";

    @Spy
    private BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private SuperAdminPasswordEncoder encoder;

    @Nested
    class ValidationTests {
        @Test // GH-1004
        void shouldNotWarn_whenPasswordHasSupportedFormat(CapturedOutput capturedOutput) {
            // given.
            String bcryptPassword = "{bcrypt}" + bcryptPasswordEncoder.encode(PLAIN_PASSWORD);

            // when.
            encoder.validatePasswordFormat(bcryptPassword);

            // then.
            assertThat(capturedOutput).doesNotContainIgnoringCase("not hashed");
        }

        @Test // GH-1004
        void shouldWarn_whenPasswordHasPlainTextFormat(CapturedOutput capturedOutput) {
            // when.
            encoder.validatePasswordFormat(PLAIN_PASSWORD);

            // then.
            assertThat(capturedOutput).containsIgnoringCase("not hashed");
        }

        @Test // GH-1004
        void shouldWarn_whenPasswordHasNoOpFormat(CapturedOutput capturedOutput) {
            // given.
            String noopPassword = "{noop}" + PLAIN_PASSWORD;

            // when.
            encoder.validatePasswordFormat(noopPassword);

            // then.
            assertThat(capturedOutput).containsIgnoringCase("not hashed");
        }

        @Test // GH-1004
        void shouldThrowIllegalArgumentException_whenPasswordHasUnsupportedFormat() {
            // given.
            String unsupportedPassword = "{unsupported}" + PLAIN_PASSWORD;
            ThrowableAssert.ThrowingCallable callable = () -> encoder.validatePasswordFormat(unsupportedPassword);

            // when.
            assertThatThrownBy(callable)
                    // then.
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test // GH-1004
        void shouldThrowIllegalArgumentException_whenPasswordHasMalformedPrefixMissingClosingBracket() {
            // given.
            String malformedPassword = "{bcrypt" + bcryptPasswordEncoder.encode(PLAIN_PASSWORD);
            ThrowableAssert.ThrowingCallable callable = () -> encoder.validatePasswordFormat(malformedPassword);

            // when.
            assertThatThrownBy(callable)
                    // then.
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test // GH-1004
        void shouldThrowIllegalArgumentException_whenPasswordHasEmptyEncoderId() {
            // given.
            String malformedPassword = "{}" + PLAIN_PASSWORD;
            ThrowableAssert.ThrowingCallable callable = () -> encoder.validatePasswordFormat(malformedPassword);

            // when.
            assertThatThrownBy(callable)
                    // then.
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class MatchTests {
        @Test // GH-1004
        void shouldMatch_whenPasswordHasPlaintTextFormat() {
            // when.
            boolean result = encoder.matches(PLAIN_PASSWORD, PLAIN_PASSWORD);

            // then.
            assertThat(result).isTrue();
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
