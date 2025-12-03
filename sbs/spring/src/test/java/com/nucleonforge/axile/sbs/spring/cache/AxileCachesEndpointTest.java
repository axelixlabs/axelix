package com.nucleonforge.axile.sbs.spring.cache;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.nucleonforge.axile.Main;
import com.nucleonforge.axile.common.api.caches.CachesFeed;
import com.nucleonforge.axile.common.api.caches.CachesFeed.CacheManagers;
import com.nucleonforge.axile.common.api.caches.CachesFeed.Caches;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxileCachesEndpoint}.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@Import({
    AxileCachesEndpoint.class,
    DefaultCacheDispatcher.class,
    AxileCachesEndpointTest.CacheDispatcherEndpointTestConfiguration.class,
    CachesEndpoint.class
})

// TODO:
//  This is required since we tinker with caches. However, ideally, we should clean everything up in
//  BeforeEach callback or some sort.
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AxileCachesEndpointTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void clear_shouldClearEntireCache() {
        String key = "key";
        Cache cache = cacheManager.getCache("cache");
        assertThat(cache).isNotNull();

        cache.put(key, "value");
        assertThat(cache.get(key)).isNotNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path("/cacheManager/cache/clear?key=key"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(true, CacheClearResponse::cleared);
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        String key1 = "key1", key2 = "key2";
        Cache cache = cacheManager.getCache("cache");
        assertThat(cache).isNotNull();

        cache.put(key1, "value1");
        cache.put(key2, "value2");
        assertThat(cache.get(key1)).isNotNull();
        assertThat(cache.get(key2)).isNotNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path("/cacheManager/cache/clear?key=key2"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(true, CacheClearResponse::cleared);
        assertThat(cache.get(key2)).isNull();
        assertThat(cache.get(key1)).isNotNull();
    }

    @Test
    void clear_shouldFallbackToClearCache_whenKeyIsMissing() {
        String key = "key";
        Cache cache = cacheManager.getCache("cache");
        assertThat(cache).isNotNull();

        cache.put(key, "value");
        assertThat(cache.get(key)).isNotNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path("/cacheManager/cache/clear"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(true, CacheClearResponse::cleared);
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void clearKey_shouldReturnFalseEvenIfKeyDoesNotExist() {
        Cache cache = cacheManager.getCache("cache");
        assertThat(cache).isNotNull();
        assertThat(cache.get("nonExistingKey")).isNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path("/cacheManager/cache?key=nonExistingKey"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(false, CacheClearResponse::cleared);
    }

    @Test
    void clear_shouldReturnFalse_cacheDoesNotExist() {
        CacheClearResponse response = testRestTemplate.postForObject(
                path("/cacheManager/nonExistentCache"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(false, CacheClearResponse::cleared);
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");
        assertThat(cache1).isNotNull();
        assertThat(cache2).isNotNull();

        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertThat(cache1.get(key1)).isNotNull();
        assertThat(cache2.get(key2)).isNotNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path("/cacheManager/clear-all"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(true, CacheClearResponse::cleared);
        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void disableManager_shouldDisableCacheManager() {
        // given.
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");

        // when.
        testRestTemplate.postForObject(path("/cacheManager/disable"), defaultEntity(), Void.class);
        cache1.put("key3", "value2");
        cache2.put("key4", "value2");

        // then.
        assertThat(cache1.get("key1")).isNull();
        assertThat(cache1.get("key3")).isNull();
        assertThat(cache2.get("key2")).isNull();
        assertThat(cache2.get("key4")).isNull();
        assertThat(cacheManager.getCacheNames()).containsOnly("cache1", "cache2");
    }

    @Test
    void enableManager_shouldEnableCacheManager() {
        // given.
        Cache cache = cacheManager.getCache("cache");

        // when.
        testRestTemplate.postForObject(path("/cacheManager/disable"), defaultEntity(), Void.class);
        testRestTemplate.postForObject(path("/cacheManager/enable"), defaultEntity(), Void.class);
        cache.put("key", "value");

        // then.
        assertThat(cache.get("key")).isNotNull();
    }

    @Test
    void enableCache_shouldEnableSpecificCache() {
        // given.
        Cache cache = cacheManager.getCache("cache");

        // when.
        testRestTemplate.postForObject(path("/cacheManager/cache/disable"), defaultEntity(), Void.class);
        testRestTemplate.postForObject(path("/cacheManager/cache/enable"), defaultEntity(), Void.class);

        // then.
        cache.put("key", "value");
        assertThat(cache.get("key")).isNotNull();
    }

    @Test
    void disableCache_shouldDisableSpecificCache() {
        Cache enabledCache = cacheManager.getCache("enabledCache");
        Cache disabledCache = cacheManager.getCache("disabledCache");
        assert enabledCache != null;
        assert disabledCache != null;

        enabledCache.put("key", "value");
        disabledCache.put("key", "value");
        assertThat(enabledCache.get("key")).isNotNull();
        assertThat(disabledCache.get("key")).isNotNull();

        testRestTemplate.postForObject(path("/cacheManager/disabledCache/disable"), defaultEntity(), Void.class);

        enabledCache.put("key2", "value2");
        disabledCache.put("key2", "value2");

        assertThat(enabledCache.get("key2")).isNotNull();
        assertThat(disabledCache.get("key2")).isNull();
    }

    @Test
    void caches_shouldReturnAllCachesWithEnabledStatus() {
        cacheManager.getCache("cache1");
        cacheManager.getCache("cache2");

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(path(""), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CachesFeed cachesFeed = response.getBody();

        CacheManagers cacheManagers = cachesFeed.cacheManagers().stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManagers.caches()).hasSize(2);

        Caches cache1Info = cacheManagers.caches().stream()
                .filter(c -> "cache1".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isTrue();
        assertThat(cache1Info.target()).isNotNull();

        Caches cache2Info = cacheManagers.caches().stream()
                .filter(c -> "cache2".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.enabled()).isTrue();
        assertThat(cache2Info.target()).isNotNull();
    }

    @Test
    void caches_shouldShowDisableEnabledCache() {
        cacheManager.getCache("cache");

        testRestTemplate.postForObject(path("/cacheManager/cache/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> afterDisablingResponse = testRestTemplate.getForEntity(path(""), CachesFeed.class);
        assertThat(afterDisablingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers disabledCacheManager = afterDisablingResponse.getBody().cacheManagers().stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches disabledCache = disabledCacheManager.caches().stream()
                .filter(c -> "cache".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(disabledCache.enabled()).isFalse();

        testRestTemplate.postForObject(path("/cacheManager/cache/enable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> afterEnablingResponse = testRestTemplate.getForEntity(path(""), CachesFeed.class);
        assertThat(afterEnablingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers enabledCacheManager = afterEnablingResponse.getBody().cacheManagers().stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches enabledCache = enabledCacheManager.caches().stream()
                .filter(c -> "cache".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(enabledCache.enabled()).isTrue();
    }

    @Test
    void caches_shouldShowAllCachesDisabledWhenManagerIsDisabled() {
        cacheManager.getCache("cache1");
        cacheManager.getCache("cache2");

        testRestTemplate.postForObject(path("/cacheManager/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(path(""), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers cacheManagers = response.getBody().cacheManagers().stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManagers.caches())
                .allSatisfy(cacheInfo -> assertThat(cacheInfo.enabled()).isFalse());
    }

    @Test
    void caches_shouldShowMixedEnabledStatusWhenSomeCachesAreDisabled() {
        cacheManager.getCache("cache1");
        cacheManager.getCache("cache2");

        testRestTemplate.postForObject(path("/cacheManager/cache1/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(path(""), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers cacheManagers = response.getBody().cacheManagers().stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches cache1Info = cacheManagers.caches().stream()
                .filter(c -> "cache1".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isFalse();

        Caches cache2Info = cacheManagers.caches().stream()
                .filter(c -> "cache2".equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.enabled()).isTrue();
    }

    @Test
    void caches_shouldIncludeTargetInformation() {
        cacheManager.getCache("cache");

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(path(""), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CachesFeed.CacheManagers cacheManagers = response.getBody().cacheManagers().stream()
                .filter(cm -> "cacheManager".equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches cacheInfo = cacheManagers.caches().stream()
                .filter(c -> "cache".equals(c.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheInfo.target()).isNotNull().isNotEmpty().contains("ConcurrentHashMap");
    }

    // TODO: I'm not sure that this return 200 OK is the correct way of handling the non existent cache
    @Test
    void enableCache_shouldHandleNonExistentCache() {
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                path("/cacheManager/nonExistentCache/enable"), defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void disableCache_shouldHandleNonExistentCache() {
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                path("/cacheManager/nonExistentCache/disable"), defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void enableManager_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(path("/nonExistentManager/enable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void disableManager_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(path("/nonExistentManager/disable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void clearAll_shouldReturnFalse_cacheManagerDoesNotExist() {
        CacheClearResponse response =
                testRestTemplate.postForObject(path("/nonExistentManager"), defaultEntity(), CacheClearResponse.class);

        assertThat(response.cleared()).isFalse();
    }

    @Test
    void enableCache_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                path("/nonExistentManager/nonExistentCache/enable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void disableCache_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                path("/nonExistentManager/nonExistentCache/disable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void invalidPath_shouldReturn404() {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity("/actuator/cache-dispatch", defaultEntity(), String.class);
        assertThat(response).isNotNull().returns(HttpStatus.NOT_FOUND, ResponseEntity::getStatusCode);
    }

    private HttpEntity<Void> defaultEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    private String path(String relative) {
        return "/actuator/axile-caches" + relative;
    }

    @TestConfiguration
    public static class CacheDispatcherEndpointTestConfiguration {

        @Bean
        public static CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
            return new CacheManagerBeanPostProcessor();
        }
    }
}
