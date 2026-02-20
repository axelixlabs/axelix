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
 * The response of the caches actuator endpoint contains a map of all cache managers in the application.
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/caches.html">Caches Endpoint</a>
 *
 * @author Sergey Cherkasov
 */
public final class CachesFeed {

    private final List<CacheManager> cacheManagers;

    /**
     * Creates a new CachesFeed.
     *
     * @param cacheManagers The list of cache managers in the application.
     */
    @JsonCreator
    public CachesFeed(@JsonProperty("cacheManagers") List<CacheManager> cacheManagers) {
        this.cacheManagers = cacheManagers;
    }

    public List<CacheManager> getCacheManagers() {
        return cacheManagers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CachesFeed that = (CachesFeed) o;
        return Objects.equals(cacheManagers, that.cacheManagers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cacheManagers);
    }

    @Override
    public String toString() {
        return "CachesFeed{" + "cacheManagers=" + cacheManagers + '}';
    }

    /**
     * DTO that encapsulates a map of all caches inside the given cache manager.
     */
    public static final class CacheManager {

        private final String name;
        private final List<Cache> caches;

        /**
         * Creates a new CacheManager.
         *
         * @param name   The cache manager name.
         * @param caches The caches are identified by the cache name.
         */
        @JsonCreator
        public CacheManager(@JsonProperty("name") String name, @JsonProperty("caches") List<Cache> caches) {
            this.name = name;
            this.caches = caches;
        }

        public String getName() {
            return name;
        }

        public List<Cache> getCaches() {
            return caches;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheManager that = (CacheManager) o;
            return Objects.equals(name, that.name) && Objects.equals(caches, that.caches);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, caches);
        }

        @Override
        public String toString() {
            return "CacheManager{" + "name='" + name + '\'' + ", caches=" + caches + '}';
        }
    }

    /**
     * DTO that encapsulates the full cache name.
     */
    public static final class Cache {

        private final String name;
        private final String target;

        @Nullable
        private final Long estimatedEntrySize;

        private final boolean enabled;

        /**
         * Creates a new Cache.
         *
         * @param name               The cache name.
         * @param target             The fully qualified name of the native cache.
         * @param estimatedEntrySize The estimated number of entries in the cache, or {@code null} if unknown.
         * @param enabled            Whether the cache is enabled ({@code true}) or disabled ({@code false}).
         */
        @JsonCreator
        public Cache(
                @JsonProperty("name") String name,
                @JsonProperty("target") String target,
                @JsonProperty("estimatedEntrySize") @Nullable Long estimatedEntrySize,
                @JsonProperty("enabled") boolean enabled) {
            this.name = name;
            this.target = target;
            this.estimatedEntrySize = estimatedEntrySize;
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public String getTarget() {
            return target;
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
            Cache cache = (Cache) o;
            return enabled == cache.enabled
                    && Objects.equals(name, cache.name)
                    && Objects.equals(target, cache.target)
                    && Objects.equals(estimatedEntrySize, cache.estimatedEntrySize);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, target, estimatedEntrySize, enabled);
        }

        @Override
        public String toString() {
            return "Cache{"
                    + "name='"
                    + name
                    + '\''
                    + ", target='"
                    + target
                    + '\''
                    + ", estimatedEntrySize="
                    + estimatedEntrySize
                    + ", enabled="
                    + enabled
                    + '}';
        }
    }
}
