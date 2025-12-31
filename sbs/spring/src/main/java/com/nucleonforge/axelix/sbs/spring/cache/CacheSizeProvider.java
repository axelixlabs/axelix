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
package com.nucleonforge.axelix.sbs.spring.cache;

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
