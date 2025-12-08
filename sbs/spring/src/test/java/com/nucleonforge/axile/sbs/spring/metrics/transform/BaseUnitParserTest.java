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
package com.nucleonforge.axile.sbs.spring.metrics.transform;

import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BytesMemoryBaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.KiloBytesMemoryBaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.MegabytesMemoryBaseUnit;

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
                of("kilobytes", Optional.of(KiloBytesMemoryBaseUnit.INSTANCE)),
                of("megabytes", Optional.of(MegabytesMemoryBaseUnit.INSTANCE)),
                of("classes", Optional.empty()));
    }
}
