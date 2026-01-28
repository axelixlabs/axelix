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
package com.axelixlabs.axelix.master.service.convert.utils;

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
