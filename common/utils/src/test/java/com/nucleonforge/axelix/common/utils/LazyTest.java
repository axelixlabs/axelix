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
package com.nucleonforge.axelix.common.utils;

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

    @Test
    void testRequiredLazyLiteral() {
        Lazy<String> value = Lazy.resolved("value");

        String first = value.required();

        Assertions.assertThat(first).isEqualTo("value");
    }

    @Test
    void testRequiredLazySupplier() {
        Lazy<String> value = Lazy.of(() -> "value");

        String first = value.required();

        Assertions.assertThat(first).isEqualTo("value");
    }
}
