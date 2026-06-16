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
package com.axelixlabs.maven.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ThymeleafVersionPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {"3.0.15.RELEASE", "3.1.4.RELEASE", "3.1.5-SNAPSHOT", "2.5.8", "3.1.0.RELEASE"})
    void treatsLowerOrPreReleaseVersionsAsBelowFloor(String version) {
        // given / when / then
        assertThat(ThymeleafVersionPolicy.isBelowFloor(version)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.1.5", "3.1.5.RELEASE", "3.1.6.RELEASE", "3.2.0", "4.0.0"})
    void treatsFloorOrHigherVersionsAsSatisfied(String version) {
        // given / when / then
        assertThat(ThymeleafVersionPolicy.isBelowFloor(version)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void treatsUnknownVersionAsBelowFloor(String version) {
        // given / when / then
        assertThat(ThymeleafVersionPolicy.isBelowFloor(version)).isTrue();
    }

    @Test
    void floorConstantIsResolvableThymeleafCoordinate() {
        // given / when / then
        assertThat(ThymeleafVersionPolicy.FLOOR).isEqualTo("3.1.5.RELEASE");
    }
}
