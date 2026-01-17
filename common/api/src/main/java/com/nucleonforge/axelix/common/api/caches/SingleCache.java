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
package com.nucleonforge.axelix.common.api.caches;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the details of the requested cache.
 *
 * @param name                The cache name.
 * @param target              The fully qualified name of the native cache.
 * @param cacheManager        The name of the cache manager that manages current cache.
 * @param hitsCount           The estimated number of cache hits, or {@code null} if unknown.
 * @param missesCount         The estimated number of cache misses, or {@code null} if unknown.
 * @param estimatedEntrySize  The estimated number of entries in the cache, or {@code null} if unknown.
 * @param enabled             Whether the cache is enabled ({@code true}) or disabled ({@code false}).
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public record SingleCache(
        @JsonProperty("name") String name,
        @JsonProperty("target") String target,
        @JsonProperty("cacheManager") String cacheManager,
        @JsonProperty("hitsCount") @Nullable Long hitsCount,
        @JsonProperty("missesCount") @Nullable Long missesCount,
        @JsonProperty("estimatedEntrySize") @Nullable Long estimatedEntrySize,
        @JsonProperty("enabled") boolean enabled) {}
