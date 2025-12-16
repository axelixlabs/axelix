package com.nucleonforge.axile.common.domain.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Lazy}.
 *
 * @author Mikhail Polivakha
 */
class LazyTest {

    @Test
    void testAlreadyResolved() {
        Lazy<String> value = Lazy.resolved("value");

        String first = value.get();
        String second = value.get();

        Assertions.assertThat(first).isEqualTo(second);
    }

    @Test
    void testFromSupplier() {
        Lazy<String> value = Lazy.of(() -> "value");

        String first = value.get();
        String second = value.get();

        Assertions.assertThat(first).isEqualTo(second);
    }
}
