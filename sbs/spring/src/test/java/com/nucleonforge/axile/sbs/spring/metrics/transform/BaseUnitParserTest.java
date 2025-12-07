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
