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
package com.nucleonforge.axelix.common.api.transform;

import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.nucleonforge.axelix.common.api.transform.units.BaseUnit;
import com.nucleonforge.axelix.common.api.transform.units.BytesMemoryBaseUnit;
import com.nucleonforge.axelix.common.api.transform.units.GigabytesMemoryBaseUnit;
import com.nucleonforge.axelix.common.api.transform.units.KilobytesMemoryBaseUnit;
import com.nucleonforge.axelix.common.api.transform.units.MegabytesMemoryBaseUnit;

import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit tests for {@link BaseUnitParser}.
 *
 * @author Mikhail Polivakha
 */
class BaseUnitParserTest {

    private BaseUnitParser subject;

    @BeforeEach
    void setUp() {
        subject = new BaseUnitParser();
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void shouldParseBaseUnitCorrectly(String input, Optional<BaseUnit> expected) {
        Assertions.assertThat(subject.parse(input)).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                of("bytes", Optional.of(BytesMemoryBaseUnit.INSTANCE)),
                of("kilobytes", Optional.of(KilobytesMemoryBaseUnit.INSTANCE)),
                of("megabytes", Optional.of(MegabytesMemoryBaseUnit.INSTANCE)),
                of("gigabytes", Optional.of(GigabytesMemoryBaseUnit.INSTANCE)),
                of("classes", Optional.empty()));
    }
}
