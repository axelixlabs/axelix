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

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SlidingWindow}.
 *
 * @author Mikhail Polivakha
 */
class SlidingWindowTest {

    private static final int CAPACITY = 5;
    private static final Duration CLEANUP_INTERVAL = Duration.ofSeconds(1);

    private SlidingWindow<String> slidingWindow;

    @BeforeEach
    void setUp() {
        slidingWindow = new SlidingWindow<>(CAPACITY, CLEANUP_INTERVAL);
    }

    @Test
    void put_shouldAddElementToWindow() {
        // given.
        String element = "test";

        // when.
        slidingWindow.put(element);

        // then.
        List<String> result = slidingWindow.get();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("test");
    }

    @Test
    void get_shouldReturnEmptyListWhenWindowIsEmpty() {
        // given.
        // window is empty

        // when.
        List<String> result = slidingWindow.get();

        // then.
        assertThat(result).isEmpty();
    }

    @Test
    void put_shouldAddMultipleElementsInTheExactOrder() {
        // given.
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

    @Test
    void get_shouldReturnAllElementsWhenSizeIsLessThanCapacity() {
        // given.
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

    @Test
    void get_shouldReturnOnlyLastCapacityElementsWhenSizeExceedsCapacity() {
        // given.
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

    @Test
    void clear_shouldNotRemoveElementsWhenSizeIsLessThanOrEqualToCapacity() {
        // given.
        slidingWindow.put("element1");
        slidingWindow.put("element2");
        slidingWindow.put("element3");

        // when.
        slidingWindow.clear();

        // then.
        List<String> result = slidingWindow.get();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("element1", "element2", "element3");
    }

    @Test
    void clear_shouldRemoveExcessElementsWhenSizeExceedsCapacity() {
        // given.
        int elementsToAdd = CAPACITY + 3;
        for (int i = 0; i < elementsToAdd; i++) {
            slidingWindow.put("element" + i);
        }

        // when.
        slidingWindow.clear();

        // then.
        List<String> result = slidingWindow.get();
        assertThat(result).hasSize(CAPACITY);
        assertThat(result).containsExactly("element3", "element4", "element5", "element6", "element7");
    }

    @Test
    void clear_shouldRemoveOldestElementsFirst() {
        // given.
        for (int i = 0; i < CAPACITY + 1; i++) {
            slidingWindow.put("element" + i);
        }

        // when.
        slidingWindow.clear();

        // then.
        List<String> result = slidingWindow.get();
        assertThat(result).hasSize(CAPACITY);
        assertThat(result).doesNotContain("element0");
        assertThat(result).contains("element1");
        assertThat(result).contains("element5");
    }

    @Test
    void get_shouldReturnNewListInstance() {
        // given.
        slidingWindow.put("element1");
        slidingWindow.put("element2");

        // when.
        List<String> result1 = slidingWindow.get();
        List<String> result2 = slidingWindow.get();

        // then.
        assertThat(result1).isNotSameAs(result2);
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void clear_shouldHandleEmptyWindow() {
        // given.
        // window is empty

        // when.
        slidingWindow.clear();

        // then.
        List<String> result = slidingWindow.get();
        assertThat(result).isEmpty();
    }
}
