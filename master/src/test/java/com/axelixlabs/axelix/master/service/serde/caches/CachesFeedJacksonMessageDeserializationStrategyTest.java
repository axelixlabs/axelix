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
package com.axelixlabs.axelix.master.service.serde.caches;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;

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
        List<CachesFeed.CacheManager> cacheManagerList = caches.getCacheManagers();
        assertThat(cacheManagerList).hasSize(2);

        CachesFeed.CacheManager another = cacheManagerList.stream()
                .filter(cm -> "anotherCacheManager".equals(cm.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(another.getCaches()).hasSize(1);

        CachesFeed.CacheManager main = cacheManagerList.stream()
                .filter(cm -> "cacheManager".equals(cm.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(main.getCaches()).hasSize(2);

        // "anotherCacheManager" -> Caches -> "countries"
        assertThat(another.getCaches()).hasSize(1);
        CachesFeed.Cache anotherCountries = another.getCaches().get(0);
        assertThat(anotherCountries.getName()).isEqualTo("countries");
        assertThat(anotherCountries.getTarget()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(anotherCountries.getHitsCount()).isEqualTo(15);
        assertThat(anotherCountries.getMissesCount()).isEqualTo(2);
        assertThat(anotherCountries.getEstimatedEntrySize()).isEqualTo(10);
        assertThat(anotherCountries.isEnabled()).isFalse();

        // "cacheManager" -> Caches -> "countries"
        CachesFeed.Cache mainCountries = main.getCaches().stream()
                .filter(c -> "countries".equals(c.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCountries.getTarget()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCountries.getHitsCount()).isEqualTo(35);
        assertThat(mainCountries.getMissesCount()).isEqualTo(5);
        assertThat(mainCountries.getEstimatedEntrySize()).isEqualTo(10);
        assertThat(mainCountries.isEnabled()).isFalse();

        // "cacheManager" -> Caches -> "cities"
        CachesFeed.Cache mainCities = main.getCaches().stream()
                .filter(c -> "cities".equals(c.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(mainCities.getTarget()).isEqualTo("java.util.concurrent.ConcurrentHashMap");
        assertThat(mainCities.getHitsCount()).isEqualTo(25);
        assertThat(mainCities.getMissesCount()).isEqualTo(5);
        assertThat(mainCities.getEstimatedEntrySize()).isEqualTo(6);
        assertThat(mainCities.isEnabled()).isFalse();
    }
}
