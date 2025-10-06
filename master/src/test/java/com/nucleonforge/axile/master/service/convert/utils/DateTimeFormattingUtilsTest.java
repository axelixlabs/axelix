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
