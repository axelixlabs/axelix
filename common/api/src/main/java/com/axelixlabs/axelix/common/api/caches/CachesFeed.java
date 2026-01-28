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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response of the caches actuator endpoint contains a map of all cache managers in the application.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/caches.html">Caches Endpoint</a>
 *
 * @param cacheManagers The list of cache managers in the application.
 *
 * @author Sergey Cherkasov
 */
public record CachesFeed(@JsonProperty("cacheManagers") List<CacheManager> cacheManagers) {
    public CachesFeed() {
        this(Collections.emptyList());
    }

    /**
     * DTO that encapsulates a map of all caches inside the given cache manager.
     *
     * @param name   The cache manager name.
     * @param caches The caches are identified by the cache name.
     */
    public record CacheManager(@JsonProperty("name") String name, @JsonProperty("caches") List<Cache> caches) {}

    /**
     * DTO that encapsulates the full cache name.
     *
     * @param name                  The cache name.
     * @param target                The fully qualified name of the native cache.
     * @param hitsCount             The number of cache hits, or {@code null} if unknown.
     * @param missesCount           The number of cache misses, or {@code null} if unknown.
     * @param estimatedEntrySize    The estimated number of entries in the cache, or {@code null} if unknown.
     * @param enabled               Whether the cache is enabled ({@code true}) or disabled ({@code false}).
     */
    public record Cache(
            @JsonProperty("name") String name,
            @JsonProperty("target") String target,
            @JsonProperty("hitsCount") @Nullable Long hitsCount,
            @JsonProperty("missesCount") @Nullable Long missesCount,
            @JsonProperty("estimatedEntrySize") @Nullable Long estimatedEntrySize,
            @JsonProperty("enabled") boolean enabled) {}
}
