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
package com.axelixlabs.axelix.common.domain.http;

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
