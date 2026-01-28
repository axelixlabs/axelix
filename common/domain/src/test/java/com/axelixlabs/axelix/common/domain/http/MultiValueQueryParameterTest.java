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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit test for {@link MultiValueQueryParameter}.
 *
 * @author Mikhail Polivakha
 */
class MultiValueQueryParameterTest {

    @ParameterizedTest
    @MethodSource("args")
    void shouldRenderCorrectMultiValuesParameter(String key, List<String> values, String result) {
        // given.
        var subject = new MultiValueQueryParameter(key, values);

        // when.
        String rendered = subject.toEncodedString();

        // then.
        Assertions.assertThat(rendered).isEqualTo(result);
    }

    static Stream<Arguments> args() {
        return Stream.of(
                of("k1", List.of("v1"), "k1=v1"), // simple case
                of("k1", List.of("v1", "v2", "v3"), "k1=v1,v2,v3"), // multiple values
                of("k1", List.of("v1 v2", "v3"), "k1=v1%20v2,v3") // reserved characters in use
                );
    }
}
