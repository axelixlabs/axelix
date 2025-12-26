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
package com.nucleonforge.axile.sbs.spring.conditions;

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
