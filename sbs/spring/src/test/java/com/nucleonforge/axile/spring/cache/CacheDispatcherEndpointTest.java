package com.nucleonforge.axile.spring.cache;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link CacheDispatcherEndpoint} using {@link TestRestTemplate}
 * and a real HTTP context with web environment.
 *
 * <p>These tests verify that the actuator endpoint {@code /actuator/cache-dispatcher}
 * responds correctly to various operations such as clearing caches, evicting keys,
 * and handling invalid CacheManager names.
 *
 * <p>To be discoverable and enabled during tests, the actuator endpoint should either be:
 * <ul>
 *     <li>Explicitly included via {@code management.endpoints.web.exposure.include=cache-dispatcher}, or</li>
 *     <li>Configured as part of auto-configuration in the test application context.</li>
 * </ul>
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@TestPropertySource(properties = {"spring.cache.type=simple"})
class CacheDispatcherEndpointTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void clear_shouldClearEntireCache() {
        String key = "key";
        Cache cache = cacheManager.getCache("cache");
        assertNotNull(cache);
        cache.put(key, "value");
        assertNotNull(cache.get(key));

        CacheClearResponse result = testRestTemplate.postForObject(
                path("/cacheManager/cache?key=key"), defaultEntity(), CacheClearResponse.class);

        assertTrue(result.cleared());
        assertNull(cache.get(key));
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        String key1 = "key1", key2 = "key2";
        Cache cache = cacheManager.getCache("cache");
        assertNotNull(cache);
        cache.put(key1, "value1");
        cache.put(key2, "value2");
        assertNotNull(cache.get(key1));
        assertNotNull(cache.get(key2));

        CacheClearResponse result = testRestTemplate.postForObject(
                path("/cacheManager/cache?key=key2"), defaultEntity(), CacheClearResponse.class);

        assertTrue(result.cleared());
        assertNull(cache.get(key2));
        assertNotNull(cache.get(key1));
    }

    @Test
    void clear_shouldFallbackToClearCache_whenKeyIsMissing() {
        String key = "key";
        Cache cache = cacheManager.getCache("cache");
        assertNotNull(cache);
        cache.put(key, "value");
        assertNotNull(cache.get(key));

        CacheClearResponse result =
                testRestTemplate.postForObject(path("/cacheManager/cache"), defaultEntity(), CacheClearResponse.class);

        assertTrue(result.cleared());
        assertNull(cache.get(key));
    }

    @Test
    void clearKey_shouldReturnFalseEvenIfKeyDoesNotExist() {
        Cache cache = cacheManager.getCache("cache");
        assertNotNull(cache);
        assertNull(cache.get("nonExistingKey"));

        CacheClearResponse result = testRestTemplate.postForObject(
                path("/cacheManager/cache?key=nonExistingKey"), defaultEntity(), CacheClearResponse.class);

        assertFalse(result.cleared());
    }

    @Test
    void clear_shouldReturnFalse_cacheDoesNotExist() {
        CacheClearResponse result = testRestTemplate.postForObject(
                path("/cacheManager/nonExistentCache"), defaultEntity(), CacheClearResponse.class);

        assertFalse(result.cleared());
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");
        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertNotNull(cache1.get(key1));
        assertNotNull(cache2.get(key2));

        CacheClearResponse result =
                testRestTemplate.postForObject(path("/cacheManager"), defaultEntity(), CacheClearResponse.class);

        assertTrue(result.cleared());
        assertNull(cache1.get(key1));
        assertNull(cache2.get(key2));
    }

    @Test
    void clearAll_shouldReturnFalse_cacheManagerDoesNotExist() {
        CacheClearResponse result =
                testRestTemplate.postForObject(path("/nonExistentManager"), defaultEntity(), CacheClearResponse.class);

        assertFalse(result.cleared());
    }

    @Test
    void invalidPath_shouldReturn404() {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity("/actuator/cache-dispatch", defaultEntity(), String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Helper to creates a default HttpEntity with application/json headers and no body.
     */
    private HttpEntity<Void> defaultEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    /**
     * Helper to construct a relative path to the cache-dispatcher actuator endpoint.
     */
    private String path(String relative) {
        return "/actuator/cache-dispatcher" + relative;
    }
}
