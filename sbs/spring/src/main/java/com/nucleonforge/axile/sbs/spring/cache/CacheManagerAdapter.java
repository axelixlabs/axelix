package com.nucleonforge.axile.sbs.spring.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Adapter to Spring's {@link CacheManager}.
 *
 * <p>
 * Provides a unified interface for performing cache clearing operations.
 * All methods return {@code true} if at least one entry was removed,
 * and {@code false} if no entries were removed.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public interface CacheManagerAdapter {

    /**
     * Clear the cache with the given name.
     *
     * @param cacheName the name of the cache to clear
     */
    boolean clear(String cacheName);

    /**
     * Clear all caches managed by this {@link CacheManager}.
     */
    boolean clearAll();

    /**
     * Clear the specific entry in the specific cache.
     *
     * @param cacheName the name of the cache to clear
     * @param key       the key to evict
     */
    boolean clear(String cacheName, Object key);

    /**
     * Enable all caches in this cache manager, allowing them to perform caching operations.
     */
    void enableCacheManager();

    /**
     * Disable all caches in this cache manager, preventing them from performing caching operations.
     * <p>
     * Please note, that this API disabled all the caches inside the given cache manager
     * that are only known by the time of this exact invocation. Some underlying {@link CacheManager}
     * implementations (such as {@link ConcurrentMapCacheManager} for instance) support the dynamic
     * addition of {@link org.springframework.cache.Cache caches}. The caches that are going to be added
     * dynamically later after the given invocation of this method will not be disabled.
     */
    void disableCacheManager();

    /**
     * Enable a specific cache by name.
     * This activates caching operations for the specified cache only.
     *
     * @param cacheName the name of the cache to enable
     */
    void enableCache(String cacheName);

    /**
     * Disable a specific cache by name.
     * This deactivates caching operations for the specified cache only.
     *
     * @param cacheName the name of the cache to disable
     */
    void disableCache(String cacheName);

    /**
     * Check if a specific cache is currently enabled.
     *
     * @param cacheName the name of the cache to check
     * @return {@code true} if the cache is enabled, {@code false} if it's disabled
     */
    boolean isCacheEnabled(String cacheName);
}
