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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A fixed-capacity, non-blocking history with configurable write guarantees.
 * Supports at most {@link Long#MAX_VALUE} insertions per window.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@NullMarked
public class SlidingWindow<E> {

    private final AtomicReferenceArray<@Nullable Entry<E>> buffer;
    private final AtomicLong nextSequence = new AtomicLong();
    private final int capacity;
    private final WriteMode writeMode;

    public enum WriteMode {
        /** Prevents delayed writers from overwriting newer entries. */
        STRICT,
        /** Allows delayed writers to overwrite newer entries. */
        BEST_EFFORT
    }

    /**
     * Creates a window with strict writes.
     *
     * @param capacity the maximum number of elements to keep.
     */
    public SlidingWindow(int capacity) {
        this(capacity, WriteMode.STRICT);
    }

    /**
     * @param capacity the maximum number of elements to keep.
     * @param writeMode the insertion guarantees.
     */
    public SlidingWindow(int capacity, WriteMode writeMode) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.writeMode = writeMode;
        this.buffer = new AtomicReferenceArray<>(capacity);
    }

    /**
     * Adds an element, replacing the oldest one when the window is full.
     * Concurrent insertions are ordered by their assigned sequence numbers.
     */
    public void put(E element) {
        long sequence = nextSequence.getAndIncrement();
        int slot = (int) (sequence % capacity);
        Entry<E> update = new Entry<>(sequence, element);

        if (writeMode == WriteMode.BEST_EFFORT) {
            buffer.set(slot, update);
            return;
        }

        while (true) {
            Entry<E> current = buffer.get(slot);
            if (current != null && current.sequence > sequence) {
                return;
            }
            if (buffer.compareAndSet(slot, current, update)) {
                return;
            }
        }
    }

    /**
     * Returns a weakly consistent copy of up to capacity elements in insertion order.
     * Best-effort writes may leave gaps even after writers finish.
     */
    public List<E> get() {
        long end = nextSequence.get();
        long start = Math.max(0, end - capacity);
        List<E> result = new ArrayList<>((int) (end - start));
        for (long sequence = start; sequence < end; sequence++) {
            Entry<E> entry = buffer.get((int) (sequence % capacity));
            if (entry != null && entry.sequence == sequence) {
                result.add(entry.value);
            }
        }
        return result;
    }

    private static final class Entry<E> {
        private final long sequence;
        private final E value;

        private Entry(long sequence, E value) {
            this.sequence = sequence;
            this.value = value;
        }
    }
}
