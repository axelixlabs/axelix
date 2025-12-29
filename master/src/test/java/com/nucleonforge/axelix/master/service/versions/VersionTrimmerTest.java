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
package com.nucleonforge.axelix.master.service.versions;

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
