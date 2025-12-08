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
package com.nucleonforge.axile.master.service.convert.utils;

import java.time.Duration;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link DateTimeFormattingUtils}.
 *
 * @author Mikhail Polivakha
 */
class DateTimeFormattingUtilsTest {

    @ParameterizedTest
    @MethodSource(value = "arguments")
    void shouldConvertToHumanReadableDuration(Duration source, String expectedResult) {
        Assertions.assertThat(DateTimeFormattingUtils.toHumanReadableDuration(source))
                .isEqualTo(expectedResult);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of(Duration.ofHours(26), "1d 2h"),
                Arguments.of(Duration.ofMinutes(144), "2h 24m"),
                Arguments.of(Duration.ofSeconds(4), "0m 4s"),
                Arguments.of(Duration.ofSeconds(89), "1m 29s"));
    }
}
