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
package com.nucleonforge.axelix.common.domain.http;

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
                // Basic Cases
                of("", List.of()),
                of("?q=java", List.of(new SingleValueQueryParameter("q", "java"))),

                // 1. Structural Conflicts (Must be escaped)
                of(
                        "?filter=price%3D100%26tax%3D20",
                        List.of(new SingleValueQueryParameter("filter", "price=100&tax=20"))),
                of("?search=What%3F", List.of(new SingleValueQueryParameter("search", "What?"))),
                of("?anchor=part%231", List.of(new SingleValueQueryParameter("anchor", "part#1"))),

                // 2. Spaces and Special Symbols
                of("?title=Salt%20%26%20Pepper", List.of(new SingleValueQueryParameter("title", "Salt & Pepper"))),
                of("?formula=1%2B1%3D2", List.of(new SingleValueQueryParameter("formula", "1+1=2"))),
                of("?path=C%3A%5CUsers", List.of(new SingleValueQueryParameter("path", "C:\\Users"))),

                // 3. Double Encoding Prevention
                of("?discount=100%25", List.of(new SingleValueQueryParameter("discount", "100%"))),

                // 4. Unicode & Emojis (Assuming UTF-8 encoding)
                of("?city=M%C3%BCnchen", List.of(new SingleValueQueryParameter("city", "München"))),
                of("?status=%F0%9F%9A%80", List.of(new SingleValueQueryParameter("status", "🚀"))),

                // 5. Unsafe / Control Characters
                of("?msg=Line1%0ALine2", List.of(new SingleValueQueryParameter("msg", "Line1\nLine2"))));
    }
}
