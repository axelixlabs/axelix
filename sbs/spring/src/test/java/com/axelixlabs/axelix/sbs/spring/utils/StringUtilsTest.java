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
package com.axelixlabs.axelix.sbs.spring.utils;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StringUtils}.
 *
 * @since 07.05.25
 * @author Mikhail Polivakha
 */
class StringUtilsTest {

    @ParameterizedTest
    @MethodSource(value = "sourceContainsIgnoreCase")
    void test_containsIgnoreCase(String source, String dest, boolean result) {
        assertThat(StringUtils.containsIgnoreCase(source, dest)).isEqualTo(result);
    }

    static Stream<Arguments> sourceContainsIgnoreCase() {
        return Stream.of(
                Arguments.of(null, null, true),
                Arguments.of("Non null string", null, false),
                Arguments.of("", null, false),
                Arguments.of("exact_match", "exact_match", true),
                Arguments.of("non_non_exact", "non_exact", true),
                Arguments.of("it_is_non_exact_match_but_still_contains", "non_exact_match", true),
                Arguments.of("case_IgnoreD", "CASE_iGNOREd", true),
                Arguments.of("head_case_IGNORED_tail", "CASE_Ig", true),
                Arguments.of("less_characters", "more_characters_in_dest", false),
                Arguments.of("Letter_missing", "Letter_mssing", false));
    }
}
