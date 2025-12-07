package com.nucleonforge.axile.sbs.spring.metrics.transform;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
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
                of(1044, KiloBytesMemoryBaseUnit.INSTANCE, (double) 1044 / 1024),
                of(12024, KiloBytesMemoryBaseUnit.INSTANCE, (double) 12024 / 1024),
                of(1024 * 1024 * 6 * 1.2, MegabytesMemoryBaseUnit.INSTANCE, (double) 6 * 1.2));
    }
}
