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
package com.axelixlabs.axelix.sbs.spring.core;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow.WriteMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SlidingWindow}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
class SlidingWindowTest {

    private static final int CAPACITY = 5;

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void put_shouldAddElementToWindow(WriteMode mode) {
        // given.
        SlidingWindow<String> slidingWindow = new SlidingWindow<>(CAPACITY, mode);
        String element = "test";

        // when.
        slidingWindow.put(element);

        // then.
        List<String> result = slidingWindow.get();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("test");
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void get_shouldReturnEmptyListWhenWindowIsEmpty(WriteMode mode) {
        // given.
        SlidingWindow<String> slidingWindow = new SlidingWindow<>(CAPACITY, mode);
        // window is empty

        // when.
        List<String> result = slidingWindow.get();

        // then.
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void put_shouldAddMultipleElementsInTheExactOrder(WriteMode mode) {
        // given.
        SlidingWindow<String> slidingWindow = new SlidingWindow<>(CAPACITY, mode);
        String element1 = "element1";
        String element2 = "element2";
        String element3 = "element3";

        // when.
        slidingWindow.put(element1);
        slidingWindow.put(element2);
        slidingWindow.put(element3);

        // then.
        List<String> result = slidingWindow.get();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("element1", "element2", "element3");
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void get_shouldReturnAllElementsWhenSizeIsLessThanCapacity(WriteMode mode) {
        // given.
        SlidingWindow<String> slidingWindow = new SlidingWindow<>(CAPACITY, mode);
        int elementsToAdd = CAPACITY - 1;
        for (int i = 0; i < elementsToAdd; i++) {
            slidingWindow.put("element" + i);
        }

        // when.
        List<String> result = slidingWindow.get();

        // then.
        assertThat(result).hasSize(elementsToAdd);
        assertThat(result).containsExactly("element0", "element1", "element2", "element3");
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void get_shouldReturnOnlyLastCapacityElementsWhenSizeExceedsCapacity(WriteMode mode) {
        // given.
        SlidingWindow<String> slidingWindow = new SlidingWindow<>(CAPACITY, mode);
        int elementsToAdd = CAPACITY + 3;
        for (int i = 0; i < elementsToAdd; i++) {
            slidingWindow.put("element" + i);
        }

        // when.
        List<String> result = slidingWindow.get();

        // then.
        assertThat(result).hasSize(CAPACITY);
        assertThat(result).containsExactly("element3", "element4", "element5", "element6", "element7");
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void get_shouldReturnNewListInstance(WriteMode mode) {
        // given.
        SlidingWindow<String> slidingWindow = new SlidingWindow<>(CAPACITY, mode);
        slidingWindow.put("element1");
        slidingWindow.put("element2");

        // when.
        List<String> result1 = slidingWindow.get();
        List<String> result2 = slidingWindow.get();

        // then.
        assertThat(result1).isNotSameAs(result2);
        assertThat(result1).isEqualTo(result2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void constructor_shouldRejectNonPositiveCapacity(int capacity) {
        // when / then.
        assertThatThrownBy(() -> new SlidingWindow<>(capacity)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void put_shouldKeepOnlyLatestElementWhenCapacityIsOne(WriteMode mode) {
        // given.
        SlidingWindow<String> window = new SlidingWindow<>(1, mode);

        // when.
        window.put("first");
        window.put("second");

        // then.
        assertThat(window.get()).containsExactly("second");
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void get_shouldReturnIndependentCopy(WriteMode mode) {
        // given.
        SlidingWindow<String> slidingWindow = new SlidingWindow<>(CAPACITY, mode);
        slidingWindow.put("first");
        List<String> snapshot = slidingWindow.get();

        // when.
        for (int i = 0; i < CAPACITY * 3; i++) {
            slidingWindow.put("next" + i);
        }

        // then.
        assertThat(snapshot).containsExactly("first");
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void constructor_shouldRejectNonPositiveCapacityWithWriteMode(WriteMode mode) {
        // when / then.
        assertThatThrownBy(() -> new SlidingWindow<>(0, mode)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(WriteMode.class)
    void put_shouldKeepRepeatedValues(WriteMode mode) {
        // given.
        SlidingWindow<String> window = new SlidingWindow<>(CAPACITY, mode);

        // when.
        window.put("same");
        window.put("same");

        // then.
        assertThat(window.get()).containsExactly("same", "same");
    }
}
