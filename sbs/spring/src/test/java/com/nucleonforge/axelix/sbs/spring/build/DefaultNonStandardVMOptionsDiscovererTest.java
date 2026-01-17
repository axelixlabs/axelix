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
package com.nucleonforge.axelix.sbs.spring.build;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.domain.JvmNonStandardOption;
import com.nucleonforge.axelix.common.domain.JvmNonStandardOptions;

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
