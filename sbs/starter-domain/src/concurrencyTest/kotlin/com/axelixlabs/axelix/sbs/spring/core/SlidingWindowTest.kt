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

import org.jetbrains.lincheck.datastructures.Operation
import org.jetbrains.lincheck.datastructures.Param
import org.jetbrains.lincheck.datastructures.StressOptions
import org.jetbrains.lincheck.datastructures.StringGen
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * Lincheck test for [SlidingWindow].
 *
 * @author Mikhail Polivakha
 */
@Param(name = "element", gen = StringGen::class)
class SlidingWindowTest {

    private val slidingWindow = SlidingWindow<String>(5)

    @Operation
    fun put(@Param(name = "element") element: String) = slidingWindow.put(element)

    @Operation
    fun get() = slidingWindow.get().size

    @Operation
    fun clear() = slidingWindow.clear()

    @Test
    fun test_StressTesting() = StressOptions().check(SlidingWindowTest::class.java)
}
