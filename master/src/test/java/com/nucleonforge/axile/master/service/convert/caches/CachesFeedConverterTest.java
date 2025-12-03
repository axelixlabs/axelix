package com.nucleonforge.axile.master.service.convert.caches;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.CachesFeed;
import com.nucleonforge.axile.common.api.caches.CachesFeed.CacheManagers;
import com.nucleonforge.axile.common.api.caches.CachesFeed.Caches;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse;
import com.nucleonforge.axile.master.service.convert.response.caches.CachesFeedConverter;

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
                .satisfies(caches -> assertThat(caches.enabled()).isFalse());

        // "cacheManager" -> Caches -> "cities"
        assertThat(cacheManager.caches())
                .filteredOn("name", "cities")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("cities"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap"))
                .satisfies(caches -> assertThat(caches.enabled()).isTrue());

        // "cacheManager" -> Caches -> "test"
        assertThat(cacheManager.caches())
                .filteredOn("name", "test")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("test"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.TestHashMap"))
                .satisfies(caches -> assertThat(caches.enabled()).isTrue());
    }

    public CachesFeed getCaches() {
        // Caches
        Caches cities = new Caches("cities", "java.util.concurrent.ConcurrentHashMap", true);
        Caches countries = new Caches("countries", "java.util.concurrent.ConcurrentHashMap", false);
        Caches test = new Caches("test", "java.util.concurrent.TestHashMap", true);

        // CacheManagers
        CacheManagers anotherCacheManager = new CacheManagers("anotherCacheManager", List.of(countries));
        CacheManagers cacheManager = new CacheManagers("cacheManager", List.of(cities, countries, test));

        // return -> CachesFeed
        return new CachesFeed(List.of(anotherCacheManager, cacheManager));
    }
}
