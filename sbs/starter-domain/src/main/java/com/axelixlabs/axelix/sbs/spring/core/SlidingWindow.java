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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.jspecify.annotations.NullMarked;

/**
 * The sliding window implementation.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@NullMarked
public class SlidingWindow<E> {

    private final Deque<E> deque;
    private final int capacity;
    private final Object lock = new Object();

    public SlidingWindow(int capacity) {
        this.deque = new LinkedList<>();
        this.capacity = capacity;
    }

    public void put(E element) {
        synchronized (lock) {
            deque.offer(element);
            clear();
        }
    }

    public List<E> get() {
        synchronized (lock) {
            var copy = new LinkedList<>(deque);

            if (copy.size() > capacity) {
                return copy.subList(copy.size() - capacity, copy.size());
            }

            return copy;
        }
    }

    public void clear() {
        synchronized (lock) {
            int currentSize = deque.size();

            if (currentSize <= capacity) {
                return;
            }

            // We're not draining the queue till the 'capacity' size to avoid potential infinite loop
            int toRemove = currentSize - capacity;
            for (int i = 0; i < toRemove; i++) {
                if (deque.pollFirst() == null) {
                    break;
                }
            }
        }
    }
}
