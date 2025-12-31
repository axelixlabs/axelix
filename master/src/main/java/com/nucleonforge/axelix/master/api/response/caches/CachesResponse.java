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
package com.nucleonforge.axelix.master.api.response.caches;

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
