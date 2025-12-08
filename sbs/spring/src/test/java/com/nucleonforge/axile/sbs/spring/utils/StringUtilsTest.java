/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.spring.utils;

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
