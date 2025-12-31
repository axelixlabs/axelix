/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.cache;

import org.springframework.cache.Cache;

/**
 * The enhanced variation of {@link Cache} to handle specific operations.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public interface EnhancedCache extends Cache {

    /**
     * Disable the given cache. By "disabling" we mean that
     * <ul>
     *   <li>Any {@code get} variation will return {@code null}</li>
     *   <li>Any {@code put} variation will actually be no-op, i.e.
     *   not put anything into underlying delegating cache</li>
     *   <li>The {@code evict/clear/invalidate} will also be no-op, i.e. will
     *   not anyhow affect the delegate cache</li>
     * </ul>
     *
     * All the other behavior (i.e. {@link #getNativeCache()}, {@link #getName()}) remains the same.
     */
    void disable();

    /**
     * Enable this cache. By "enabling" we mean to undo all the effects
     * caused by {@link #disable()} method invocation. If the cache is not disabled,
     * this calling this method is a no-op.
     */
    void enable();

    /**
     * @return {@code true} if this cache is enabled, {@code false} otherwise.
     */
    boolean isEnabled();

    /**
     * @return the estimated number of cache hits.
     */
    long getHitsCount();

    /**
     * @return the estimated number of cache misses.
     */
    long getMissesCount();
}
