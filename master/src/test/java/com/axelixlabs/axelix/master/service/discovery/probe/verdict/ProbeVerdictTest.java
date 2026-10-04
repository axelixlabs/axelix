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
package com.axelixlabs.axelix.master.service.discovery.probe.verdict;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProbeVerdict}.
 *
 * @author Marsel Semenov
 */
class ProbeVerdictTest {

    @ParameterizedTest
    @CsvSource({
        "SUCCESS,   0, true",
        "TRANSIENT, 1, true",
        "TRANSIENT, 4, true",
        "TRANSIENT, 5, false",
        "THROTTLED, 4, true",
        "THROTTLED, 5, false",
        "PERMANENT, 1, false",
        "REJECTED,  1, false",
    })
    void keepsRegistration_shouldTolerateOnlyRecoverableFailuresBelowThreshold(
            ProbeVerdict verdict, int failures, boolean expected) {
        assertThat(verdict.keepsRegistration(failures, 5)).isEqualTo(expected);
    }
}
