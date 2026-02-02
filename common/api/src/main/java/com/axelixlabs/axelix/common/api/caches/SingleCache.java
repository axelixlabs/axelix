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
package com.axelixlabs.axelix.common.api.caches;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the details of the requested cache.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public final class SingleCache {

    private final String name;
    private final String target;
    private final String cacheManager;

    @Nullable
    private final Long hitsCount;

    @Nullable
    private final Long missesCount;

    @Nullable
    private final Long estimatedEntrySize;

    private final boolean enabled;

    /**
     * Creates a new SingleCache.
     *
     * @param name               The cache name.
     * @param target             The fully qualified name of the native cache.
     * @param cacheManager       The name of the cache manager that manages current cache.
     * @param hitsCount          The estimated number of cache hits, or {@code null} if unknown.
     * @param missesCount        The estimated number of cache misses, or {@code null} if unknown.
     * @param estimatedEntrySize The estimated number of entries in the cache, or {@code null} if unknown.
     * @param enabled            Whether the cache is enabled ({@code true}) or disabled ({@code false}).
     */
    @JsonCreator
    public SingleCache(
            @JsonProperty("name") String name,
            @JsonProperty("target") String target,
            @JsonProperty("cacheManager") String cacheManager,
            @JsonProperty("hitsCount") @Nullable Long hitsCount,
            @JsonProperty("missesCount") @Nullable Long missesCount,
            @JsonProperty("estimatedEntrySize") @Nullable Long estimatedEntrySize,
            @JsonProperty("enabled") boolean enabled) {
        this.name = name;
        this.target = target;
        this.cacheManager = cacheManager;
        this.hitsCount = hitsCount;
        this.missesCount = missesCount;
        this.estimatedEntrySize = estimatedEntrySize;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getTarget() {
        return target;
    }

    public String getCacheManager() {
        return cacheManager;
    }

    @Nullable
    public Long getHitsCount() {
        return hitsCount;
    }

    @Nullable
    public Long getMissesCount() {
        return missesCount;
    }

    @Nullable
    public Long getEstimatedEntrySize() {
        return estimatedEntrySize;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SingleCache that = (SingleCache) o;
        return enabled == that.enabled
                && Objects.equals(name, that.name)
                && Objects.equals(target, that.target)
                && Objects.equals(cacheManager, that.cacheManager)
                && Objects.equals(hitsCount, that.hitsCount)
                && Objects.equals(missesCount, that.missesCount)
                && Objects.equals(estimatedEntrySize, that.estimatedEntrySize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, target, cacheManager, hitsCount, missesCount, estimatedEntrySize, enabled);
    }

    @Override
    public String toString() {
        return "SingleCache{"
                + "name='"
                + name
                + '\''
                + ", target='"
                + target
                + '\''
                + ", cacheManager='"
                + cacheManager
                + '\''
                + ", hitsCount="
                + hitsCount
                + ", missesCount="
                + missesCount
                + ", estimatedEntrySize="
                + estimatedEntrySize
                + ", enabled="
                + enabled
                + '}';
    }
}
