package com.nucleonforge.axile.spring.utils;

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
    @MethodSource(value = "source_containsIgnoreCase")
    void test_containsIgnoreCase(String source, String dest, boolean result) {
        assertThat(StringUtils.containsIgnoreCase(source, dest)).isEqualTo(result);
    }

    static Stream<Arguments> source_containsIgnoreCase() {
        return Stream.of(
                Arguments.of(null, null, true),
                Arguments.of("Non null string", null, false),
                Arguments.of("", null, false),
                Arguments.of("exact_match", "exact_match", true),
                Arguments.of("it_is_non_exact_match_but_still_contains", "non_exact_match", true),
                Arguments.of("case_IgnoreD", "CASE_iGNOREd", true),
                Arguments.of("head_case_IGNORED_tail", "CASE_Ig", true),
                Arguments.of("Letter_missin", "Letter_missing", false));
    }

    @ParameterizedTest
    @MethodSource("source_defaultIfBlank")
    void test_defaultIfBlank(String value, String defaultValue, String expected) {
        assertThat(StringUtils.defaultIfBlank(value, defaultValue)).isEqualTo(expected);
    }

    static Stream<Arguments> source_defaultIfBlank() {
        return Stream.of(
                Arguments.of(null, "default", "default"),
                Arguments.of("", "default", "default"),
                Arguments.of("   ", "default", "default"),
                Arguments.of("actual", "default", "actual"),
                Arguments.of("  text  ", "default", "  text  "));
    }
}
