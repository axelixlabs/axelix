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
package com.nucleonforge.axelix.sbs.spring.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.nucleonforge.axelix.Main;
import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.CacheManagers;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.Caches;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxilixCachesEndpoint}.
 * <p>
 * TODO:
 *  Gosh, we need to refactor this test to use String Templates if
 *  the Java Language designers team will descend to us finally and
 *  deliver this. Come on Brian, I know you can do this! Push, push,
 *  push, push! We're praying for you and the team!
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@Import({
    AxilixCachesEndpoint.class,
    DefaultCacheDispatcher.class,
    AxilixCachesEndpointTest.CacheDispatcherEndpointTestConfiguration.class,
    CachesEndpoint.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AxilixCachesEndpointTest {

    // Cache names under test
    private static final String TEST_CACHE_1 = "cache1";
    private static final String TEST_CACHE_2 = "cache2";
    private static final String TEST_CACHE_MANAGER = TEST_CACHE_2;

    @Autowired
    private EnhancedCacheManager cacheManager;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
        cacheManager.enableAllCaches();

        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).invalidate();
        }
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        String key1 = "key1", key2 = "key2";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        assertThat(cache).isNotNull();

        cache.put(key1, "value1");
        cache.put(key2, "value2");
        assertThat(cache.get(key1)).isNotNull();
        assertThat(cache.get(key2)).isNotNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path(TEST_CACHE_1 + "/clear?key=key2"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(true, CacheClearResponse::cleared);
        assertThat(cache.get(key2)).isNull();
        assertThat(cache.get(key1)).isNotNull();
    }

    @Test
    void clear_shouldClearEntireCache() {
        String key1 = "key1", key2 = "key2";
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        assertThat(cache).isNotNull();

        cache.put(key1, "value1");
        cache.put(key2, "value2");
        assertThat(cache.get(key1)).isNotNull();
        assertThat(cache.get(key2)).isNotNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path(TEST_CACHE_1 + "/clear"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(true, CacheClearResponse::cleared);
        assertThat(cache.get(key1)).isNull();
        assertThat(cache.get(key2)).isNull();
    }

    @Test
    void clearKey_shouldReturnFalseIfKeyDoesNotExist() {
        Cache cache = cacheManager.getCache(TEST_CACHE_1);
        assertThat(cache).isNotNull();
        assertThat(cache.get("nonExistingKey")).isNull();

        CacheClearResponse response = testRestTemplate.postForObject(
                path(TEST_CACHE_1 + "?key=nonExistingKey"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(false, CacheClearResponse::cleared);
    }

    @Test
    void clear_shouldReturnFalse_cacheDoesNotExist() {
        CacheClearResponse response =
                testRestTemplate.postForObject(path("/nonExistentCache"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(false, CacheClearResponse::cleared);
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        Cache cache1 = cacheManager.getCache(TEST_CACHE_1);
        Cache cache2 = cacheManager.getCache(TEST_CACHE_2);
        assertThat(cache1).isNotNull();
        assertThat(cache2).isNotNull();

        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertThat(cache1.get(key1)).isNotNull();
        assertThat(cache2.get(key2)).isNotNull();

        CacheClearResponse response =
                testRestTemplate.postForObject(path("/clear-all"), defaultEntity(), CacheClearResponse.class);

        assertThat(response).isNotNull().returns(true, CacheClearResponse::cleared);
        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void shouldDisableAllCaches_onDisableCacheManager() {
        // given.
        Cache cache1 = cacheManager.getCache(TEST_CACHE_1);
        Cache cache2 = cacheManager.getCache(TEST_CACHE_2);

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");

        // when.
        testRestTemplate.postForObject(path("/disable"), defaultEntity(), Void.class);
        cache1.put("key3", "value2");
        cache2.put("key4", "value2");

        // then.
        assertThat(cache1.get("key1")).isNull();
        assertThat(cache1.get("key3")).isNull();
        assertThat(cache2.get("key2")).isNull();
        assertThat(cache2.get("key4")).isNull();
        assertThat(cacheManager.getCacheNames()).containsOnly(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Test
    void enableManager_shouldEnableCacheManager() {
        // given.
        Cache cache = cacheManager.getCache(TEST_CACHE_1);

        // when.
        testRestTemplate.postForObject(path("/disable"), defaultEntity(), Void.class);
        testRestTemplate.postForObject(path("/enable"), defaultEntity(), Void.class);
        cache.put("key", "value");

        // then.
        assertThat(cache.get("key")).isNotNull();
    }

    @Test
    void enableCache_shouldEnableOnlySpecificCache() {
        // given.
        Cache cache = cacheManager.getCache(TEST_CACHE_1);

        // when.
        testRestTemplate.postForObject(path(TEST_CACHE_1 + "/disable"), defaultEntity(), Void.class);
        testRestTemplate.postForObject(path(TEST_CACHE_1 + "/enable"), defaultEntity(), Void.class);

        // then.
        cache.put("key", "value");
        assertThat(cache.get("key")).isNotNull();
    }

    @Test
    void disableCache_shouldDisableSpecifiedCache() {
        String targetEnabledCache = TEST_CACHE_1;
        String targetDisabledCache = TEST_CACHE_2;

        Cache enabledCache = cacheManager.getCache(targetEnabledCache);
        Cache disabledCache = cacheManager.getCache(targetDisabledCache);

        enabledCache.put("key1", "value");
        disabledCache.put("key1", "value");

        assertThat(enabledCache.get("key1")).isNotNull();
        assertThat(disabledCache.get("key1")).isNotNull();

        testRestTemplate.postForObject(path(targetDisabledCache + "/disable"), defaultEntity(), Void.class);

        enabledCache.put("key2", "value2");
        disabledCache.put("key2", "value2");

        assertThat(enabledCache.get("key2")).isNotNull();
        assertThat(enabledCache.get("key1")).isNotNull();
        assertThat(disabledCache.get("key2")).isNull();
        assertThat(disabledCache.get("key1")).isNull();
    }

    @Test
    void caches_shouldReturnAllCachesWithEnabledStatus() {
        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CachesFeed cachesFeed = response.getBody();

        CacheManagers cacheManagers = cachesFeed.cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManagers.caches()).hasSize(2);

        Caches cache1Info = cacheManagers.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isTrue();
        assertThat(cache1Info.target()).isNotNull();
        assertThat(cache1Info.missesCount()).isEqualTo(0);
        assertThat(cache1Info.hitsCount()).isEqualTo(0);
        assertThat(cache1Info.estimatedEntrySize()).isEqualTo(0);

        Caches cache2Info = cacheManagers.caches().stream()
                .filter(c -> TEST_CACHE_2.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.enabled()).isTrue();
        assertThat(cache2Info.target()).isNotNull();
        assertThat(cache2Info.missesCount()).isEqualTo(0);
        assertThat(cache2Info.hitsCount()).isEqualTo(0);
        assertThat(cache2Info.estimatedEntrySize()).isEqualTo(0);
    }

    @Test
    void concurrentCache_shouldReturnCacheInformation() {
        Cache cache1 = cacheManager.getCache(TEST_CACHE_1);
        cache1.put("key1", "value1");
        cache1.put("key2", "value2");
        cache1.put("key3", "value3");
        cache1.get("key1");
        cache1.get("key2");

        Cache cache2 = cacheManager.getCache(TEST_CACHE_2);
        cache2.put("key", "value");
        cache2.get("key");
        cache2.get("notCache1");
        cache2.get("notCache2");

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        CacheManagers cacheManagers = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManagers.caches()).hasSize(2);

        Caches cache1Info = cacheManagers.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isTrue();
        assertThat(cache1Info.target()).isNotNull();
        assertThat(cache1Info.missesCount()).isEqualTo(0L);
        assertThat(cache1Info.hitsCount()).isEqualTo(2L);
        assertThat(cache1Info.estimatedEntrySize()).isEqualTo(3L);

        Caches cache2Info = cacheManagers.caches().stream()
                .filter(c -> TEST_CACHE_2.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.enabled()).isTrue();
        assertThat(cache2Info.target()).isNotNull();
        assertThat(cache2Info.missesCount()).isEqualTo(2L);
        assertThat(cache2Info.hitsCount()).isEqualTo(1L);
        assertThat(cache2Info.estimatedEntrySize()).isEqualTo(1L);
    }

    @Test
    void caches_shouldShowDisableEnabledCache() {
        cacheManager.getCache(TEST_CACHE_1);

        testRestTemplate.postForObject(path(TEST_CACHE_1 + "/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> afterDisablingResponse = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);
        assertThat(afterDisablingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers disabledCacheManager = afterDisablingResponse.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches disabledCache = disabledCacheManager.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(disabledCache.enabled()).isFalse();

        testRestTemplate.postForObject(path(TEST_CACHE_1 + "/enable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> afterEnablingResponse = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);
        assertThat(afterEnablingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers enabledCacheManager = afterEnablingResponse.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches enabledCache = enabledCacheManager.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(enabledCache.enabled()).isTrue();
    }

    @Test
    void caches_shouldShowAllCachesDisabledWhenManagerIsDisabled() {
        testRestTemplate.postForObject(path("/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers cacheManagers = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManagers.caches())
                .allSatisfy(cacheInfo -> assertThat(cacheInfo.enabled()).isFalse());
    }

    @Test
    void caches_shouldShowMixedEnabledStatusWhenSomeCachesAreDisabled() {
        testRestTemplate.postForObject(path(TEST_CACHE_1 + "/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagers cacheManagers = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches cache1Info = cacheManagers.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isFalse();

        Caches cache2Info = cacheManagers.caches().stream()
                .filter(c -> TEST_CACHE_2.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.enabled()).isTrue();
    }

    @Test
    void caches_shouldIncludeTargetInformation() {
        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CachesFeed.CacheManagers cacheManagers = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Caches cacheInfo = cacheManagers.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheInfo.target()).isNotNull().isNotEmpty().contains("ConcurrentHashMap");
    }

    // TODO: I'm not sure that this return 200 OK is the correct way of handling the non existent cache
    @Test
    void enableCache_shouldHandleNonExistentCache() {
        ResponseEntity<Void> response =
                testRestTemplate.postForEntity(path("/nonExistentCache/enable"), defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void disableCache_shouldHandleNonExistentCache() {
        ResponseEntity<Void> response =
                testRestTemplate.postForEntity(path("/nonExistentCache/disable"), defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void enableManager_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(path("nonExistentManager", "/enable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void disableManager_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(path("/nonExistentManager", "/disable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void clearAll_shouldReturnFalse_cacheManagerDoesNotExist() {
        CacheClearResponse response = testRestTemplate.postForObject(
                path("/nonExistentManager", ""), defaultEntity(), CacheClearResponse.class);

        assertThat(response.cleared()).isFalse();
    }

    @Test
    void enableCache_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                path("/nonExistentManager", "/nonExistentCache/enable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void disableCache_shouldThrowExceptionForNonExistentManager() {
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                path("/nonExistentManager", "/nonExistentCache/disable"), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpEntity<Void> defaultEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    private String path(String relative) {
        return path(TEST_CACHE_MANAGER, relative);
    }

    private String rootPath() {
        return path("", "");
    }

    private String path(String cacheManagerName, String relative) {
        relative = prefixPathIfNeeded(relative);
        cacheManagerName = prefixPathIfNeeded(cacheManagerName);

        return "/actuator/axelix-caches" + cacheManagerName + relative;
    }

    private static String prefixPathIfNeeded(String path) {
        return (path.isEmpty() || path.charAt(0) == '/') ? path : "/" + path;
    }

    @TestConfiguration
    public static class CacheDispatcherEndpointTestConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public CacheSizeProvider cacheSizeProvider() {
            return new DefaultCacheSizeProvider();
        }

        @Bean
        public static CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
            return new CacheManagerBeanPostProcessor();
        }

        @Bean(name = TEST_CACHE_MANAGER)
        public CacheManager testSubjectCacheManager() {
            return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
        }
    }
}
