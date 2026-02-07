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
package com.axelixlabs.axelix.master.service.convert.caches;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;
import com.axelixlabs.axelix.common.api.caches.CachesFeed.Cache;
import com.axelixlabs.axelix.common.api.caches.CachesFeed.CacheManager;
import com.axelixlabs.axelix.master.api.external.response.caches.CachesResponse;
import com.axelixlabs.axelix.master.service.convert.response.caches.CachesFeedConverter;

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
        CachesResponse emptyResponse = subject.convertInternal(new CachesFeed(List.of()));

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
        Cache cities = new Cache("cities", "java.util.concurrent.ConcurrentHashMap", 11L, 2L, 5L, true);
        Cache countries = new Cache("countries", "java.util.concurrent.ConcurrentHashMap", 0L, 0L, 0L, false);
        Cache test = new Cache("test", "java.util.concurrent.TestHashMap", 5L, 0L, 5L, true);

        // CacheManagers
        CacheManager anotherCacheManager = new CacheManager("anotherCacheManager", List.of(countries));
        CacheManager cacheManager = new CacheManager("cacheManager", List.of(cities, countries, test));

        // return -> CachesFeed
        return new CachesFeed(List.of(anotherCacheManager, cacheManager));
    }
}
