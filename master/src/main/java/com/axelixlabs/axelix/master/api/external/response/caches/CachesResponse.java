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
package com.axelixlabs.axelix.master.api.external.response.caches;

import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nullable;

/**
 * The profile contains a list of all cache managers in the application.
 *
 * @param cacheManagers    The list of cache managers.
 *
 * @author Sergey Cherkasov
 */
public record CachesResponse(List<CacheManagers> cacheManagers) {

    public CachesResponse() {
        this(Collections.emptyList());
    }

    /**
     * The profile contains a list of all caches in the application.
     *
     * @param name   The cache manager name.
     * @param caches The list of caches.
     *
     * @author Sergey Cherkasov
     */
    public record CacheManagers(String name, List<Caches> caches) {}

    /**
     * The profile contains details about the cache.
     *
     * @param name                  The cache name.
     * @param target                The fully qualified name of the native cache.
     * @param hitsCount             The number of cache hits, or {@code null} if unknown.
     * @param missesCount           The number of cache misses, or {@code null} if unknown.
     * @param estimatedEntrySize    The estimated number of entries in the cache, or {@code null} if unknown.
     * @param enabled               Whether the cache is enabled ({@code true}) or disabled ({@code false}).
     *
     * @author Sergey Cherkasov
     */
    public record Caches(
            String name,
            String target,
            @Nullable Long hitsCount,
            @Nullable Long missesCount,
            @Nullable Long estimatedEntrySize,
            boolean enabled) {}
}
