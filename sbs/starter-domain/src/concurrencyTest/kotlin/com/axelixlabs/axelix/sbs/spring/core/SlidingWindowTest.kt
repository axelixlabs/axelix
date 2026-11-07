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
package com.axelixlabs.axelix.sbs.spring.core

import org.jetbrains.lincheck.datastructures.ModelCheckingOptions
import org.jetbrains.lincheck.datastructures.Operation
import org.jetbrains.lincheck.datastructures.StressOptions
import org.jetbrains.lincheck.datastructures.Validate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lincheck tests for concurrent writes and weakly consistent reads.
 *
 * @author Mikhail Polivakha
 */
class SlidingWindowTest {

    @ParameterizedTest
    @ValueSource(classes = [StrictWithoutEviction::class, StrictWithEviction::class, BestEffort::class])
    fun modelChecking(scenario: Class<*>) = ModelCheckingOptions()
        .iterations(50)
        .invocationsPerIteration(100)
        .threads(3)
        .actorsPerThread(3)
        .actorsBefore(0)
        .actorsAfter(0)
        .check(scenario)

    @ParameterizedTest
    @ValueSource(classes = [StrictWithoutEviction::class, StrictWithEviction::class, BestEffort::class])
    fun stressTesting(scenario: Class<*>) = StressOptions()
        .iterations(30)
        .invocationsPerIteration(1000)
        .threads(3)
        .actorsPerThread(3)
        .actorsBefore(0)
        .actorsAfter(0)
        .check(scenario)

    // Nine operations can insert at most eighteen entries.
    class StrictWithoutEviction : Scenario(32, SlidingWindow.WriteMode.STRICT)

    class StrictWithEviction : Scenario(2, SlidingWindow.WriteMode.STRICT)

    class BestEffort : Scenario(2, SlidingWindow.WriteMode.BEST_EFFORT)

    abstract class Scenario(private val capacity: Int, private val mode: SlidingWindow.WriteMode) {
        private val slidingWindow = SlidingWindow<Int>(capacity, mode)
        private val pairs = AtomicInteger()

        @Operation
        fun putPair() {
            val first = pairs.getAndIncrement() * 2
            slidingWindow.put(first)
            slidingWindow.put(first + 1)
        }

        @Operation
        fun get() {
            validateEntries(slidingWindow.get())
        }

        @Validate
        fun validate() {
            val entries = slidingWindow.get()
            validateEntries(entries)
            if (mode == SlidingWindow.WriteMode.STRICT) {
                val inserted = pairs.get() * 2
                check(entries.size == minOf(capacity, inserted))
                if (inserted <= capacity) {
                    check(entries.sorted() == (0 until inserted).toList())
                }
                // A retained first write cannot outlive its later write once all writers finish.
                for (entry in entries.filter { it % 2 == 0 }) {
                    check(entry + 1 in entries)
                }
            }
        }

        private fun validateEntries(entries: List<Int>) {
            check(entries.size <= capacity)
            check(entries.distinct().size == entries.size)
            check(entries.all { it >= 0 && it < pairs.get() * 2 })
            for ((index, entry) in entries.withIndex()) {
                if (entry % 2 == 0) {
                    val successor = entries.indexOf(entry + 1)
                    check(successor == -1 || successor > index)
                }
            }
        }
    }
}
