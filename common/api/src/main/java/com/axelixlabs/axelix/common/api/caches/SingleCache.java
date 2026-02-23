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

import java.util.List;
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
    private final List<CacheLookup> hits;
    private final List<CacheLookup> misses;

    @Nullable
    private final Long estimatedEntrySize;

    private final boolean enabled;

    /**
     * Creates a new SingleCache.
     *
     * @param name               The cache name.
     * @param target             The fully qualified name of the native cache.
     * @param cacheManager       The name of the cache manager that manages current cache.
     * @param hits               The array of all recorded cache hits, or empty if unknown.
     * @param misses             The array of all recorded cache misses, or empty if unknown.
     * @param estimatedEntrySize The estimated number of entries in the cache, or {@code null} if unknown.
     * @param enabled            Whether the cache is enabled ({@code true}) or disabled ({@code false}).
     */
    @JsonCreator
    public SingleCache(
            @JsonProperty("name") String name,
            @JsonProperty("target") String target,
            @JsonProperty("cacheManager") String cacheManager,
            @JsonProperty("hits") List<CacheLookup> hits,
            @JsonProperty("misses") List<CacheLookup> misses,
            @JsonProperty("estimatedEntrySize") @Nullable Long estimatedEntrySize,
            @JsonProperty("enabled") boolean enabled) {
        this.name = name;
        this.target = target;
        this.cacheManager = cacheManager;
        this.hits = hits;
        this.misses = misses;
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

    public List<CacheLookup> getHits() {
        return hits;
    }

    public List<CacheLookup> getMisses() {
        return misses;
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
                && Objects.equals(hits, that.hits)
                && Objects.equals(misses, that.misses)
                && Objects.equals(estimatedEntrySize, that.estimatedEntrySize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, target, cacheManager, hits, misses, estimatedEntrySize, enabled);
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
                + hits
                + ", missesCount="
                + misses
                + ", estimatedEntrySize="
                + estimatedEntrySize
                + ", enabled="
                + enabled
                + '}';
    }

    public static class CacheLookup {

        /**
         * Timestamp when the cache operation occurred, in milliseconds from unix epoch
         */
        private final long timestamp;

        @JsonCreator
        public CacheLookup(@JsonProperty("timestamp") long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheLookup that = (CacheLookup) o;
            return timestamp == that.timestamp;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(timestamp);
        }

        @Override
        public String toString() {
            return "CacheLookup{" + "timestamp=" + timestamp + '}';
        }
    }
}
