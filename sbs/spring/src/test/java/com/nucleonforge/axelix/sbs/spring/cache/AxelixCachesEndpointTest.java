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

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.nucleonforge.axelix.Main;
import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.Cache;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixCachesEndpoint}.
 * <p>
 * TODO:
 *  Gosh, we need to refactor this test to use String Templates if
 *  the Java Language designers team will descend to us finally and
 *  deliver this. Come on Brian, I know you can do this! Push, push,
 *  push, push! We're praying for you and the team!
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @since 24.06.2025
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@Import({
    AxelixCachesEndpoint.class,
    DefaultCacheOperationsDispatcher.class,
    AxelixCachesEndpointTest.CacheDispatcherEndpointTestConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AxelixCachesEndpointTest {

    // Cache names under test
    private static final String TEST_CACHE_1 = "cache1";

    private static final String TEST_CACHE_2 = "cache2";

    private static final String TEST_CACHE_MANAGER = TEST_CACHE_2;

    private EnhancedCacheManager cacheManager;

    @Autowired
    // The bean definition in the context for cache manager has a type of CacheManager,
    // so we cannot do simple field injection via EnhancedCacheManager class.
    public AxelixCachesEndpointTest setCacheManager(org.springframework.cache.CacheManager cacheManager) {
        this.cacheManager = (EnhancedCacheManager) cacheManager;
        return this;
    }

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
        cacheManager.enableAll();

        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).invalidate();
        }
    }

    @Test
    void shouldGetSingleCacheByName() {

        // when.
        ResponseEntity<String> response = testRestTemplate.getForEntity(path(TEST_CACHE_1), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonAssertions.assertThatJson(response.getBody())
                .isEqualTo(
                        // language=json
                        """
                {
                    "cacheManager" : "cache2",
                    "name" : "cache1",
                    "target" : "java.util.concurrent.ConcurrentHashMap",
                    "enabled" : true,
                    "estimatedEntrySize" : 0,
                    "hitsCount" : 0,
                    "missesCount":0
                }
                """);
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        String key1 = "key1", key2 = "key2";
        org.springframework.cache.Cache cache = cacheManager.getCache(TEST_CACHE_1);
        assertThat(cache).isNotNull();

        cache.put(key1, "value1");
        cache.put(key2, "value2");
        assertThat(cache.get(key1)).isNotNull();
        assertThat(cache.get(key2)).isNotNull();

        ResponseEntity<Void> response =
                testRestTemplate.exchange(path(TEST_CACHE_1 + "/clear?key=key2"), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cache.get(key2)).isNull();
        assertThat(cache.get(key1)).isNotNull();
    }

    @Test
    void clear_shouldClearEntireCache() {
        String key1 = "key1", key2 = "key2";
        org.springframework.cache.Cache cache = cacheManager.getCache(TEST_CACHE_1);
        assertThat(cache).isNotNull();

        cache.put(key1, "value1");
        cache.put(key2, "value2");
        assertThat(cache.get(key1)).isNotNull();
        assertThat(cache.get(key2)).isNotNull();

        ResponseEntity<Void> response =
                testRestTemplate.exchange(path(TEST_CACHE_1 + "/clear"), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cache.get(key1)).isNull();
        assertThat(cache.get(key2)).isNull();
    }

    @Test
    void clearKey_shouldReturnFalseIfKeyDoesNotExist() {
        org.springframework.cache.Cache cache = cacheManager.getCache(TEST_CACHE_1);
        assertThat(cache).isNotNull();
        assertThat(cache.get("nonExistingKey")).isNull();

        ResponseEntity<Void> response = testRestTemplate.exchange(
                path(TEST_CACHE_1 + "/clear?key=nonExistingKey"), HttpMethod.DELETE, defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void clear_shouldReturnFalse_cacheDoesNotExist() {
        ResponseEntity<Void> response = testRestTemplate.exchange(
                path("/nonExistentCache/clear"), HttpMethod.DELETE, defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        org.springframework.cache.Cache cache1 = cacheManager.getCache(TEST_CACHE_1);
        org.springframework.cache.Cache cache2 = cacheManager.getCache(TEST_CACHE_2);

        cache1.put(key1, "value1");
        cache2.put(key2, "value2");

        ResponseEntity<Void> response =
                testRestTemplate.exchange(path("/clear-all"), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void shouldDisableAllCaches_onDisableCacheManager() {
        // given.
        org.springframework.cache.Cache cache1 = cacheManager.getCache(TEST_CACHE_1);
        org.springframework.cache.Cache cache2 = cacheManager.getCache(TEST_CACHE_2);

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
        org.springframework.cache.Cache cache = cacheManager.getCache(TEST_CACHE_1);

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
        org.springframework.cache.Cache cache = cacheManager.getCache(TEST_CACHE_1);

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

        org.springframework.cache.Cache enabledCache = cacheManager.getCache(targetEnabledCache);
        org.springframework.cache.Cache disabledCache = cacheManager.getCache(targetDisabledCache);

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

        CacheManager cacheManager = cachesFeed.cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManager.caches()).hasSize(2);

        Cache cache1Info = cacheManager.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isTrue();
        assertThat(cache1Info.target()).isNotNull();
        assertThat(cache1Info.missesCount()).isEqualTo(0);
        assertThat(cache1Info.hitsCount()).isEqualTo(0);
        assertThat(cache1Info.estimatedEntrySize()).isEqualTo(0);

        Cache cache2Info = cacheManager.caches().stream()
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
        org.springframework.cache.Cache cache1 = this.cacheManager.getCache(TEST_CACHE_1);
        cache1.put("key1", "value1");
        cache1.put("key2", "value2");
        cache1.put("key3", "value3");
        cache1.get("key1");
        cache1.get("key2");

        org.springframework.cache.Cache cache2 = this.cacheManager.getCache(TEST_CACHE_2);
        cache2.put("key", "value");
        cache2.get("key");
        cache2.get("notCache1");
        cache2.get("notCache2");

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        CacheManager cacheManager = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManager.caches()).hasSize(2);

        Cache cache1Info = cacheManager.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isTrue();
        assertThat(cache1Info.target()).isNotNull();
        assertThat(cache1Info.missesCount()).isEqualTo(0L);
        assertThat(cache1Info.hitsCount()).isEqualTo(2L);
        assertThat(cache1Info.estimatedEntrySize()).isEqualTo(3L);

        Cache cache2Info = cacheManager.caches().stream()
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

        CachesFeed.CacheManager disabledCacheManager = afterDisablingResponse.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Cache disabledCache = disabledCacheManager.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(disabledCache.enabled()).isFalse();

        testRestTemplate.postForObject(path(TEST_CACHE_1 + "/enable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> afterEnablingResponse = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);
        assertThat(afterEnablingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManager enabledCacheManager = afterEnablingResponse.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Cache enabledCache = enabledCacheManager.caches().stream()
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

        CachesFeed.CacheManager cacheManager = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        assertThat(cacheManager.caches())
                .allSatisfy(cacheInfo -> assertThat(cacheInfo.enabled()).isFalse());
    }

    @Test
    void caches_shouldShowMixedEnabledStatusWhenSomeCachesAreDisabled() {
        testRestTemplate.postForObject(path(TEST_CACHE_1 + "/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManager cacheManager = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Cache cache1Info = cacheManager.caches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.enabled()).isFalse();

        Cache cache2Info = cacheManager.caches().stream()
                .filter(c -> TEST_CACHE_2.equals(c.name()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.enabled()).isTrue();
    }

    @Test
    void caches_shouldIncludeTargetInformation() {
        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManager cacheManager = response.getBody().cacheManagers().stream()
                .filter(cm -> TEST_CACHE_MANAGER.equals(cm.name()))
                .findFirst()
                .orElseThrow();

        Cache cacheInfo = cacheManager.caches().stream()
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
    @Disabled // TODO: Uncomment once we solve the exception handling on the starter side
    void clearAll_shouldReturnFalse_cacheManagerDoesNotExist() {
        ResponseEntity<Void> response = testRestTemplate.exchange(
                path("/nonExistentManager/clear-all", ""), HttpMethod.DELETE, defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
        public org.springframework.cache.CacheManager testSubjectCacheManager() {
            return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
        }
    }
}
