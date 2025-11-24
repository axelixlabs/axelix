package com.nucleonforge.axile.master.service.convert.caches;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.ServiceCaches;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse;
import com.nucleonforge.axile.master.service.convert.response.caches.ServiceCachesConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceCachesConverter}
 *
 * @author Sergey Cherkasov
 */
public class ServiceCachesConverterTest {
    private final ServiceCachesConverter subject = new ServiceCachesConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        CachesResponse response = subject.convertInternal(getCaches());
        CachesResponse emptyResponse = subject.convertInternal(new ServiceCaches());

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
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap"));

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
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap"));

        // "cacheManager" -> Caches -> "cities"
        assertThat(cacheManager.caches())
                .filteredOn("name", "cities")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("cities"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.ConcurrentHashMap"));

        // "cacheManager" -> Caches -> "cities"
        assertThat(cacheManager.caches())
                .filteredOn("name", "test")
                .first()
                .satisfies(caches -> assertThat(caches.name()).isEqualTo("test"))
                .satisfies(caches -> assertThat(caches.target()).isEqualTo("java.util.concurrent.TestHashMap"));
    }

    public ServiceCaches getCaches() {
        // Caches
        ServiceCaches.CacheManagers.Caches cities =
                new ServiceCaches.CacheManagers.Caches("java.util.concurrent.ConcurrentHashMap");
        ServiceCaches.CacheManagers.Caches countries =
                new ServiceCaches.CacheManagers.Caches("java.util.concurrent.ConcurrentHashMap");
        ServiceCaches.CacheManagers.Caches test =
                new ServiceCaches.CacheManagers.Caches("java.util.concurrent.TestHashMap");

        // CacheManagers
        ServiceCaches.CacheManagers anotherCacheManager =
                new ServiceCaches.CacheManagers(Map.of("countries", countries));
        ServiceCaches.CacheManagers cacheManager =
                new ServiceCaches.CacheManagers(Map.of("cities", cities, "countries", countries, "test", test));

        // return -> ServiceCaches
        return new ServiceCaches(Map.of(
                "anotherCacheManager", anotherCacheManager,
                "cacheManager", cacheManager));
    }
}
