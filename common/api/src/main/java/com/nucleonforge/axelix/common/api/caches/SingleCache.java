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
