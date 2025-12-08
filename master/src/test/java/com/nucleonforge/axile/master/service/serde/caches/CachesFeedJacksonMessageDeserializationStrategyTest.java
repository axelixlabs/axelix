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
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.CachesFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceCachesJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class CachesFeedJacksonMessageDeserializationStrategyTest {
    private final ServiceCachesJacksonMessageDeserializationStrategy subject =
            new ServiceCachesJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeServiceCaches() {
        // language=json
        String response =
                """
        {
          "cacheManagers" : [
            {
              "name": "anotherCacheManager",
              "caches": [
                {
                  "name": "countries",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "enabled": false
                }
              ]
            },
            {
            "name": "cacheManager",
              "caches": [
                {
                  "name": "cities",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "enabled": false
                },
                {
                  "name": "countries",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "enabled": false
                }
              ]
            }
          ]
        }
        """;

        // when.
        CachesFeed caches = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // ServiceCaches -> CacheManagers
        List<CachesFeed.CacheManagers> cacheManagersList = caches.cacheManagers();
        assertThat(cacheManagersList).hasSize(2);

        CachesFeed.CacheManagers another = cacheManagersList.stream()
                .filter(cm -> "anotherCacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(another.caches()).hasSize(1);

        CachesFeed.CacheManagers main = cacheManagersList.stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(main.caches()).hasSize(2);

        // "anotherCacheManager" -> Caches -> "countries"
        assertThat(another.caches()).hasSize(1);
        CachesFeed.Caches anotherCountries = another.caches().get(0);
        assertThat(anotherCountries.name()).isEqualTo("countries");
        assertThat(anotherCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(anotherCountries.enabled()).isFalse();

        // "cacheManager" -> Caches -> "countries"
        CachesFeed.Caches mainCountries = main.caches().stream()
                .filter(c -> "countries".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCountries.enabled()).isFalse();

        // "cacheManager" -> Caches -> "cities"
        CachesFeed.Caches mainCities = main.caches().stream()
                .filter(c -> "cities".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCities.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCities.enabled()).isFalse();
    }
}
