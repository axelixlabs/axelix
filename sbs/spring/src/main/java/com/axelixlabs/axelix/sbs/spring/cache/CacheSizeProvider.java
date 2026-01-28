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
package com.axelixlabs.axelix.sbs.spring.cache;

import io.micrometer.common.lang.Nullable;

/**
 * Interface for providing the estimate size of the provided cache (for example, Caffeine, ConcurrentHashMap, Guava, etc.)
 *
 * @author Sergey Cherkasov
 */
public interface CacheSizeProvider {

    /**
     * @param nativeCache the underlying native cache provider.
     * @return the estimated cache size, or {@code null} if the value cannot be determined, or if the provided cache is not supported.
     */
    @Nullable
    Long getEstimatedCacheSize(@Nullable Object nativeCache);
}
