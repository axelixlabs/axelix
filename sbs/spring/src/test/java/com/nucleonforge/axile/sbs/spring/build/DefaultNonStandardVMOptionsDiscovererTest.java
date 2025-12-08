/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.spring.build;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.domain.JvmNonStandardOption;
import com.nucleonforge.axile.common.domain.JvmNonStandardOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultNonStandardVMOptionsDiscoverer}.
 *
 * @since 25.08.2025
 * @author Nikita Kirillov
 */
class DefaultNonStandardVMOptionsDiscovererTest {

    private final DefaultNonStandardVMOptionsDiscoverer discoverer = new DefaultNonStandardVMOptionsDiscoverer();

    @Test
    void shouldReturnNonStandardVMOptions() {
        JvmNonStandardOptions options = discoverer.discover();

        Set<String> actualOptions = options.getNonStandardOptions().stream()
                .map(JvmNonStandardOption::option)
                .collect(Collectors.toSet());

        // Use `contains` instead of `containsOnly` because Gradle may add extra JVM options during the build.
        assertThat(actualOptions).contains("-Xms256m", "-Xmx512m", "-XX:+UseG1GC");
    }
}
