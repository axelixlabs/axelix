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
package com.nucleonforge.axelix.master.service.convert.caches;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.api.caches.SingleCache;
import com.nucleonforge.axelix.master.api.response.caches.CacheProfileResponse;
import com.nucleonforge.axelix.master.service.convert.response.caches.SingleCacheConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SingleCacheConverter}
 *
 * @author Sergey Cherkasov
 */
public class SingleCacheConverterTest {
    private final SingleCacheConverter subject = new SingleCacheConverter();

    @Test
    void testConvertHappyPath() {
        SingleCache cache =
                new SingleCache("cities", "java.util.concurrent.ConcurrentHashMap", "cacheManager", 1L, 2L, 3L, true);

        // when
        CacheProfileResponse response = subject.convertInternal(cache);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("cities");
        assertThat(response.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(response.cacheManager()).isEqualTo("cacheManager");
    }
}
