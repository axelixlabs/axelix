package com.nucleonforge.axile.sbs.spring.cache;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
@Import({CacheDispatcherEndpoint.class, DefaultCacheDispatcher.class})
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
    void clearAll_shouldReturnFalse_cacheManagerDoesNotExist() {
        CacheClearResponse response =
                testRestTemplate.postForObject(path("/nonExistentManager"), defaultEntity(), CacheClearResponse.class);

        assertThat(response.cleared()).isFalse();
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
}
