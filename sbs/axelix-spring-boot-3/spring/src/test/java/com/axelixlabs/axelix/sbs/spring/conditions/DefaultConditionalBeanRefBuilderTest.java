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
package com.axelixlabs.axelix.sbs.spring.conditions;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.test.context.ContextCustomizer;

import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit tests for {@link DefaultConditionalBeanRefBuilder}.
 *
 * @author Mikhail Polivakha
 */
class DefaultConditionalBeanRefBuilderTest {

    private DefaultConditionalBeanRefBuilder subject;

    @BeforeEach
    void setUp() {
        subject = new DefaultConditionalBeanRefBuilder();
    }

    @ParameterizedTest
    @MethodSource(value = "args")
    void shouldBuildBeanRef(Class<?> clazz, String methodName, String expectedBeanRef) {

        // when.
        String beanRef = subject.buildBeanRefInternal(clazz, methodName);

        // then.
        Assertions.assertThat(beanRef).isEqualTo(expectedBeanRef);
    }

    static Stream<Arguments> args() {
        return Stream.of(
                of(TaskExecutionAutoConfiguration.class, null, "TaskExecutionAutoConfiguration"),
                of(
                        TaskExecutionAutoConfiguration.class,
                        "taskExecutorBuilder",
                        "TaskExecutionAutoConfiguration#taskExecutorBuilder"),
                of(ContextCustomizer.class, null, ContextCustomizer.class.getSimpleName()),
                of(
                        NestedClass.DeeplyNestedClass.class,
                        null,
                        "DefaultConditionalBeanRefBuilderTest.NestedClass.DeeplyNestedClass"),
                of(
                        NestedClass.DeeplyNestedClass.class,
                        "method",
                        "DefaultConditionalBeanRefBuilderTest.NestedClass.DeeplyNestedClass#method"));
    }

    static class NestedClass {

        static class DeeplyNestedClass {}
    }
}
