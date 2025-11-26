package com.nucleonforge.axile.sbs.spring.cache;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.nucleonforge.axile.Main;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CacheDispatcherEndpoint}.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@Import({
    CacheDispatcherEndpoint.class,
    DefaultCacheDispatcher.class,
    CacheDispatcherEndpointTest.CacheDispatcherEndpointTestConfiguration.class
})
class CacheDispatcherEndpointTest {

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
        Cache cache = cacheManager.getCache("cache");
        assert cache != null;
        cache.put("key", "value");
        assertThat(cache.get("key")).isNotNull();

        testRestTemplate.postForObject(path("/cacheManager/disable"), defaultEntity(), Void.class);

        cache.put("key2", "value2");
        assertThat(cache.get("key2")).isNull();
    }

    @Test
    void enableManager_shouldEnableCacheManager() {
        Cache cache = cacheManager.getCache("cache");
        assert cache != null;

        testRestTemplate.postForObject(path("/cacheManager/disable"), defaultEntity(), Void.class);

        testRestTemplate.postForObject(path("/cacheManager/enable"), defaultEntity(), Void.class);

        cache.put("key", "value");
        assertThat(cache.get("key")).isNotNull();
    }

    @Test
    void enableCache_shouldEnableSpecificCache() {
        Cache cache = cacheManager.getCache("cache");
        assert cache != null;

        testRestTemplate.postForObject(path("/cacheManager/cache/disable"), defaultEntity(), Void.class);

        testRestTemplate.postForObject(path("/cacheManager/cache/enable"), defaultEntity(), Void.class);

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
    void enableAllCache_shouldEnableAllCaches() {
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");
        assert cache1 != null;
        assert cache2 != null;

        testRestTemplate.postForObject(path("/cacheManager/disable"), defaultEntity(), Void.class);

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");

        assertThat(cache1.get("key1")).isNull();
        assertThat(cache2.get("key2")).isNull();

        testRestTemplate.postForObject(path("/enable-all-cache"), defaultEntity(), Void.class);

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");

        assertThat(cache1.get("key1")).isNotNull();
        assertThat(cache2.get("key2")).isNotNull();
    }

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
        return "/actuator/cache-dispatcher" + relative;
    }

    @TestConfiguration
    public static class CacheDispatcherEndpointTestConfiguration {

        @Bean
        public static CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
            return new CacheManagerBeanPostProcessor();
        }
    }
}
