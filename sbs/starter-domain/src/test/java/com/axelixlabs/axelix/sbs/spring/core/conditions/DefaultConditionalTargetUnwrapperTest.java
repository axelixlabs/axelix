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
package com.axelixlabs.axelix.sbs.spring.core.conditions;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit tests for {@link DefaultConditionalTargetUnwrapper}.
 *
 * @author Sergey Cherkasov
 */
class DefaultConditionalTargetUnwrapperTest {

    private DefaultConditionalTargetUnwrapper subject = new DefaultConditionalTargetUnwrapper();

    @ParameterizedTest
    @MethodSource("unwrapArgs")
    void shouldUnwrapTarget(String target, String className, String methodName) {
        // when.
        UnwrappedTarget unwrappedTarget = subject.unwrap(target);

        // then.
        assertThat(unwrappedTarget.getClassName()).isEqualTo(className);
        assertThat(unwrappedTarget.getMethodName()).isEqualTo(methodName);
    }

    private static Stream<Arguments> unwrapArgs() {
        return Stream.of(
                of("com.example.ClassName#MethodName", "com.example.ClassName", "MethodName"),
                of("com.example.ClassName", "com.example.ClassName", null));
    }
}
