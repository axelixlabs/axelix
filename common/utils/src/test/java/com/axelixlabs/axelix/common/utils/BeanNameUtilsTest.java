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
package com.axelixlabs.axelix.common.utils;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit tests for {@link BeanNameUtils}.
 *
 * @author Sergey Cherkasov
 */
class BeanNameUtilsTest {

    @MethodSource("stripConfigPropsPrefix")
    @ParameterizedTest
    void shouldStripConfigPropsPrefix(String beanName, String expected) {
        // when.
        String result = BeanNameUtils.stripConfigPropsPrefix(beanName);

        // then.
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> stripConfigPropsPrefix() {
        return Stream.of(
                of("-com.example.MyConfigProps", "com.example.MyConfigProps"),
                of("this.prefix-com.example.MyConfigProps", "com.example.MyConfigProps"),
                of("prefix-com.example.MyConfigProps", "com.example.MyConfigProps"),
                of("prefix-$com.example.MyConfigProps", "$com.example.MyConfigProps"),
                of("com.example.MyConfigProps", "com.example.MyConfigProps"));
    }
}
