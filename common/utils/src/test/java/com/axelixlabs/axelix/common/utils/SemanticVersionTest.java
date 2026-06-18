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
package com.axelixlabs.axelix.common.utils;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SemanticVersion}.
 *
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
class SemanticVersionTest {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Parse {

        @ParameterizedTest
        @MethodSource("invalidVersions")
        void tryParseReturnsEmpty(String input) {
            // when
            Optional<SemanticVersion> actual = SemanticVersion.tryParse(input);

            // then
            assertThat(actual).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("invalidVersions")
        void parseThrows(String input) {
            assertThatThrownBy(() -> SemanticVersion.parse(input)).isInstanceOf(IllegalArgumentException.class);
        }

        Stream<Arguments> invalidVersions() {
            return Stream.of(
                    Arguments.of((String) null),
                    Arguments.of(""),
                    Arguments.of("   "),
                    Arguments.of("abc"),
                    Arguments.of("v1"));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Major {

        @ParameterizedTest
        @MethodSource("majorCases")
        void major(String input, int expected) {
            // when
            int actual = SemanticVersion.parse(input).major();

            // then
            assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> majorCases() {
            return Stream.of(
                    Arguments.of("17.0.19u", 17),
                    Arguments.of("3.5.2", 3),
                    Arguments.of("11", 11),
                    Arguments.of("2.0", 2),
                    Arguments.of("1.2.3.4", 1),
                    Arguments.of("2.5.0-SNAPSHOT", 2),
                    Arguments.of("3.0.0-RELEASE", 3));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Minor {

        @ParameterizedTest
        @MethodSource("minorCases")
        void minor(String input, int expected) {
            // when
            int actual = SemanticVersion.parse(input).minor();

            // then
            assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> minorCases() {
            return Stream.of(
                    Arguments.of("17.0.19u", 0),
                    Arguments.of("3.5.2", 5),
                    Arguments.of("11", 0),
                    Arguments.of("2.4", 4),
                    Arguments.of("1.2.3.4", 2),
                    Arguments.of("2.5.0-SNAPSHOT", 5));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class MajorMinor {

        @ParameterizedTest
        @MethodSource("majorMinorCases")
        void majorMinor(String input, String expected) {
            // when
            String actual = SemanticVersion.parse(input).majorMinor();

            // then
            assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> majorMinorCases() {
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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class VersionNumber {

        @ParameterizedTest
        @MethodSource("versionNumberCases")
        void versionNumber(String input, String expected) {
            // when
            String actual = SemanticVersion.parse(input).versionNumber();

            // then
            assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> versionNumberCases() {
            return Stream.of(
                    Arguments.of("3.0.15.RELEASE", "3.0.15"),
                    Arguments.of("1.2.0.Final", "1.2.0"),
                    Arguments.of("2.5.0-SNAPSHOT", "2.5.0"),
                    Arguments.of("17.0.19u", "17.0.19"),
                    Arguments.of("1.2.3.4", "1.2.3"),
                    Arguments.of("11", "11"),
                    Arguments.of("2.0", "2.0"));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class PatchSegment {

        @ParameterizedTest
        @MethodSource("patchCases")
        void patch(String input, int expected) {
            // when
            int actual = SemanticVersion.parse(input).patch();

            // then
            assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> patchCases() {
            return Stream.of(
                    Arguments.of("3.0.15.RELEASE", 15),
                    Arguments.of("1.2.3.4", 3),
                    Arguments.of("17.0.19u", 19),
                    Arguments.of("2.0", 0),
                    Arguments.of("11", 0));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class QualifierSuffix {

        @ParameterizedTest
        @MethodSource("qualifierCases")
        void qualifier(String input, String expected) {
            // when
            String actual = SemanticVersion.parse(input).qualifier();

            // then
            assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> qualifierCases() {
            return Stream.of(
                    Arguments.of("3.0.15.RELEASE", "RELEASE"),
                    Arguments.of("1.2.0.Final", "Final"),
                    Arguments.of("2.5.0-SNAPSHOT", "SNAPSHOT"),
                    Arguments.of("17.0.19u", "u"),
                    Arguments.of("1.2.3.4", "4"),
                    Arguments.of("11", null));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Compare {

        @ParameterizedTest
        @MethodSource("comparisonCases")
        void compareTo(String left, String right, int expectedSign) {
            // given
            SemanticVersion a = SemanticVersion.parse(left);
            SemanticVersion b = SemanticVersion.parse(right);

            // when
            int result = a.compareTo(b);

            // then
            assertThat(Integer.signum(result)).isEqualTo(expectedSign);
        }

        Stream<Arguments> comparisonCases() {
            return Stream.of(
                    Arguments.of("2.5.0", "2.4.9", 1),
                    Arguments.of("2.4.9", "2.5.0", -1),
                    Arguments.of("3.0.0", "3.0.0", 0),
                    Arguments.of("2.0.0", "1.9.9", 1),
                    Arguments.of("1.2.3", "1.2.4", -1),
                    Arguments.of("1.2", "1.2.0", 0),
                    Arguments.of("11", "11.0.0", 0),
                    Arguments.of("1.0.0-SNAPSHOT", "1.0.0.RELEASE", 0),
                    Arguments.of("1.0.0-SNAPSHOT", "1.0.0", -1),
                    Arguments.of("1.0.0", "1.0.0-SNAPSHOT", 1));
        }
    }
}
