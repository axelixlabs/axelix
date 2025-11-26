package com.nucleonforge.axile.sbs.spring.cache;

import org.springframework.cache.CacheManager;

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
}
