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
package com.axelixlabs.axelix.master.service.versions;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link VersionTrimmer}
 *
 * @since 18.12.2025
 * @author Nikita Kirillov
 */
class VersionTrimmerTest {

    @ParameterizedTest
    @MethodSource("majorVersionTestCases")
    void getMajorVersion(String input, String expected) {
        assertThat(expected).isEqualTo(VersionTrimmer.getMajorVersion(input));
    }

    private static Stream<Arguments> majorVersionTestCases() {
        return Stream.of(
                Arguments.of("17.0.19u", "17"),
                Arguments.of("3.5.2", "3"),
                Arguments.of("11", "11"),
                Arguments.of("2.0", "2"),
                Arguments.of("1.2.3.4", "1"),
                Arguments.of("17.0.19", "17"),
                Arguments.of("2.5.0-SNAPSHOT", "2"),
                Arguments.of("3.0.0-RELEASE", "3"));
    }

    @ParameterizedTest
    @MethodSource("majorMinorVersionTestCases")
    void getMajorMinorVersion(String input, String expected) {
        assertThat(expected).isEqualTo(VersionTrimmer.getMajorMinorVersion(input));
    }

    private static Stream<Arguments> majorMinorVersionTestCases() {
        return Stream.of(
                Arguments.of("17.0.19u", "17.0"),
                Arguments.of("3.5.2", "3.5"),
                Arguments.of("1.2.3.4", "1.2"),
                Arguments.of("11.0", "11.0"),
                Arguments.of("11", "11"),
                Arguments.of("2.5.0-SNAPSHOT", "2.5"),
                Arguments.of("3.0.0-RELEASE", "3.0"));
    }
}
