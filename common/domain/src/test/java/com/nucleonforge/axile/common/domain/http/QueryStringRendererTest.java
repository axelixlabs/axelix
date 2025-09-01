package com.nucleonforge.axile.common.domain.http;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit tests for {@link QueryStringRenderer}.
 *
 * @author Mikhail Polivakha
 */
class QueryStringRendererTest {

    @MethodSource("args")
    @ParameterizedTest
    void testRenderQueryString(String expected, List<QueryParameter<?>> parameters) {
        assertThat(QueryStringRenderer.renderQueryString(parameters)).isEqualTo(expected);
    }

    public static Stream<Arguments> args() {
        return Stream.of(
                of("", List.of()),
                of("?key=value", List.of(new SingleValueQueryParameter("key", "value"))),
                of(
                        "?key1=value1&key2=value2",
                        List.of(
                                new SingleValueQueryParameter("key1", "value1"),
                                new SingleValueQueryParameter("key2", "value2"))));
    }
}
