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
package com.nucleonforge.axelix.common.api.caches;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;

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
public record CachesFeed(@JsonProperty("cacheManagers") List<CacheManagers> cacheManagers) {
    public CachesFeed() {
        this(Collections.emptyList());
    }

    /**
     * DTO that encapsulates a map of all caches in the cache manager.
     *
     * @param name   The cache manager name.
     * @param caches The caches are identified by the cache name.
     */
    public record CacheManagers(@JsonProperty("name") String name, @JsonProperty("caches") List<Caches> caches) {}

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
    public record Caches(
            @JsonProperty("name") String name,
            @JsonProperty("target") String target,
            @JsonProperty("hitsCount") @Nullable Long hitsCount,
            @JsonProperty("missesCount") @Nullable Long missesCount,
            @JsonProperty("estimatedEntrySize") @Nullable Long estimatedEntrySize,
            @JsonProperty("enabled") boolean enabled) {}
}
