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
package com.axelixlabs.axelix.master.service.discovery;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link WindowCompatibilityDetectionStrategy}.
 *
 * @author Mikhail Polivakha
 */
class WindowCompatibilityDetectionStrategyTest {

    private WindowCompatibilityDetectionStrategy subject;

    @BeforeEach
    void setUp() {
        // given.
        AxelixVersionDiscoverer masterVersionDiscoverer = () -> "1.5.0";
        subject = new WindowCompatibilityDetectionStrategy(masterVersionDiscoverer);
    }

    @ParameterizedTest
    @MethodSource("compatibleStarterVersions")
    void shouldTreatStarterAsCompatibleWhenInsideTheWindow(String starterVersion) {
        // when.
        boolean compatible = subject.isCompatible(starterVersion);

        // then.
        assertThat(compatible).isTrue();
    }

    @ParameterizedTest
    @MethodSource("incompatibleStarterVersions")
    void shouldTreatStarterAsIncompatibleWhenOutsideTheWindow(String starterVersion) {
        // when.
        boolean compatible = subject.isCompatible(starterVersion);

        // then.
        assertThat(compatible).isFalse();
    }

    private static Stream<String> compatibleStarterVersions() {
        return Stream.of("1.5.0", "1.5.0", "1.5.3", "1.4.0", "1.3.9", "1.2.0", "1.2.1");
    }

    private static Stream<String> incompatibleStarterVersions() {
        return Stream.of("1.1.0", "1.0.0", "1.6.0", "1.6.0-SNAPSHOT", "2.5.0", "0.9.0", "not-a-version");
    }
}
