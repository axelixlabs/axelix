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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.util.List;

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
    List<CacheLookup> getHits();

    /**
     * @return the estimated number of cache misses.
     */
    List<CacheLookup> getMisses();
}
