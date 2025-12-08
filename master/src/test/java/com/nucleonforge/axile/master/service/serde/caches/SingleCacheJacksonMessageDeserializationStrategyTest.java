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
package com.nucleonforge.axile.master.service.serde.caches;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.SingleCache;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SingleCacheJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class SingleCacheJacksonMessageDeserializationStrategyTest {
    private final SingleCacheJacksonMessageDeserializationStrategy subject =
            new SingleCacheJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeSingleCache() {
        // language=json
        String response =
                """
        {
          "target" : "java.util.concurrent.ConcurrentHashMap",
          "name" : "cities",
          "cacheManager" : "cacheManager"
        }
        """;

        // when.
        SingleCache cache = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then
        assertThat(cache.name()).isEqualTo("cities");
        assertThat(cache.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(cache.cacheManager()).isEqualTo("cacheManager");
    }
}
