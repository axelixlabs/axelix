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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.CacheManagers;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.Caches;
import com.nucleonforge.axelix.master.api.response.caches.CachesResponse;
import com.nucleonforge.axelix.master.service.convert.response.caches.CachesFeedConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CachesFeedConverter}
 *
 * @author Sergey Cherkasov
 */
public class CachesFeedConverterTest {
    private final CachesFeedConverter subject = new CachesFeedConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        CachesResponse response = subject.convertInternal(getCaches());
        CachesResponse emptyResponse = subject.convertInternal(new CachesFeed());

        // then
        assertThat(response).isNotNull();
        assertThat(emptyResponse.cacheManagers()).isEmpty();

        // CacheManagers -> "anotherCacheManager"
        CachesResponse.CacheManagers anotherCacheManager = response.cacheManagers().stream()
                .filter(manager -> manager.name().equals("anotherCacheManager"))
                .findFirst()
                .orElseThrow();

        assertThat(anotherCacheManager.name()).isEqualTo("anotherCacheManager");

        // "anotherCacheManager" -> Caches -> "countries"
        assertThat(anotherCacheManager.caches())
                .filteredOn("name", "countries")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("countries"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap"))
                .satisfies(caches -> assertThat(caches.hitsCount()).isEqualTo(0))
                .satisfies(caches -> assertThat(caches.missesCount()).isEqualTo(0))
                .satisfies(caches -> assertThat(caches.estimatedEntrySize()).isEqualTo(0))
                .satisfies(caches -> assertThat(caches.enabled()).isFalse());

        // CacheManagers -> "cacheManager"
        CachesResponse.CacheManagers cacheManager = response.cacheManagers().stream()
                .filter(manager -> manager.name().equals("cacheManager"))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManager.name()).isEqualTo("cacheManager");

        // "cacheManager" -> Caches -> "countries"
        assertThat(cacheManager.caches())
                .filteredOn("name", "countries")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("countries"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap"))
                .satisfies(caches -> assertThat(caches.hitsCount()).isEqualTo(0))
                .satisfies(caches -> assertThat(caches.missesCount()).isEqualTo(0))
                .satisfies(caches -> assertThat(caches.estimatedEntrySize()).isEqualTo(0))
                .satisfies(caches -> assertThat(caches.enabled()).isFalse());

        // "cacheManager" -> Caches -> "cities"
        assertThat(cacheManager.caches())
                .filteredOn("name", "cities")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("cities"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap"))
                .satisfies(caches -> assertThat(caches.hitsCount()).isEqualTo(11))
                .satisfies(caches -> assertThat(caches.missesCount()).isEqualTo(2))
                .satisfies(caches -> assertThat(caches.estimatedEntrySize()).isEqualTo(5))
                .satisfies(caches -> assertThat(caches.enabled()).isTrue());

        // "cacheManager" -> Caches -> "test"
        assertThat(cacheManager.caches())
                .filteredOn("name", "test")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("test"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.TestHashMap"))
                .satisfies(caches -> assertThat(caches.hitsCount()).isEqualTo(5))
                .satisfies(caches -> assertThat(caches.missesCount()).isEqualTo(0))
                .satisfies(caches -> assertThat(caches.estimatedEntrySize()).isEqualTo(5))
                .satisfies(caches -> assertThat(caches.enabled()).isTrue());
    }

    public CachesFeed getCaches() {
        // Caches
        Caches cities = new Caches("cities", "java.util.concurrent.ConcurrentHashMap", 11L, 2L, 5L, true);
        Caches countries = new Caches("countries", "java.util.concurrent.ConcurrentHashMap", 0L, 0L, 0L, false);
        Caches test = new Caches("test", "java.util.concurrent.TestHashMap", 5L, 0L, 5L, true);

        // CacheManagers
        CacheManagers anotherCacheManager = new CacheManagers("anotherCacheManager", List.of(countries));
        CacheManagers cacheManager = new CacheManagers("cacheManager", List.of(cities, countries, test));

        // return -> CachesFeed
        return new CachesFeed(List.of(anotherCacheManager, cacheManager));
    }
}
