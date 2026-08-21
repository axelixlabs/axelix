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
package com.axelixlabs.axelix.common.api.transform;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.api.transform.units.BaseUnit;
import com.axelixlabs.axelix.common.api.transform.units.BytesMemoryBaseUnit;
import com.axelixlabs.axelix.common.api.transform.units.KilobytesMemoryBaseUnit;
import com.axelixlabs.axelix.common.api.transform.units.MegabytesMemoryBaseUnit;

import static org.junit.jupiter.params.provider.Arguments.of;

/**
 * Unit test for {@link BytesMemoryBaseUnitValueTransformer}.
 *
 * @author Mikhail Polivakha
 */
class BytesMemoryBaseUnitValueTransformerTest {

    private BytesMemoryBaseUnitValueTransformer subject;

    @BeforeEach
    void setUp() {
        subject = new BytesMemoryBaseUnitValueTransformer();
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void shouldTransformByteValue(double value, BaseUnit expectedBaseUnit, double expectedValue) {
        TransformedMetricValue result = subject.transform(value);

        Assertions.assertThat(result.baseUnit()).isEqualTo(expectedBaseUnit);
        Assertions.assertThat(result.value()).isCloseTo(expectedValue, Percentage.withPercentage(1));
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                of(121, BytesMemoryBaseUnit.INSTANCE, 121),
                of(1044, KilobytesMemoryBaseUnit.INSTANCE, (double) 1044 / 1024),
                of(12024, KilobytesMemoryBaseUnit.INSTANCE, (double) 12024 / 1024),
                of(1024 * 1024 * 6 * 1.2, MegabytesMemoryBaseUnit.INSTANCE, (double) 6 * 1.2));
    }
}
