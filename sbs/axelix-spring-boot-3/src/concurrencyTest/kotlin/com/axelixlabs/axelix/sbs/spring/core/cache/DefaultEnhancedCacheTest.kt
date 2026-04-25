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

import java.util.concurrent.Callable
import org.jetbrains.lincheck.datastructures.*
import org.jetbrains.lincheck.datastructures.verifier.LinearizabilityVerifier
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCache

/**
 * Lincheck test for [DefaultEnhancedCache].
 *
 * @author Mikhail Polivakha
 */
@Param(name = "value", gen = StringGen::class)
@Param(name = "key", gen = StringGen::class, conf = "1:3")
class DefaultEnhancedCacheTest {

  val delegate = DefaultEnhancedCache(ConcurrentMapCache("test-cache"))

  @Operation fun getValueWrapper(@Param(name = "key") key: Any) = delegate.get(key)

  @Operation fun getWithType(@Param(name = "key") key: Any) = delegate.get(key, String::class.java)

  @Operation
  fun getWithValueLoader(@Param(name = "key") key: Any) =
      delegate.get(key, Callable { "loaded value" })

  @Operation
  fun put(@Param(name = "key") key: Any, @Param(name = "value") value: Any?) =
      delegate.put(key, value)

  @Operation fun evict(@Param(name = "key") key: Any) = delegate.evict(key)

  @Operation fun clear() = delegate.clear()

  @Operation fun disable() = delegate.disable()

  @Operation fun enable() = delegate.enable()

  @Operation fun isEnabled() = delegate.isEnabled()

  @Test
  fun test_StressTesting() {
    StressOptions()
        .verifier(LinearizabilityVerifier::class.java)
        .check(DefaultEnhancedCacheTest::class.java)
  }

  @Test
  fun test_ModelChecking() {
    ModelCheckingOptions().check(DefaultEnhancedCacheTest::class.java)
  }
}
