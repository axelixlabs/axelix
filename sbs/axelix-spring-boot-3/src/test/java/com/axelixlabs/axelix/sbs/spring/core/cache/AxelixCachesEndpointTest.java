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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.util.Map;
import java.util.stream.Stream;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;
import com.axelixlabs.axelix.common.api.caches.CachesFeed.CacheDto;
import com.axelixlabs.axelix.common.api.caches.CachesFeed.CacheManagerDto;
import com.axelixlabs.axelix.sbs.spring.core.Main;

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
class AxelixCachesEndpointTest {

    // Cache names under test
    private static final String TEST_CACHE_1 = "cache1";
    private static final String TEST_CACHE_2 = "cache2";

    private static final String MAIN_CACHE_MANAGER = "mainCacheManager";
    private static final String CLEAR_CACHE_MANAGER = "clearCacheManager";
    private static final String ENABLE_CACHE_MANAGER = "enableCacheManager";
    private static final String DISABLE_CACHE_MANAGER = "disableCacheManager";

    private EnhancedCacheManager mainCacheManager;
    private EnhancedCacheManager clearCacheManager;
    private EnhancedCacheManager enableCacheManager;
    private EnhancedCacheManager disableCacheManager;

    // The bean definition in the context for cache manager has a type of CacheManager,
    // so we cannot do simple field injection via EnhancedCacheManager class.
    @Autowired
    public AxelixCachesEndpointTest setMainCacheManager(CacheManager mainCacheManager) {
        this.mainCacheManager = (EnhancedCacheManager) mainCacheManager;
        return this;
    }

    @Autowired
    public AxelixCachesEndpointTest setClearCacheManager(@Qualifier(CLEAR_CACHE_MANAGER) CacheManager cacheManager) {
        this.clearCacheManager = (EnhancedCacheManager) cacheManager;
        return this;
    }

    @Autowired
    public AxelixCachesEndpointTest setEnableCacheManager(@Qualifier(ENABLE_CACHE_MANAGER) CacheManager cacheManager) {
        this.enableCacheManager = (EnhancedCacheManager) cacheManager;
        return this;
    }

    @Autowired
    public AxelixCachesEndpointTest setDisableCacheManager(
            @Qualifier(DISABLE_CACHE_MANAGER) CacheManager cacheManager) {
        this.disableCacheManager = (EnhancedCacheManager) cacheManager;
        return this;
    }

    @Autowired
    private Map<String, CacheManager> allCacheManagers;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
        for (CacheManager cacheManager : allCacheManagers.values()) {
            if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
                enhancedCacheManager.enableAll();
                for (String cacheName : enhancedCacheManager.getCacheNames()) {
                    Cache cache = enhancedCacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                }
            }
        }
    }

    @Test
    void get_shouldReturnSingleCacheByName() {
        // when.
        ResponseEntity<String> response =
                testRestTemplate.getForEntity(path(MAIN_CACHE_MANAGER, TEST_CACHE_1), String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonAssertions.assertThatJson(response.getBody())
                .isEqualTo(
                        // language=json
                        """
                {
                    "cacheManager" : "%s",
                    "name" : "%s",
                    "target" : "java.util.concurrent.ConcurrentHashMap",
                    "enabled" : true,
                    "estimatedEntrySize" : 0,
                    "lookupHistory" : []
                }
                """.formatted(MAIN_CACHE_MANAGER, TEST_CACHE_1));
    }

    @Test
    void get_shouldReturnCacheInformation() {
        Cache cache1 = mainCacheManager.getCache(TEST_CACHE_1);
        cache1.put("key1", "value1");
        cache1.put("key2", "value2");
        cache1.put("key3", "value3");
        cache1.get("key1");
        cache1.get("key2");

        Cache cache2 = mainCacheManager.getCache(TEST_CACHE_2);
        cache2.put("key", "value");
        cache2.get("key");
        cache2.get("notCache1");
        cache2.get("notCache2");

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagerDto cacheManager = getCacheManager(response.getBody(), MAIN_CACHE_MANAGER);
        assertThat(cacheManager.getCaches()).hasSize(2);

        assertThat(cacheManager.getCaches().stream()
                        .filter(c -> TEST_CACHE_1.equals(c.getName()))
                        .findFirst())
                .hasValueSatisfying(c -> {
                    assertThat(c.isEnabled()).isTrue();
                    assertThat(c.getTarget()).isNotNull();
                    assertThat(c.isContainsStats()).isTrue();
                });

        assertThat(cacheManager.getCaches().stream()
                        .filter(c -> TEST_CACHE_2.equals(c.getName()))
                        .findFirst())
                .hasValueSatisfying(c -> {
                    assertThat(c.isEnabled()).isTrue();
                    assertThat(c.getTarget()).isNotNull();
                    assertThat(c.isContainsStats()).isTrue();
                });
    }

    @Test
    void clear_shouldEvictSingleEntry() {
        String key1 = "key1", key2 = "key2";
        Cache cache = clearCacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");
        cache.put(key2, "value2");

        ResponseEntity<Void> response = testRestTemplate.exchange(
                path(CLEAR_CACHE_MANAGER, TEST_CACHE_1 + "/clear?key=key2"), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cache.get(key2)).isNull();
        assertThat(cache.get(key1)).isNotNull();
    }

    @Test
    void clear_shouldClearEntireCache() {
        String key1 = "key1", key2 = "key2";
        Cache cache = clearCacheManager.getCache(TEST_CACHE_1);
        cache.put(key1, "value1");
        cache.put(key2, "value2");

        ResponseEntity<Void> response = testRestTemplate.exchange(
                path(CLEAR_CACHE_MANAGER, TEST_CACHE_1 + "/clear"), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cache.get(key1)).isNull();
        assertThat(cache.get(key2)).isNull();
    }

    @Test
    void clear_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        Cache cache1 = clearCacheManager.getCache(TEST_CACHE_1);
        Cache cache2 = clearCacheManager.getCache(TEST_CACHE_2);
        cache1.put(key1, "value1");
        cache2.put(key2, "value2");

        ResponseEntity<Void> response =
                testRestTemplate.exchange(path(CLEAR_CACHE_MANAGER, "/clear-all"), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void disable_onDisableAllCacheManager() {
        Cache cache1 = disableCacheManager.getCache(TEST_CACHE_1);
        Cache cache2 = disableCacheManager.getCache(TEST_CACHE_2);
        cache1.put("key1", "value1");
        cache2.put("key2", "value2");

        // when.
        testRestTemplate.postForObject(path(DISABLE_CACHE_MANAGER, "/disable"), defaultEntity(), Void.class);
        cache1.put("key3", "value2");
        cache2.put("key4", "value2");

        // then.
        assertThat(cache1.get("key1")).isNull();
        assertThat(cache1.get("key3")).isNull();
        assertThat(cache2.get("key2")).isNull();
        assertThat(cache2.get("key4")).isNull();
        assertThat(disableCacheManager.getCacheNames()).containsOnly(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Test
    void enable_shouldEnableCacheManager() {
        Cache cache = enableCacheManager.getCache(TEST_CACHE_1);

        // when.
        testRestTemplate.postForObject(path(ENABLE_CACHE_MANAGER, "/disable"), defaultEntity(), Void.class);
        testRestTemplate.postForObject(path(ENABLE_CACHE_MANAGER, "/enable"), defaultEntity(), Void.class);
        cache.put("key", "value");

        // then.
        assertThat(cache.get("key")).isNotNull();
    }

    @Test
    void enable_shouldEnableOnlySpecificCache() {
        Cache cache = enableCacheManager.getCache(TEST_CACHE_1);

        // when.
        testRestTemplate.postForObject(
                path(ENABLE_CACHE_MANAGER, TEST_CACHE_1 + "/disable"), defaultEntity(), Void.class);
        testRestTemplate.postForObject(
                path(ENABLE_CACHE_MANAGER, TEST_CACHE_1 + "/enable"), defaultEntity(), Void.class);

        // then.
        cache.put("key", "value");
        assertThat(cache.get("key")).isNotNull();
    }

    @Test
    void disable_shouldDisableSpecifiedCache() {
        String targetEnabledCache = TEST_CACHE_1;
        String targetDisabledCache = TEST_CACHE_2;
        Cache enabledCache = disableCacheManager.getCache(targetEnabledCache);
        Cache disabledCache = disableCacheManager.getCache(targetDisabledCache);
        enabledCache.put("key1", "value");
        disabledCache.put("key1", "value");

        testRestTemplate.postForObject(
                path(DISABLE_CACHE_MANAGER, targetDisabledCache + "/disable"), defaultEntity(), Void.class);

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
        CacheManagerDto cacheManager = getCacheManager(response.getBody(), MAIN_CACHE_MANAGER);

        assertThat(cacheManager.getCaches()).hasSize(2);

        CacheDto cache1Info = cacheManager.getCaches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.isEnabled()).isTrue();
        assertThat(cache1Info.getTarget()).isNotNull();
        assertThat(cache1Info.isContainsStats()).isFalse();

        CacheDto cache2Info = cacheManager.getCaches().stream()
                .filter(c -> TEST_CACHE_2.equals(c.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.isEnabled()).isTrue();
        assertThat(cache2Info.getTarget()).isNotNull();
        assertThat(cache2Info.isContainsStats()).isFalse();
    }

    @Test
    void caches_shouldShowDisableEnabledCache() {
        mainCacheManager.getCache(TEST_CACHE_1);

        testRestTemplate.postForObject(
                path(ENABLE_CACHE_MANAGER, TEST_CACHE_1 + "/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> afterDisablingResponse = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);
        assertThat(afterDisablingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheDto disabledCache =
                getCacheManager(afterDisablingResponse.getBody(), ENABLE_CACHE_MANAGER).getCaches().stream()
                        .filter(c -> TEST_CACHE_1.equals(c.getName()))
                        .findFirst()
                        .orElseThrow();
        assertThat(disabledCache.isEnabled()).isFalse();

        testRestTemplate.postForObject(
                path(ENABLE_CACHE_MANAGER, TEST_CACHE_1 + "/enable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> afterEnablingResponse = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);
        assertThat(afterEnablingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheDto enabledCache =
                getCacheManager(afterEnablingResponse.getBody(), ENABLE_CACHE_MANAGER).getCaches().stream()
                        .filter(c -> TEST_CACHE_1.equals(c.getName()))
                        .findFirst()
                        .orElseThrow();
        assertThat(enabledCache.isEnabled()).isTrue();
    }

    @Test
    void disable_shouldShowAllCachesDisabledWhenManagerIsDisabled() {
        testRestTemplate.postForObject(path(DISABLE_CACHE_MANAGER, "/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagerDto cacheManager = getCacheManager(response.getBody(), DISABLE_CACHE_MANAGER);

        assertThat(cacheManager.getCaches())
                .allSatisfy(cacheInfo -> assertThat(cacheInfo.isEnabled()).isFalse());
    }

    @Test
    void disable_shouldShowMixedEnabledStatusWhenSomeCachesAreDisabled() {
        testRestTemplate.postForObject(
                path(DISABLE_CACHE_MANAGER, TEST_CACHE_1 + "/disable"), defaultEntity(), Void.class);

        ResponseEntity<CachesFeed> response = testRestTemplate.getForEntity(rootPath(), CachesFeed.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CacheManagerDto cacheManager = getCacheManager(response.getBody(), DISABLE_CACHE_MANAGER);

        CacheDto cache1Info = cacheManager.getCaches().stream()
                .filter(c -> TEST_CACHE_1.equals(c.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(cache1Info.isEnabled()).isFalse();

        CacheDto cache2Info = cacheManager.getCaches().stream()
                .filter(c -> TEST_CACHE_2.equals(c.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(cache2Info.isEnabled()).isTrue();
    }

    // TODO: I'm not sure that this return 200 OK is the correct way of handling the non existent cache
    @Test
    @Disabled
    void enableCache_shouldHandleNonExistentCache() {
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                path(ENABLE_CACHE_MANAGER, "/nonExistentCache/enable"), defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void disableCache_shouldHandleNonExistentCache() {
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                path(DISABLE_CACHE_MANAGER, "/nonExistentCache/disable"), defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("nonExistentManagerPaths")
    void managerOperation_shouldThrowExceptionForNonExistentManager(String cacheManagerName, String relativePath) {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(path(cacheManagerName, relativePath), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static Stream<Arguments> nonExistentManagerPaths() {
        return Stream.of(
                Arguments.of("nonExistentManager", "/enable"), Arguments.of("/nonExistentManager", "/disable"));
    }

    @Test
    @Disabled // TODO: Uncomment once we solve the exception handling on the starter side
    void clearAll_shouldReturnFalse_cacheManagerDoesNotExist() {
        ResponseEntity<Void> response = testRestTemplate.exchange(
                path("/nonExistentManager/clear-all", ""), HttpMethod.DELETE, defaultEntity(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("nonExistentManagerAndCachePaths")
    void cacheOperation_shouldThrowExceptionForNonExistentManager(String cacheManagerName, String relativePath) {
        ResponseEntity<String> response =
                testRestTemplate.postForEntity(path(cacheManagerName, relativePath), defaultEntity(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static Stream<Arguments> nonExistentManagerAndCachePaths() {
        return Stream.of(
                Arguments.of("/nonExistentManager", "/nonExistentCache/enable"),
                Arguments.of("/nonExistentManager", "/nonExistentCache/disable"));
    }

    private HttpEntity<Void> defaultEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
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

    private CacheManagerDto getCacheManager(CachesFeed source, String cacheManagerName) {
        return source.getCacheManagers().stream()
                .filter(cm -> cacheManagerName.equals(cm.getName()))
                .findFirst()
                .orElseThrow();
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

        @Bean(name = MAIN_CACHE_MANAGER)
        @Primary
        public CacheManager testSubjectCacheManager() {
            return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
        }

        @Bean(name = CLEAR_CACHE_MANAGER)
        public CacheManager clearSubjectCacheManager() {
            return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
        }

        @Bean(name = ENABLE_CACHE_MANAGER)
        public CacheManager enableSubjectCacheManager() {
            return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
        }

        @Bean(name = DISABLE_CACHE_MANAGER)
        public CacheManager disableSubjectCacheManager() {
            return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
        }
    }
}
