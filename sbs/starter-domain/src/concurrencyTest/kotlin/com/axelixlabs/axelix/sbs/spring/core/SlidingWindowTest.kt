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
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lincheck test for [SlidingWindow].
 *
 * @author Mikhail Polivakha
 */
class SlidingWindowTest {

    private val slidingWindow = SlidingWindow<Int>(2)
    private val inserted = AtomicInteger()

    @Operation
    fun put() {
        slidingWindow.put(inserted.getAndIncrement())
    }

    @Operation
    fun get() {
        val entries = slidingWindow.get()
        check(entries.size <= 2)
        check(entries.distinct().size == entries.size)
        check(entries.all { it >= 0 && it < inserted.get() })
    }

    @Validate
    fun validate() {
        val entries = slidingWindow.get()
        check(entries.size == minOf(2, inserted.get()))
        check(entries.distinct().size == entries.size)
    }

    @Test
    fun test_ModelChecking() = ModelCheckingOptions().iterations(50).invocationsPerIteration(100).check(SlidingWindowTest::class.java)

    @Test
    fun test_StressTesting() = StressOptions().iterations(30).invocationsPerIteration(1000).check(SlidingWindowTest::class.java)
}
