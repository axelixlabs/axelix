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
package com.axelixlabs.axelix.master.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JavaVersion}.
 *
 * @author Mikhail Polivakha
 */
class JavaVersionTest {

    @ParameterizedTest
    @CsvSource({
        "25,        25, 0, 0, 0",
        "21,        21, 0, 0, 0",
        "21.0.2,    21, 0, 2, 0",
        "17.0.14,   17, 0, 14, 0",
        "21.0.2.1,  21, 0, 2, 1",
    })
    void shouldParseJep322VersionsIntoComponents(String reported, int feature, int interim, int update, int patch) {
        assertThat(JavaVersion.parse(reported)).isEqualTo(new JavaVersion(feature, interim, update, patch));
    }

    @Test
    void shouldRejectMalformedVersion() {
        assertThatThrownBy(() -> JavaVersion.parse("not-a-version")).isInstanceOf(IllegalArgumentException.class);
    }
}
