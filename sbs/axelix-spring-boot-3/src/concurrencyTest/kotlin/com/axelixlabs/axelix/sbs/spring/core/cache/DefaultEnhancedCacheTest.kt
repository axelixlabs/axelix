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
package com.axelixlabs.axelix.sbs.spring.core.cache

import org.jetbrains.lincheck.datastructures.ModelCheckingOptions
import org.jetbrains.lincheck.datastructures.Operation
import org.jetbrains.lincheck.datastructures.Param
import org.jetbrains.lincheck.datastructures.StressOptions
import org.jetbrains.lincheck.datastructures.StringGen
import org.jetbrains.lincheck.datastructures.verifier.LinearizabilityVerifier
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCache
import java.util.concurrent.Callable

/**
 * Lincheck test for [DefaultEnhancedCache]
 *
 * @author Mikhail Polivakha
 */
class DefaultEnhancedCacheTest {

    val delegate = DefaultEnhancedCache(ConcurrentMapCache("test-cache"))
    val subject =

        @Param(name = "value", gen = StringGen::class)
        @Param(name = "key", gen = StringGen::class, conf = "1:3")
        object : EnhancedCache by delegate {

        @Operation
        fun getValueWrapper(@Param(name = "key") key: Any) = delegate.get(key)

        @Operation
        fun getWithType(@Param(name = "key") key: Any) = delegate.get(key, String::class.java)

        @Operation
        fun getWithValueLoader(@Param(name = "key") key: Any) = delegate.get(key, Callable { "loaded value" })

        @Operation
        override fun put(@Param(name = "key") key: Any, @Param(name = "value") value: Any?) = delegate.put(key, value)

        @Operation
        override fun evict(@Param(name = "key") key: Any) = delegate.evict(key)

        @Operation
        override fun clear() = delegate.clear()

        @Operation
        override fun disable() = delegate.disable()

        @Operation
        override fun enable() = delegate.enable()

        @Operation
        override fun isEnabled() = delegate.isEnabled()
    }

    @Test
    fun test_StressTesting() {
        StressOptions()
            .verifier(LinearizabilityVerifier::class.java)
            .check(subject::class)
    }

    @Test
    fun test_ModelChecking() {
        ModelCheckingOptions().check(subject::class)
    }
}