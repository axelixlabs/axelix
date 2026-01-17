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
package com.nucleonforge.axelix.master.service.serde.caches;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.api.caches.CachesFeed;

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
                  "hitsCount" : 15,
                  "missesCount" : 2,
                  "estimatedEntrySize": 10,
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
                  "hitsCount" : 25,
                  "missesCount" : 5,
                  "estimatedEntrySize": 6,
                  "enabled": false
                },
                {
                  "name": "countries",
                  "target" : "java.util.concurrent.ConcurrentHashMap",
                  "hitsCount" : 35,
                  "missesCount" : 5,
                  "estimatedEntrySize": 10,
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
        List<CachesFeed.CacheManager> cacheManagerList = caches.cacheManagers();
        assertThat(cacheManagerList).hasSize(2);

        CachesFeed.CacheManager another = cacheManagerList.stream()
                .filter(cm -> "anotherCacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(another.caches()).hasSize(1);

        CachesFeed.CacheManager main = cacheManagerList.stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();
        assertThat(main.caches()).hasSize(2);

        // "anotherCacheManager" -> Caches -> "countries"
        assertThat(another.caches()).hasSize(1);
        CachesFeed.Cache anotherCountries = another.caches().get(0);
        assertThat(anotherCountries.name()).isEqualTo("countries");
        assertThat(anotherCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(anotherCountries.hitsCount()).isEqualTo(15);
        assertThat(anotherCountries.missesCount()).isEqualTo(2);
        assertThat(anotherCountries.estimatedEntrySize()).isEqualTo(10);
        assertThat(anotherCountries.enabled()).isFalse();

        // "cacheManager" -> Caches -> "countries"
        CachesFeed.Cache mainCountries = main.caches().stream()
                .filter(c -> "countries".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCountries.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCountries.hitsCount()).isEqualTo(35);
        assertThat(mainCountries.missesCount()).isEqualTo(5);
        assertThat(mainCountries.estimatedEntrySize()).isEqualTo(10);
        assertThat(mainCountries.enabled()).isFalse();

        // "cacheManager" -> Caches -> "cities"
        CachesFeed.Cache mainCities = main.caches().stream()
                .filter(c -> "cities".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCities.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCities.hitsCount()).isEqualTo(25);
        assertThat(mainCities.missesCount()).isEqualTo(5);
        assertThat(mainCities.estimatedEntrySize()).isEqualTo(6);
        assertThat(mainCities.enabled()).isFalse();
    }
}
